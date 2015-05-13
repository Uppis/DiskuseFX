package com.vajasoft.diskusefx;

import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Pertti Uppala
 */
public class Util {

    public static Initializable setSceneContent(URL fromFxml, Stage toStage, double width, double height) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(fromFxml);
        Parent root = (Parent) loader.load();
        Scene scene = new Scene(root, width, height);
        toStage.setScene(scene);

        return (Initializable) loader.getController();
    }

    private Util() {
    }
}
