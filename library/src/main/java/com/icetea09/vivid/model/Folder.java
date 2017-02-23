package com.icetea09.vivid.model;

import java.util.ArrayList;

public class Folder {

    private String folderName;
    private ArrayList<Image> images;

    public Folder(String folderName) {
        this.folderName = folderName;
        images = new ArrayList<>();
    }

    public String getFolderName() {
        return folderName;
    }

    public ArrayList<Image> getImages() {
        return images;
    }

    public void setImages(ArrayList<Image> images) {
        this.images = images;
    }
}
