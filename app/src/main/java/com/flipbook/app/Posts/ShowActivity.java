package com.flipbook.app.Posts;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.flipbook.app.Comments.Comment;
import com.flipbook.app.Comments.CommentAdapter;
import com.flipbook.app.R;
import com.flipbook.app.Welcome.WelcomeActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Hayden on 2017-09-06.
 */

public class ShowActivity extends AppCompatActivity {

    private final static String GET_POST_URL = "https://railsphotoapp.herokuapp.com//api/v1/post/";

    private ListView comments, feed;
    private TextView usernameView;
    private EditText newComment;
    private ImageButton back, send;
    private String getEmail, getToken, postId, comment;
    private PostAdapter postAdapter;
    private CommentAdapter commentAdapter;
    public static String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        postAdapter = new PostAdapter(this, R.layout.post_item);
        commentAdapter = new CommentAdapter(this, R.layout.comment_item);

        getEmail = prefs.getString("email", "");
        getToken = prefs.getString("auth_token", "");

        comments = (ListView) findViewById(R.id.commentsList);
        comments.setAdapter(commentAdapter);
        usernameView = (TextView) findViewById(R.id.username);
        newComment = (EditText) findViewById(R.id.newComment);
        back = (ImageButton) findViewById(R.id.back);
        send = (ImageButton) findViewById(R.id.send);
        feed = (ListView) findViewById(R.id.feed);
        feed.setAdapter(postAdapter);

        Bundle bundle = getIntent().getExtras();
        username = bundle.getString("username");
        postId = bundle.getString("postId");

        usernameView.setText(username);

        getPost();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    comment = URLEncoder.encode(newComment.getText().toString(), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                hideKeyboard(ShowActivity.this);
                sendComment(comment);
                newComment.setText("");
            }
        });
    }

    private void getPost(){
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GET_POST_URL +
                    postId, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject post = (JSONObject)response.get("post");
                        JSONObject user = (JSONObject) post.get("user");
                        JSONArray images = (JSONArray) post.get("images");
                        String caption = post.getString("caption");
                        String decodedCaption = URLDecoder.decode(caption, "utf-8");
                        String userId = user.getString("id");
                        JSONObject userAvatar = user.getJSONObject("avatar");
                        String userAvatarUrl = userAvatar.getString("url");
                        int postDate = Integer.parseInt(post.getString("posted"));
                        int likes_count = post.getInt("get_likes_count");
                        int comment_count = post.getInt("get_comment_count");
                        int speed = post.getInt("speed");
                        boolean isLiked = post.getBoolean("liked");
                        ArrayList<String> imageUrls = new ArrayList<>();
                        //add post
                        for (int j = 0; j < images.length(); j++) {
                            JSONObject image = images.getJSONObject(j);
                            String url = image.getString("url");
                            imageUrls.add(url);
                        }
                        Posts posts = new Posts(username, decodedCaption, postId, userAvatarUrl, likes_count, comment_count,
                                speed ,imageUrls, isLiked, userId, WelcomeActivity.getTimeAgo(postDate), false, false);
                        postAdapter.add(posts);
                        //add comments associated with post
                        JSONArray comments = post.getJSONArray("comments");
                        for (int k = 0; k < comments.length(); k++) {
                            JSONObject commentObject = comments.getJSONObject(k);
                            JSONObject commentObject2 = commentObject.getJSONObject("comment");
                            String text = commentObject2.getString("content");
                            String decodedText = URLDecoder.decode(text, "utf-8");
                            String id = commentObject2.getString("id");
                            int postedAt = Integer.parseInt(commentObject2.getString("posted_at"));
                            JSONObject userObject = commentObject2.getJSONObject("user");
                            String username = userObject.getString("username");
                            String commenterId = userObject.getString("id");
                            JSONObject commenterAvatarObject = userObject.getJSONObject("avatar");
                            String commenterAvatarUrl = commenterAvatarObject.getString("url");
                            Comment comment = new Comment(id, commenterId, postId, decodedText, username,
                                    commenterAvatarUrl, WelcomeActivity.getTimeAgo(postedAt));
                            commentAdapter.add(comment);
                        }
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Toast.makeText(ShowActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse == null) {
                        Toast.makeText(ShowActivity.this, "Unable to load post. Check internet connection", Toast.LENGTH_SHORT).show();
                    }
                    assert networkResponse != null;
                    if (((networkResponse.statusCode == HttpsURLConnection.HTTP_UNAUTHORIZED) ||
                            (networkResponse.statusCode == HttpsURLConnection.HTTP_CLIENT_TIMEOUT))) {
                            //do stuff <- hahahhaha
                            }
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-User-Token", getToken);
                    headers.put("X-User-Email", getEmail);
                    return headers;
                }
            };
            RequestQueue requestQueue = RequestSingleton.getInstance(ShowActivity.this.getApplicationContext()).getRequestQueue();
            RequestSingleton.getInstance(ShowActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private void sendComment(final String encodedComment){
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GET_POST_URL + postId +
                "/comments/" + encodedComment, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
                try {
                    if(response.getString("status").equals("success")){
                        String id = response.getString("id");
                        String userId = response.getString("user_id");
                        String postId = response.getString("post_id");
                        String content = response.getString("content");
                        String username = response.getString("username");
                        JSONObject avatarJson = response.getJSONObject("avatar");
                        String avatar = avatarJson.getString("url");
                        Comment comment = new Comment(id, userId, postId, content, username, avatar, "1s");
                        commentAdapter.add(comment);
                        commentAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse == null) {
                    Toast.makeText(ShowActivity.this, "Unable to upload comment. Check internet connection", Toast.LENGTH_SHORT).show();
                }
                if ((networkResponse != null) && ((networkResponse.statusCode == HttpsURLConnection.HTTP_UNAUTHORIZED) ||
                        (networkResponse.statusCode == HttpsURLConnection.HTTP_CLIENT_TIMEOUT))) {
                }
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("X-User-Token", getToken);
                headers.put("X-User-Email", getEmail);
                return headers;
            }
        };
        RequestQueue requestQueue = RequestSingleton.getInstance(ShowActivity.this.getApplicationContext()).getRequestQueue();
        RequestSingleton.getInstance(ShowActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
