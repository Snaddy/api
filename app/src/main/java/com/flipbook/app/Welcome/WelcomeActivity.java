package com.flipbook.app.Welcome;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.flipbook.app.Posts.PostAdapter;
import com.flipbook.app.Posts.Posts;
import com.flipbook.app.Posts.RequestSingleton;
import com.flipbook.app.R;
import com.flipbook.app.Users.LoginActivity;
import com.flipbook.app.Users.ProfileActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Hayden on 2017-02-25.
 */

public class WelcomeActivity extends AppCompatActivity {

    private static final String GET_POSTS_URL = "https://railsphotoapp.herokuapp.com//api/v1/posts.json";
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final int WEEK_MILLIS = 7 * DAY_MILLIS;
    private static final int SOCKET_TIMEOUT_MS = 10000;

    public static String getEmail, getToken;
    private ImageButton home;
    private ListView feed;
    private PostAdapter postAdapter;
    private ProgressBar loader;
    public static SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

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
            update(builder, editor);
        }
    }

    private void update(final AlertDialog.Builder builder, final SharedPreferences.Editor editor){
            final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, GET_POSTS_URL, null, new Response.Listener<JSONArray>() {
        @Override
        public void onResponse(JSONArray response) {
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
                    String decodedCaption = URLDecoder.decode(caption, "utf-8");
                    String userId = user.getString("id");
                    JSONObject userAvatar = user.getJSONObject("avatar");
                    String userAvatarUrl = userAvatar.getString("url");
                    int postDate = Integer.parseInt(post.getString("posted"));
                    int likes_count = post.getInt("get_likes_count");
                    int speed = post.getInt("speed");
                    boolean isLiked = post.getBoolean("liked");
                    ArrayList<String> imageUrls = new ArrayList<>();
                    for (int j = 0; j < images.length(); j++) {
                        JSONObject image = images.getJSONObject(j);
                        String url = image.getString("url");
                        imageUrls.add(url);
                    }
                    Posts posts = new Posts(username, decodedCaption, id, userAvatarUrl,likes_count, speed ,imageUrls, isLiked, userId, getTimeAgo(postDate), false);
                    postAdapter.add(posts);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            loader.setVisibility(View.GONE);
        }
    }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            //Toast.makeText(WelcomeActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            NetworkResponse networkResponse = error.networkResponse;
            if(networkResponse == null){
                loader.setVisibility(View.GONE);
                Toast.makeText(WelcomeActivity.this, "Unable to update feed. Check internet connection", Toast.LENGTH_SHORT).show();
            }
            if (networkResponse != null && (networkResponse.statusCode == HttpsURLConnection.HTTP_UNAUTHORIZED ||
                    networkResponse.statusCode == HttpsURLConnection.HTTP_CLIENT_TIMEOUT)) {
                editor.clear();
                editor.commit();
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
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
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
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    RequestQueue requestQueue = RequestSingleton.getInstance(WelcomeActivity.this.getApplicationContext()).getRequestQueue();
    RequestSingleton.getInstance(WelcomeActivity.this).addToRequestQueue(jsonObjectRequest);
}

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
    }

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return diff/SECOND_MILLIS + "s";
        } else if (diff < 60 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + "m";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + "h";
        } else if (diff < 7 * DAY_MILLIS){
            return diff / DAY_MILLIS + "d";
        } else{
            return diff / WEEK_MILLIS + "w";
        }
    }
}
