package com.flipbook.app.Users;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import com.flipbook.app.R;
import com.flipbook.app.Posting.RequestSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Hayden on 2017-03-03.
 */

public class ProfileActivity extends AppCompatActivity {

    private final static String GET_POSTS_URL = "https://railsphotoapp.herokuapp.com//api/v1/profile.json";

    private ImageButton profile;
    private Button editProfile;
    private TextView textName, textUsername,textPosts, textFollowing, textFollowers, textBio;
    private ImageView profilePic;
    private GridView showPosts;
    private String getEmail, getToken, username, name, bio, followers, followings, posts, email;
    public static Activity profileActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

       profileActivity = this;

        profile = (ImageButton) findViewById(R.id.profile);
        profile.setImageResource(R.drawable.profile_selected);

        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        getEmail = prefs.getString("email", "");
        getToken = prefs.getString("auth_token", "");

        profile = (ImageButton) findViewById(R.id.profile);
        profile.setImageResource(R.drawable.profile_selected);

        editProfile = (Button) findViewById(R.id.editButton);
        textName = (TextView) findViewById(R.id.name);
        textPosts = (TextView) findViewById(R.id.posts);
        textFollowers = (TextView) findViewById(R.id.followers);
        textFollowing = (TextView) findViewById(R.id.following);
        textBio = (TextView) findViewById(R.id.bio);
        showPosts = (GridView) findViewById(R.id.showPosts);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        getProfile(builder);

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("username", username);
                intent.putExtra("email", email);
                intent.putExtra("bio", bio);
                startActivity(intent);
            }
        });
    }

    private void getProfile(final AlertDialog.Builder builder) {
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GET_POSTS_URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
                try {
                    JSONObject user = (JSONObject) response.get("user");
                    name = user.getString("name");
                    username = user.getString("username");
                    email = user.getString("email");
                    posts = user.getString("get_posts");
                    followers = user.getString("get_followers");
                    followings = user.getString("get_followings");
                    bio = user.getString("bio");

                    textName.setText(name);
                    textPosts.setText(posts + "  Posts");
                    textFollowers.setText(followers + "  Followers");
                    textFollowing.setText(followings + "  Following");
                    textBio.setText(bio);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ProfileActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
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
