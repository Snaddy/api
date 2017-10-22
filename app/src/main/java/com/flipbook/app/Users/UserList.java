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
import com.android.volley.toolbox.JsonObjectRequest;
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

    private ImageButton back;
    private EditText searchField;
    private ListView results;
    private UserAdapter userAdapter;
    private ProgressBar loader;
    private SharedPreferences prefs;
    private String getEmail, getToken, url, titleText;
    private TextView title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        getEmail = prefs.getString("email", "");
        getToken = prefs.getString("auth_token", "");

        searchField = (EditText) findViewById(R.id.search_field);
        title = (TextView) findViewById(R.id.title);
        back = (ImageButton) findViewById(R.id.back);
        userAdapter = new UserAdapter(this, R.layout.user_item);
        loader = (ProgressBar) findViewById(R.id.loader);
        loader.setVisibility(View.VISIBLE);
        results = (ListView) findViewById(R.id.results);
        results.setAdapter(userAdapter);

        Bundle bundle = getIntent().getExtras();
        url = bundle.getString("url");
        titleText = bundle.getString("title");
        title.setText(titleText);

        getList(url);

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                userAdapter.getFilter().filter(s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

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
    private void getList(final String url){
        final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                loader.setVisibility(View.INVISIBLE);
                System.out.println("list: " + response);
                    try {
                        //loop through json Array
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject userObj = (JSONObject) response.get(i);
                            if(url.contains("https://railsphotoapp.herokuapp.com//api/v1/likes/")) {
                                JSONObject userJson = userObj.getJSONObject("like");
                                JSONObject user = userJson.getJSONObject("user");
                                String id = user.getString("id");
                                String username = user.getString("username");
                                String name = user.getString("name");
                                String decodedName = URLDecoder.decode(name, "utf-8");
                                JSONObject url = (JSONObject) user.get("avatar");
                                String avatar = url.getString("url");
                                UserItem users = new UserItem(username, decodedName, id, avatar);
                                userAdapter.add(users);
                            } else {
                                JSONObject user = userObj.getJSONObject("user");
                                String id = user.getString("id");
                                String username = user.getString("username");
                                String name = user.getString("name");
                                String decodedName = URLDecoder.decode(name, "utf-8");
                                JSONObject url = (JSONObject) user.get("avatar");
                                String avatar = url.getString("url");
                                UserItem users = new UserItem(username, decodedName, id, avatar);
                                userAdapter.add(users);
                            }
                        }
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loader.setVisibility(View.GONE);
                System.out.println("list: " + error);
                Toast.makeText(UserList.this, "Unable to get list. Check internet connection", Toast.LENGTH_SHORT).show();
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
