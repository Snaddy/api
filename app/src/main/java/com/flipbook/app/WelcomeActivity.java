package com.flipbook.app;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hayden on 2017-02-25.
 */

public class WelcomeActivity extends AppCompatActivity {

    private final static String GET_POSTS_URL = "https://aqueous-river-91475.herokuapp.com/api/v1/posts.json";
    private final static String BASE_URL = "https://aqueous-river-91475.herokuapp.com";

    private String getEmail, getToken;
    private ImageButton home;
    private ListView feed;
    private PostAdapter postAdapter;
    private ProgressBar loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        getEmail = prefs.getString("email", "");
        getToken = prefs.getString("auth_token", "");

        postAdapter = new PostAdapter(this, R.layout.postitem);

        home = (ImageButton) findViewById(R.id.home);
        home.setImageResource(R.drawable.home_selected);

        feed = (ListView) findViewById(R.id.feed);
        feed.setAdapter(postAdapter);

        loader = (ProgressBar) findViewById(R.id.loader);

        final Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);

         final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, GET_POSTS_URL, null,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    //loop through json Array
                    String username = "";
                    String caption = "";
                    String likes = "";
                    String imageUrl = "";
                    for (int i = 0; i < response.length(); i++){
                        JSONObject post = (JSONObject) response.get(i);
                        JSONObject user = (JSONObject) post.get("user");
                        JSONArray images = (JSONArray) post.get("images");
                        username = user.getString("username");
                        caption = post.getString("caption");
                        likes = post.getString("get_likes_count");
                        imageUrl = "";
                        for(int j = 0; j < images.length(); j++) {
                            JSONObject image = (JSONObject) images.get(j);
                            imageUrl = BASE_URL + image.getString("url");
                        }
                        Posts posts = new Posts(username, caption, likes, imageUrl);
                        postAdapter.add(posts);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loader.animate().translationY(-loader.getHeight()/2).scaleY(0).setDuration(150).setListener(new AnimatorListenerAdapter() {
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
            }
        }){
             @Override
             public String getBodyContentType() {
                 return "application/json; charset=utf-8";
             }
             @Override
             public Map<String, String> getHeaders() throws AuthFailureError {
                 HashMap<String, String> headers = new HashMap<>();
                 headers.put("X-User-Token", getToken);
                 headers.put("X-User-Email", getEmail);
                 System.out.println(headers.toString());
                 return headers;
             }
         };
        RequestQueue requestQueue = RequestSingleton.getInstance(WelcomeActivity.this.getApplicationContext()).getRequestQueue();
        RequestSingleton.getInstance(WelcomeActivity.this).addToRequestQueue(jsonArrayRequest);
    }
}
