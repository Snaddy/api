package com.flipbook.app.Posts;

/**
 * Created by Hayden on 2017-09-06.
 */

public class GridImage {
    private String id, image, username;
    private boolean checked;

    public GridImage(String id, String image, String username) {
        this.id = id;
        this.setImage(image);
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getUsername() {return username;}

    public void setImage(String image) {
        this.image = image;
    }

}
