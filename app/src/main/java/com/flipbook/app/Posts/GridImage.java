package com.flipbook.app.Posts;

/**
 * Created by Hayden on 2017-09-06.
 */

public class GridImage {
    private String id, image;

    public GridImage(String id, String image) {
        this.id = id;
        this.setImage(image);
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
