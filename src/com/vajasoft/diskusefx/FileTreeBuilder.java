package com.vajasoft.diskusefx;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class FileTreeBuilder extends SimpleFileVisitor<Path> implements EventHandler<WorkerStateEvent> {
    FileVisitResult result = FileVisitResult.CONTINUE;
    FileNode root;
    FileNode cursor;

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        FileNode folder = new FileNode(dir);
        if (cursor != null) {
            cursor.insertNode(folder);
        } else {
            root = folder;
        }
        cursor = folder;
        return result;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        FileNode node = new FileNode(file);
        if (cursor != null) {
            cursor.insertNode(node);
        } else {
            root = node;
        }
        return result;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        FileNode parent = (FileNode) cursor.getParent();
        if (parent != null) {
            parent.increaseSize(cursor.getSize());
            cursor = parent;
        }
        return result;
    }

    public FileNode getRoot() {
        return root;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        exc.printStackTrace();
        return result;
    }

    @Override
    public void handle(WorkerStateEvent t) {
        if (t.getEventType().equals(WorkerStateEvent.WORKER_STATE_CANCELLED)) {
            result = FileVisitResult.TERMINATE;
        }
    }
}
