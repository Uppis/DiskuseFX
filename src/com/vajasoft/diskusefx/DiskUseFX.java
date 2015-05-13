package com.vajasoft.diskusefx;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author Pertti Uppala
 */
public class DiskUseFX extends Application {
    AppContext ctx;
    
    @Override
    public void start(Stage stage) throws Exception {
        ctx = new AppContext(stage);
        DiskUseFXController controller = (DiskUseFXController)Util.setSceneContent(DiskUseFX.class.getResource("DiskUseFX.fxml"), stage, 800, 600);
        controller.setAppContext(ctx);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application. main() serves only as fallback in case the
     * application can not be launched through deployment artifacts, e.g., in IDEs with limited FX support. NetBeans
     * ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}