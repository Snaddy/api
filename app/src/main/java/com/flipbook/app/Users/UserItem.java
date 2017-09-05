package com.flipbook.app.Users;

import java.util.ArrayList;

/**
 * Created by Hayden on 2017-09-04.
 */

public class UserItem {

    private String username, name, id;
    private String avatar;

    public UserItem(String username, String name, String id ,String avatar){
        this.setUsername(username);
        this.setName(name);
        this.id = id;
        this.setAvatar(avatar);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getId() {
        return id;
    }
}
