package com.flipbook.app;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Hayden on 2017-02-25.
 */

public class WelcomeActivity extends AppCompatActivity {

    private final static String GET_POSTS_URL = "https://railsphotoapp.herokuapp.com//api/v1/posts.json";

    private String getEmail, getToken;
    private ImageButton home;
    private ListView feed;
    private ImageView iv;
    private PostAdapter postAdapter;
    private ProgressBar loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        if (prefs.getString("auth_token", "") == "" && prefs.getString("email", "") == "") {
            startActivity(new Intent(getApplicationContext(), LaunchActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            finish();
        } else {

            //iv = (ImageView) findViewById(R.id.imageView2);

            //Glide.with(getApplicationContext()).load("https://dytun7vbm6t2g.cloudfront.net/uploads/post/images/134/image0.jpg").into(iv);

            getEmail = prefs.getString("email", "");
            getToken = prefs.getString("auth_token", "");

            //System.out.println(getToken);

            postAdapter = new PostAdapter(this, R.layout.postitem);

            home = (ImageButton) findViewById(R.id.home);
            home.setImageResource(R.drawable.home_selected);

            feed = (ListView) findViewById(R.id.feed);
            feed.setAdapter(postAdapter);

            loader = (ProgressBar) findViewById(R.id.loader);

            final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, GET_POSTS_URL, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        //loop through json Array
                        String username;
                        String caption ;
                        String likes;
                        String url;
                        int speed;
                        int id;
                        System.out.println(response.length());
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject post = (JSONObject) response.get(i);
                            JSONObject user = (JSONObject) post.get("user");
                            JSONArray images = (JSONArray) post.get("images");
                            username = user.getString("username");
                            caption = post.getString("caption");
                            likes = post.getString("get_likes_count");
                            speed = post.getInt("speed");
                            id = post.getInt("id");
                            ArrayList<String> imageUrls = new ArrayList<>();
                            for (int j = 0; j < images.length(); j++) {
                                url = "https://dytun7vbm6t2g.cloudfront.net/uploads/post/images/" + id + "/image" + j + ".jpg";
                                imageUrls.add(url);
                            }
                            Posts posts = new Posts(username, caption, likes, speed ,imageUrls);
                            postAdapter.add(posts);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    loader.animate().translationY(-loader.getHeight() / 2).scaleY(0).setDuration(150).scaleX(0).setDuration(150).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            loader.setVisibility(View.GONE);
                        }
                    });
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(WelcomeActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    NetworkResponse networkResponse = error.networkResponse;
                    if(networkResponse == null){
                        loader.animate().translationY(-loader.getHeight() / 2).scaleY(0).setDuration(150).scaleX(0).setDuration(150).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                loader.setVisibility(View.GONE);
                            }
                        });
                        Toast.makeText(WelcomeActivity.this, "Unable to update feed.", Toast.LENGTH_SHORT).show();
                    }
                    if (networkResponse != null && networkResponse.statusCode == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                        final Dialog dialog = new Dialog(WelcomeActivity.this);
                        dialog.setContentView(R.layout.dialog);
                        dialog.setTitle("Uh oh! Error...");
                        TextView message = (TextView) dialog.findViewById(R.id.message);
                        message.setText("Session ended. Please login again.");
                        //dialog button
                        Button okButton = (Button) dialog.findViewById(R.id.okButton);
                        okButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                finish();
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                        dialog.setCanceledOnTouchOutside(false);
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
            RequestQueue requestQueue = RequestSingleton.getInstance(WelcomeActivity.this.getApplicationContext()).getRequestQueue();
            RequestSingleton.getInstance(WelcomeActivity.this).addToRequestQueue(jsonArrayRequest);
        }
    }
}
