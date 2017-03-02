package com.flipbook.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Hayden on 2017-02-27.
 */

public class RegisterActivity extends Activity {

    private final static String URL = "https://aqueous-river-91475.herokuapp.com/api/v1/registrations.json";

    private EditText email, username, name, password, confirmPassword;
    private Button registerButton;

    JSONObject data = new JSONObject();
    JSONObject user = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        email = (EditText) findViewById(R.id.email);
        username = (EditText) findViewById(R.id.username);
        name = (EditText) findViewById(R.id.name);
        password = (EditText) findViewById(R.id.password);
        confirmPassword = (EditText) findViewById(R.id.confirmPassword);

        registerButton = (Button) findViewById(R.id.register);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    data.put("email", email.getText().toString());
                    data.put("username", username.getText().toString());
                    data.put("name", name.getText().toString());
                    data.put("password", password.getText().toString());
                    data.put("password_confirmation", confirmPassword.getText().toString());
                    user.put("user", data);
                } catch (JSONException e){
                    e.printStackTrace();
                }
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, user, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            System.out.println(response.getString("status"));
                            if(response.getString("status").equals("created")){
                                editor.putString("auth_token", response.getString("auth_token"));
                                editor.putString("email", response.getString("email"));
                                editor.apply();
                                registerButton.setClickable(false);
                                Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(RegisterActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
                RequestQueue requestQueue = RequestSingleton.getInstance(RegisterActivity.this.getApplicationContext()).getRequestQueue();
                RequestSingleton.getInstance(RegisterActivity.this).addToRequestQueue(jsonObjectRequest);
            }
        });
    }
}
