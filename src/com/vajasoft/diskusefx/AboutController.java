package com.vajasoft.diskusefx;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

/**
 * FXML Controller class
 *
 * @author Pertti Uppala
 */
public class AboutController implements Initializable {
    private AppContext ctx;
    private @FXML TextArea fldAbout;
    private @FXML Button cmdClose;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        StringBuilder buf = new StringBuilder("DiskuseFX v1.0\n");
        buf.append("\nJava Version: ").append(System.getProperty("java.version"));
        buf.append("\nOperating system: ").append(System.getProperty("os.name"));
        fldAbout.setText(buf.toString());
    }    

    public void setAppContext(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML
    private void onClose(ActionEvent event) {
        ctx.getWindow().close();
    }
}
