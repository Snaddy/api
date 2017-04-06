package com.flipbook.app;

import java.util.ArrayList;

/**
 * Created by Hayden on 2017-03-06.
 */

public class Posts {

    private String username, caption, likes, images;
    private int speed;

    public Posts(String username, String caption, String likes, int speed, String images){
        this.setUsername(username);
        this.setCaption(caption);
        this.setLikes(likes);
        this.setImages(images);
        this.setSpeed(speed);
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images= images;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
