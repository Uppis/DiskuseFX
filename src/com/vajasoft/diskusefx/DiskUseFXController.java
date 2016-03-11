package com.vajasoft.diskusefx;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    private static final NodeComparator COMPARATOR = new NodeComparator();
    private static final int X_LARGEST = 10;
    private static final long[] SIZE_UNIT = {1, 1024, 1024*1024, 1024*1024*1024};
    private static final String[] SIZE_UNIT_LABEL = {"B", "KB", "MB", "GB"};
    
    private final NumberFormat sizeFormatter = NumberFormat.getNumberInstance();
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
    BorderPane chartPane;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sizeFormatter.setMaximumFractionDigits(1);
        dirChooser = new DirectoryChooser();
        dirTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FileNode>() {
            @Override
            public void changed(ObservableValue<? extends FileNode> ov, FileNode oldVal, FileNode newVal) {
                if (newVal != null && !newVal.getChildren().isEmpty()) {
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
        int chartLimit = Math.min(node.getChildren().size(), X_LARGEST);
        String categoryLabel = chartLimit + " largest";

        List<TreeItem<String>> sortBuf = new ArrayList<>(node.getChildren());
        Collections.sort(sortBuf, COMPARATOR);

        int totalSizeUnit = getSizeUnit(node.getSize());
        int dataSizeUnit = getSizeUnit(((FileNode)sortBuf.get(0)).getSize());

        ObservableList<XYChart.Data<String, Double>> data = FXCollections.observableArrayList();
        for (int i = 0; i < chartLimit && i < sortBuf.size(); i++) {
            FileNode n = (FileNode)sortBuf.get(i);
            String x = n.getName();
            double y = (double)n.getSize() / SIZE_UNIT[dataSizeUnit];
            data.add(new XYChart.Data<>(x, y));
        }

        BarChart chart = new BarChart(new CategoryAxis(), new NumberAxis());
        chart.setAnimated(false);
        chart.setLegendVisible(false);

        XYChart.Series series = new XYChart.Series(categoryLabel, data);
        chart.setTitle(node.getPathName() + " (" + sizeFormatter.format((double)node.getSize() / SIZE_UNIT[totalSizeUnit]) + " " + SIZE_UNIT_LABEL[totalSizeUnit] + ")");
        chart.getYAxis().setLabel(SIZE_UNIT_LABEL[dataSizeUnit]);
        chart.getXAxis().setLabel(categoryLabel);
        chart.getData().clear();
        chart.getData().add(series);

        chartPane.setCenter(chart);
    }

    private void setCommands() {
        boolean hasCurrent = currentRoot != null;
        boolean hasParent = hasCurrent && currentRoot.getParent() != null;

        mnuRefresh.setDisable(!hasCurrent);
        cmdRefresh.setDisable(!hasCurrent);

        mnuParent.setDisable(!hasParent);
        cmdParent.setDisable(!hasParent);
    }

    private int getSizeUnit(long forSize) {
        int ret = SIZE_UNIT.length - 1;
        while (ret > 0  && SIZE_UNIT[ret] > forSize)
            ret--;
        return ret;
    }

    private static class NodeComparator implements Comparator<TreeItem<String>> {
        @Override
        public int compare(TreeItem<String> o1, TreeItem<String> o2) {
            if (((FileNode)o1).getSize() < ((FileNode)o2).getSize()) {
                return 1;
            } else if (((FileNode)o1).getSize() > ((FileNode)o2).getSize()) {
                return -1;
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
                dirTree.requestFocus();
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
