package com.vajasoft.diskusefx;

import java.nio.file.Files;
import java.nio.file.Path;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;

/**
 *
 * @author z705692
 */
public class TreeBuilderTask extends Task<TreeItem> {
    private final Path rootFolder;

    public TreeBuilderTask(Path rootFolder, EventHandler<WorkerStateEvent> handler) {
        super();
        this.rootFolder = rootFolder;
        setEventHandler(WorkerStateEvent.ANY, handler);
    }

    @Override
    protected TreeItem call() throws Exception {
        FileTreeBuilder builder = new FileTreeBuilder();
        Files.walkFileTree(rootFolder, builder);
        return builder.getRoot();
    }
}
