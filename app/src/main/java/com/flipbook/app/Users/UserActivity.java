package com.flipbook.app.Users;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.flipbook.app.Posts.RequestSingleton;
import com.flipbook.app.R;
import com.flipbook.app.Welcome.WelcomeActivity;

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

public class UserActivity extends AppCompatActivity {

    private final static String GET_USER_URL = "https://railsphotoapp.herokuapp.com//api/v1/users/";
    private final static String FOLLOW_URL = "https://railsphotoapp.herokuapp.com//api/v1/relationships/";

    private ImageButton back;
    private Button profileButton;
    private TextView textUsername, textName, textPosts, textFollowing, textFollowers, textBio;
    private ImageView profilePic;
    private RelativeLayout profileInfo;
    private GridView showPosts;
    private String getEmail, getToken, userId;
    private ProgressBar loader;
    private Boolean isFollowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        getEmail = prefs.getString("email", "");
        getToken = prefs.getString("auth_token", "");

        Bundle bundle = getIntent().getExtras();
        userId = bundle.getString("userId");

        profileButton = (Button) findViewById(R.id.followButton);
        profilePic = (ImageView) findViewById(R.id.profilePic);
        profileInfo = (RelativeLayout) findViewById(R.id.profile_info);
        textName = (TextView) findViewById(R.id.name);
        textPosts = (TextView) findViewById(R.id.posts);
        textFollowers = (TextView) findViewById(R.id.followers);
        textFollowing = (TextView) findViewById(R.id.following);
        textUsername = (TextView) findViewById(R.id.username);
        textBio = (TextView) findViewById(R.id.bio);
        showPosts = (GridView) findViewById(R.id.showPosts);
        back = (ImageButton) findViewById(R.id.back);
        loader = (ProgressBar) findViewById(R.id.loader);

        getProfile();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getProfile() {
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GET_USER_URL + userId, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
                try {
                    loader.setVisibility(View.GONE);
                    profileInfo.setVisibility(View.VISIBLE);
                    showPosts.setVisibility(View.VISIBLE);
                    JSONObject user = (JSONObject) response.get("user");
                    String username = user.getString("username");
                    String name = user.getString("name");
                    try {
                        name = URLDecoder.decode(name, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    String posts = user.getString("get_posts");
                    String followers = user.getString("get_followers");
                    String followings = user.getString("get_followings");
                    String bio = user.getString("bio");
                    Boolean following = user.getBoolean("is_following");
                    String avatar = user.getString("avatar");
                    Glide.with(getApplicationContext()).load(avatar).dontAnimate().into(profilePic);
                    try {
                        bio = URLDecoder.decode(bio, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    textName.setText(name);
                    textPosts.setText(posts + "  Posts");
                    textFollowers.setText(followers + "  Followers");
                    textFollowing.setText(followings + "  Following");
                    textUsername.setText("@" + username);
                    textBio.setText(bio);
                    isFollowing = following;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(UserActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse == null) {
                    Toast.makeText(UserActivity.this, "Unable to load user profile. Check internet connection", Toast.LENGTH_SHORT).show();
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
        RequestQueue requestQueue = RequestSingleton.getInstance(UserActivity.this.getApplicationContext()).getRequestQueue();
        RequestSingleton.getInstance(UserActivity.this).addToRequestQueue(jsonObjectRequest);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isFollowing) {
                    //profileButton.setEnabled(false);
                    profileInfo.setSelected(!profileButton.isSelected());
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, FOLLOW_URL + userId, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            profileButton.setText("Following");
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }) {

                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("X-User-Token", WelcomeActivity.getToken);
                            headers.put("X-User-Email", WelcomeActivity.getEmail);
                            return headers;
                        }
                    };
                    RequestQueue requestQueue = RequestSingleton.getInstance(getApplicationContext().getApplicationContext()).getRequestQueue();
                    RequestSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                } else {
                    //profileButton.setEnabled(false);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, FOLLOW_URL + userId, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            profileButton.setText("Follow");
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }) {

                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("X-User-Token", WelcomeActivity.getToken);
                            headers.put("X-User-Email", WelcomeActivity.getEmail);
                            return headers;
                        }
                    };
                    RequestQueue requestQueue = RequestSingleton.getInstance(getApplicationContext().getApplicationContext()).getRequestQueue();
                    RequestSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                }
            }
        });
    }

    public void onPause(){
        super.onPause();
        overridePendingTransition(0,0);
    }
}
