package com.flipbook.app;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hayden on 2017-02-25.
 */

public class WelcomeActivity extends AppCompatActivity {

    private final static String GET_POSTS_URL = "https://aqueous-river-91475.herokuapp.com/api/v1/posts.json";
    ImageButton home, notifications, newPost, search, profile;

    private TextView txtResponse;
    private String jsonResponse, getEmail, getToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        getEmail = prefs.getString("email", "");
        getToken = prefs.getString("auth_token", "");

        home = (ImageButton) findViewById(R.id.home);
        notifications = (ImageButton) findViewById(R.id.notifications);
        newPost = (ImageButton) findViewById(R.id.newPost);
        search = (ImageButton) findViewById(R.id.search);
        profile = (ImageButton) findViewById(R.id.profile);

        txtResponse = (TextView) findViewById(R.id.posts);

        home.setImageResource(R.drawable.home_selected);

         final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, GET_POSTS_URL, null,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    //loop through json Arrays
                    jsonResponse = "";

                    for (int i = 0; i < response.length(); i++){
                        JSONObject post = (JSONObject) response.get(i);
                        String data = post.getString("caption");

                        jsonResponse += data;
                    }
                    txtResponse.setText(jsonResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
