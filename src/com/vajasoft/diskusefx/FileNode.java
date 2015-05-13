package com.vajasoft.diskusefx;

import java.io.IOException;
import java.nio.file.*;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FileNode extends TreeItem<String> {
    private static Image imgFolder = new Image("/image/folder.png");
    private String path;
    private String name;
    private boolean isDir;
    private long size;

    public FileNode(Path file) {
        try {
            path = file.normalize().toString();
            Path filename = file.getFileName();
            if (filename != null) {
                name = filename.toString();
            }
            isDir = Files.isDirectory(file);
            setValue(name);
            if (!isDir) {
                size = Files.size(file);
            } else {
                setGraphic(new ImageView(imgFolder));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPathName() {
        return path;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return isDir;
    }

    public long getSize() {
        return size;
    }

    void increaseSize(long by) {
        size += by;
    }

    public boolean isLeaf() {
        return isDirectory() == false;
    }

    public void insertNode(FileNode newChild) {
        size += newChild.getSize();
        String newChildName = newChild.getName();
        int ix = 0;
        for (int count = getChildren().size(); ix < count; ix++) {
            FileNode c = (FileNode)getChildren().get(ix);
            if (newChild.isDirectory()) {
                if (c.isDirectory() == false || newChildName.compareToIgnoreCase(c.getName()) < 0) {
                    break;
                }
            } else {
                if (c.isDirectory() == false && newChildName.compareToIgnoreCase(c.getName()) < 0) {
                    break;
                }
            }
        }
        getChildren().add(ix, newChild);
    }

//    @Override
//    public String toString() {
//        return name;
//    }
}
