package com.flipbook.app.Users;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.flipbook.app.Posts.RequestSingleton;
import com.flipbook.app.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Hayden on 2017-09-28.
 */

public class UserList extends AppCompatActivity {

    private static final String SEARCH_URL = "https://railsphotoapp.herokuapp.com//api/v1/search/";

    private ImageButton back;
    private EditText searchField;
    private ListView results;
    private UserAdapter userAdapter;
    private ProgressBar loader;
    private SharedPreferences prefs;
    private String getEmail, getToken, url, titleText;
    private TextView empty, title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        getEmail = prefs.getString("email", "");
        getToken = prefs.getString("auth_token", "");

        searchField = (EditText) findViewById(R.id.search_field);
        empty = (TextView) findViewById(R.id.empty);
        title = (TextView) findViewById(R.id.title);
        back = (ImageButton) findViewById(R.id.back);
        userAdapter = new UserAdapter(this, R.layout.user_item);
        loader = (ProgressBar) findViewById(R.id.loader);
        loader.setVisibility(View.INVISIBLE);
        results = (ListView) findViewById(R.id.results);
        results.setAdapter(userAdapter);

        Bundle bundle = getIntent().getExtras();
        url = bundle.getString("url");
        titleText = bundle.getString("title");
        title.setText(titleText);

        getList(url);

        searchField.addTextChangedListener(new TextWatcher() {
            //setup timer to run api request 0.5 secs after user is done typing
            private Timer typeDelay = new Timer();
            private final int DELAY = 1000;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                userAdapter.list.clear();
                userAdapter.notifyDataSetChanged();
                empty.setVisibility(View.INVISIBLE);
                if(searchField.getText().length() == 0){
                    loader.setVisibility(View.INVISIBLE);
                } else {
                    loader.setVisibility(View.VISIBLE);
                }
                typeDelay.cancel();
                typeDelay = new Timer();
                typeDelay.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                //run search if search field is filled
                                if(searchField.getText().length() > 0) {
                                    search(searchField.getText().toString());
                                }
                            }
                        }, DELAY
                );
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    //populate listview with users who liked/followed/following
    private void getList(String url){
        final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                loader.setVisibility(View.INVISIBLE);
                    try {
                        //loop through json Array
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject postObj = (JSONObject) response.get(i);
                            JSONObject user = (JSONObject) postObj.get("user");
                            String id = user.getString("id");
                            String username = user.getString("username");
                            String name = user.getString("name");
                            String decodedName = URLDecoder.decode(name, "utf-8");
                            JSONObject url = (JSONObject) user.get("avatar");
                            String avatar = url.getString("url");
                            UserItem users = new UserItem(username, decodedName, id, avatar);
                            userAdapter.add(users);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(SearchActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                NetworkResponse networkResponse = error.networkResponse;
                if(networkResponse == null){
                    loader.setVisibility(View.GONE);
                    Toast.makeText(UserList.this, "Unable to update feed. Check internet connection", Toast.LENGTH_SHORT).show();
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
        RequestQueue requestQueue = RequestSingleton.getInstance(UserList.this.getApplicationContext()).getRequestQueue();
        RequestSingleton.getInstance(UserList.this).addToRequestQueue(jsonObjectRequest);
    }

    private void search(String searchQuery){
        final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, SEARCH_URL + searchQuery, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                loader.setVisibility(View.INVISIBLE);
                if (response.length() == 0) {
                    empty.setVisibility(View.VISIBLE);
                } else {
                    try {
                        //loop through json Array
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject postObj = (JSONObject) response.get(i);
                            JSONObject user = (JSONObject) postObj.get("user");
                            String id = user.getString("id");
                            String username = user.getString("username");
                            String name = user.getString("name");
                            String decodedName = URLDecoder.decode(name, "utf-8");
                            JSONObject url = (JSONObject) user.get("avatar");
                            String avatar = url.getString("url");
                            UserItem users = new UserItem(username, decodedName, id, avatar);
                            userAdapter.add(users);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(SearchActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                NetworkResponse networkResponse = error.networkResponse;
                if(networkResponse == null){
                    loader.setVisibility(View.GONE);
                    Toast.makeText(UserList.this, "Unable to update feed. Check internet connection", Toast.LENGTH_SHORT).show();
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
        RequestQueue requestQueue = RequestSingleton.getInstance(UserList.this.getApplicationContext()).getRequestQueue();
        RequestSingleton.getInstance(UserList.this).addToRequestQueue(jsonObjectRequest);
    }
}
