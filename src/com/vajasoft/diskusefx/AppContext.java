package com.vajasoft.diskusefx;

import javafx.stage.Stage;

/**
 *
 * @author z705692
 */
public class AppContext {
    private Stage mainWindow;
    private Stage window;

    public AppContext(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }

    public AppContext(AppContext parent, Stage window) {
        this.mainWindow = parent.getMainWindow();
        this.window = window;
    }

    public Stage getMainWindow() {
        return mainWindow;
    }

    public Stage getWindow() {
        return window != null ? window : mainWindow;
    }
}
