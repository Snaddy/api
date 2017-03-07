package com.flipbook.app;

/**
 * Created by Hayden on 2017-03-06.
 */

public class Posts {

    private String username, caption, likes;

    public Posts(String username, String caption, String likes){
        this.setUsername(username);
        this.setCaption(caption);
        this.setLikes(likes);
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
}
