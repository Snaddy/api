package com.flipbook.app;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Hayden on 2017-02-25.
 */

public class WelcomeActivity extends AppCompatActivity {

    private final static String GET_POSTS_URL = "https://railsphotoapp.herokuapp.com//api/v1/posts.json";

    public static String getEmail, getToken;
    private ImageButton home;
    private ListView feed;
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
            getEmail = prefs.getString("email", "");
            getToken = prefs.getString("auth_token", "");

            postAdapter = new PostAdapter(this, R.layout.postitem);

            home = (ImageButton) findViewById(R.id.home);
            home.setImageResource(R.drawable.home_selected);

            feed = (ListView) findViewById(R.id.feed);
            feed.setAdapter(postAdapter);

            loader = (ProgressBar) findViewById(R.id.loader);

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            //if api 19 change progress bar to fit color scheme
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                loader.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            }


            final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, GET_POSTS_URL, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    System.out.println(response);
                    try {
                        //loop through json Array
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject postObj = (JSONObject)response.get(i);
                            JSONObject post = (JSONObject)postObj.get("post");
                            JSONObject user = (JSONObject) post.get("user");
                            JSONArray images = (JSONArray) post.get("images");
                            String id = post.getString("id");
                            String username = user.getString("username");
                            String caption = post.getString("caption");
                            int likes_count = post.getInt("get_likes_count");
                            int speed = post.getInt("speed");
                            boolean isLiked = post.getBoolean("liked");
                            ArrayList<String> imageUrls = new ArrayList<>();
                            for (int j = 0; j < images.length(); j++) {
                                String url = "https://dytun7vbm6t2g.cloudfront.net/uploads/post/images/" + id + "/image" + j + ".jpg";
                                imageUrls.add(url);
                            }
                            Posts posts = new Posts(username, caption, id, likes_count, speed ,imageUrls, isLiked);
                            postAdapter.add(posts);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                   loader.setVisibility(View.GONE);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(WelcomeActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    NetworkResponse networkResponse = error.networkResponse;
                    if(networkResponse == null){
                        loader.setVisibility(View.GONE);
                        Toast.makeText(WelcomeActivity.this, "Unable to update feed. Check internet connection", Toast.LENGTH_SHORT).show();
                    }
                    if (networkResponse != null && (networkResponse.statusCode == HttpsURLConnection.HTTP_UNAUTHORIZED ||
                            networkResponse.statusCode == HttpsURLConnection.HTTP_CLIENT_TIMEOUT)) {
                        LayoutInflater inflater = WelcomeActivity.this.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.dialog, null);
                        builder.setView(dialogView);

                        TextView title = (TextView) dialogView.findViewById(R.id.title);
                        TextView message = (TextView) dialogView.findViewById(R.id.message);
                        Button ok = (Button) dialogView.findViewById(R.id.okButton);
                        title.setText("Connection timeout...");
                        message.setText("Please sign in again");
                        final AlertDialog alertDialog = builder.create();
                        ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.cancel();
                                startActivity(new Intent(getApplicationContext(), LaunchActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                                finish();
                            }
                        });
                        alertDialog.show();
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
            RequestSingleton.getInstance(WelcomeActivity.this).addToRequestQueue(jsonObjectRequest);
        }
    }
}
