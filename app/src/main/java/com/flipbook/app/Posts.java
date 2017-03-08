package com.flipbook.app;

import android.media.Image;

/**
 * Created by Hayden on 2017-03-06.
 */

public class Posts {

    private String username, caption, likes, imageUrl;

    public Posts(String username, String caption, String likes, String imageUrl){
        this.setUsername(username);
        this.setCaption(caption);
        this.setLikes(likes);
        this.setImages(imageUrl);
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
        return imageUrl;
    }

    public void setImages(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
