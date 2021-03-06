package com.flipbook.app.Users;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.flipbook.app.Posts.GridAdapter;
import com.flipbook.app.Posts.GridImage;
import com.flipbook.app.R;
import com.flipbook.app.Posts.RequestSingleton;
import com.flipbook.app.Settings.SettingActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Hayden on 2017-03-03.
 */

public class ProfileActivity extends AppCompatActivity {

    private final static String GET_POSTS_URL = "https://railsphotoapp.herokuapp.com//api/v1/profile.json";
    private final static String GET_FOLLOWERS = "https://railsphotoapp.herokuapp.com//api/v1/profile/followers.json";
    private final static String GET_FOLLOWINGS = "https://railsphotoapp.herokuapp.com//api/v1/profile/following.json";

    private ImageButton profile, settings, back;
    private Button editProfile;
    private TextView textName,textPosts, textFollowing, textFollowers, textBio;
    private ImageView profilePic;
    private GridView showPosts;
    private String getEmail, getToken, username, name, bio, followers, followings, posts;
    public static Activity profileActivity;
    private GridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileActivity = this;

        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        getEmail = prefs.getString("email", "");
        getToken = prefs.getString("auth_token", "");

        profile = (ImageButton) findViewById(R.id.profile);
        profile.setImageResource(R.drawable.profile_selected);

        editProfile = (Button) findViewById(R.id.followButton);
        settings = (ImageButton) findViewById(R.id.settings);
        back = (ImageButton) findViewById(R.id.back);
        textName = (TextView) findViewById(R.id.name);
        textPosts = (TextView) findViewById(R.id.posts);
        textFollowers = (TextView) findViewById(R.id.followers);
        textFollowing = (TextView) findViewById(R.id.following);
        textBio = (TextView) findViewById(R.id.bio);
        profilePic = (ImageView) findViewById(R.id.userAvatar);
        gridAdapter = new GridAdapter(this, R.layout.grid_item);
        showPosts = (GridView) findViewById(R.id.profilePosts);
        showPosts.setAdapter(gridAdapter);

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                startActivity(intent);
            }
        });
        getProfile();
        System.out.print(showPosts.toString());

        textFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserList.class);
                intent.putExtra("url", GET_FOLLOWERS);
                intent.putExtra("title", "Followers");
                startActivity(intent);
            }
        });

        textFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserList.class);
                intent.putExtra("url", GET_FOLLOWINGS);
                intent.putExtra("title", "Following");
                startActivity(intent);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getProfile() {
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GET_POSTS_URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //get user info
                    JSONObject user = (JSONObject) response.get("user");
                    JSONObject avatarObject = (JSONObject) user.get("avatar");
                    name = user.getString("name");
                    try {
                        name = URLDecoder.decode(name, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    username = user.getString("username");
                    posts = user.getString("get_posts");
                    followers = user.getString("get_followers");
                    followings = user.getString("get_followings");
                    bio = user.getString("bio");
                    try {
                        bio = URLDecoder.decode(bio, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    String avatarUrl = avatarObject.getString("url");
                    Glide.with(getApplicationContext()).load(avatarUrl).apply(new RequestOptions().circleCrop()).into(profilePic);
                    //set user info
                    textName.setText(name);
                    textPosts.setText(posts + "  Posts");
                    textFollowers.setText(followers + "  Followers");
                    textFollowing.setText(followings + "  Following");
                    if(bio.length() > 0) {
                        textBio.setText(bio);
                    } else {
                        textBio.setVisibility(View.GONE);
                    }

                    //get posts
                    JSONArray posts = user.getJSONArray("posts");
                    for (int j = 0; j < posts.length(); j++) {
                        JSONObject postObj = (JSONObject)posts.get(j);
                        JSONObject post = (JSONObject)postObj.get("post");
                        String id = post.getString("id");
                        JSONArray images = post.getJSONArray("images");
                        JSONObject url = images.getJSONObject(0);
                        String avatar = url.getString("url");
                        GridImage gridImage = new GridImage(id, avatar, username);
                        gridAdapter.add(gridImage);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(ProfileActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse == null) {
                    Toast.makeText(ProfileActivity.this, "Unable to load user profile. Check internet connection", Toast.LENGTH_SHORT).show();
                }
                if (networkResponse != null && (networkResponse.statusCode == HttpsURLConnection.HTTP_UNAUTHORIZED ||
                        networkResponse.statusCode == HttpsURLConnection.HTTP_CLIENT_TIMEOUT)) {
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
        RequestQueue requestQueue = RequestSingleton.getInstance(ProfileActivity.this.getApplicationContext()).getRequestQueue();
        RequestSingleton.getInstance(ProfileActivity.this).addToRequestQueue(jsonObjectRequest);
    }
}
