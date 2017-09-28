package com.flipbook.app.Comments;

/**
 * Created by Hayden on 2017-09-11.
 */

public class Comment {

    private String text, username, userAvatar, userId, postId, id;

    public Comment(String id, String userId, String postId, String text, String username, String userAvatar){
        this.text = text;
        this.username = username;
        this.userAvatar = userAvatar;
        this.id = id;
        this.userId = userId;
        this.postId = postId;
    }

    public String getText() {
        return text;
    }

    public String getUsername() {
        return username;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public String getUserId(){
        return userId;
    }

    public String getPostId(){
        return postId;
    }

    public String getId(){
        return id;
    }

}
