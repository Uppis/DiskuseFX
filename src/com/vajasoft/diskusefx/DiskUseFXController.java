package com.vajasoft.diskusefx;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Pertti Uppala
 */
public class DiskUseFXController implements Initializable {

    private static final NodeComparator sComparator = new NodeComparator(false);
    private static final int X_LARGEST = 10;
    private static final int SIZE_UNIT = 1024 * 1024;
    private static final String SIZE_UNIT_LABEL = "Mb";
    private AppContext ctx;
    private DirectoryChooser dirChooser;
    private File currentRoot;
    private @FXML
    MenuItem mnuParent;
    private @FXML
    MenuItem mnuRefresh;
    private @FXML
    Button cmdParent;
    private @FXML
    Button cmdRefresh;
    private @FXML
    TreeView dirTree;
    private @FXML
    BorderPane pnlChart;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dirChooser = new DirectoryChooser();
        dirTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FileNode>() {
            @Override
            public void changed(ObservableValue<? extends FileNode> ov, FileNode oldVal, FileNode newVal) {
                if (newVal != null) {
                    setChart(newVal);
                }
            }
        });
        setCommands();
    }

    void setAppContext(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML
    private void onExit(ActionEvent ev) {
        ctx.getWindow().close();
    }

    @FXML
    private void onOpen(ActionEvent ev) {
        File choice = dirChooser.showDialog(ctx.getWindow());
        if (choice != null) {
            setCurrent(choice);
        }
    }

    @FXML
    private void onParent(ActionEvent ev) {
        File f = currentRoot.getParentFile();
        if (f != null) {
            setCurrent(f);
        }
    }

    @FXML
    private void onRefresh(ActionEvent ev) {
        setCurrent(currentRoot);
    }

    @FXML
    private void onAbout(ActionEvent event) {
        try {
            Stage about = new Stage();
            AboutController controller = (AboutController) Util.setSceneContent(AboutController.class.getResource("About.fxml"), about, 400, 200);
            controller.setAppContext(new AppContext(ctx, about));
            about.initOwner(ctx.getWindow());
            about.initModality(Modality.WINDOW_MODAL);
            about.showAndWait();
        } catch (Exception ex) {
            Logger.getLogger(DiskUseFXController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setCurrent(File f) {
        try {
            dirChooser.setInitialDirectory(f.getParentFile());
            currentRoot = f.getCanonicalFile();
            TreeBuilderTask builder = new TreeBuilderTask(currentRoot.toPath(), new WorkerStateEventHandler());
            Thread th = new Thread(builder);
            th.setDaemon(true);
            th.start();
            setCommands();
        } catch (IOException ex) {
            Logger.getLogger(DiskUseFXController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setChart(FileNode node) {
        int count = node.getChildren().size();
        FileNode[] nodes = new FileNode[count];
        for (int i = 0; i < count; i++) {
            nodes[i] = (FileNode) node.getChildren().get(i);
        }
        java.util.Arrays.sort(nodes, sComparator);
        count = Math.min(count, X_LARGEST);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> topTen = new BarChart<>(xAxis, yAxis);
        topTen.setAnimated(false);
        topTen.getYAxis().setLabel(SIZE_UNIT_LABEL);
        topTen.setTitle(node.getPathName() + " (" + (node.getSize() / SIZE_UNIT) + " " + SIZE_UNIT_LABEL + ")");
        XYChart.Series series = new XYChart.Series();
        series.setName(count + " largest");
        for (int i = 0; i < count; i++) {
            series.getData().add(new XYChart.Data(nodes[i].getName(), nodes[i].getSize() / SIZE_UNIT));
        }
        topTen.getData().add(series);
//        pnlChart.getChildren().clear();
        pnlChart.setCenter(topTen);
    }

    private void setCommands() {
        boolean hasCurrent = currentRoot != null;
        boolean hasParent = hasCurrent && currentRoot.getParent() != null;

        mnuRefresh.setDisable(!hasCurrent);
        cmdRefresh.setDisable(!hasCurrent);

        mnuParent.setDisable(!hasParent);
        cmdParent.setDisable(!hasParent);
    }

    private static class NodeComparator implements java.util.Comparator {

        private final boolean ascend;

        public NodeComparator(boolean ascending) {
            ascend = ascending;
        }

        @Override
        public int compare(Object o1, Object o2) {
            FileNode n1 = (FileNode) o1;
            FileNode n2 = (FileNode) o2;
            if (n1.getSize() < n2.getSize()) {
                return ascend ? - 1 : 1;
            } else if (n1.getSize() > n2.getSize()) {
                return ascend ? 1 : -1;
            } else {
                return 0;
            }
        }
    }

    private class WorkerStateEventHandler implements EventHandler<WorkerStateEvent> {

        @Override
        public void handle(WorkerStateEvent t) {
            EventType etype = t.getEventType();
            if (WorkerStateEvent.WORKER_STATE_RUNNING.equals(etype)) {
                ctx.getWindow().getScene().setCursor(Cursor.WAIT);
            } else if (WorkerStateEvent.WORKER_STATE_SUCCEEDED.equals(etype)) {
                TreeItem root = (TreeItem) t.getSource().getValue();
                root.setExpanded(true);
                dirTree.setRoot(root);
                dirTree.getSelectionModel().selectFirst();
                ctx.getWindow().getScene().setCursor(Cursor.DEFAULT);
            } else if (WorkerStateEvent.WORKER_STATE_FAILED.equals(etype)) {
                Throwable ex = t.getSource().getException();
                if (ex != null) {
                    if (ex instanceof AccessDeniedException) {
                        Logger.getLogger(DiskUseFXController.class.getName()).log(Level.INFO, ex.toString());
                    } else {
                        Logger.getLogger(DiskUseFXController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                ctx.getWindow().getScene().setCursor(Cursor.DEFAULT);
            } else if (WorkerStateEvent.WORKER_STATE_CANCELLED.equals(etype)) {
                ctx.getWindow().getScene().setCursor(Cursor.DEFAULT);
            }
        }
    }
}
