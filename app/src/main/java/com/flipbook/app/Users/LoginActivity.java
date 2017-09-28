package com.flipbook.app.Users;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.flipbook.app.R;
import com.flipbook.app.Registration.RegisterActivity;
import com.flipbook.app.Posts.RequestSingleton;
import com.flipbook.app.Welcome.WelcomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private final static String URL = "https://railsphotoapp.herokuapp.com//api/v1/sessions.json";
    private Button loginButton, createAccount;
    private EditText email;
    private EditText password;
    private TextView title, forgotPassword;
    private Typeface font;

    JSONObject data = new JSONObject();
    JSONObject user = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        loginButton = (Button) findViewById(R.id.login);
        createAccount = (Button) findViewById(R.id.createAccount);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        title = (TextView) findViewById(R.id.title);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);

        font = Typeface.createFromAsset(this.getAssets(), "fonts/default.otf");
        title.setTypeface(font);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    data.put("email", email.getText().toString());
                    data.put("password", password.getText().toString());
                    user.put("user", data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, user ,new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response);
                        try {
                            if(response.getString("status").equals("success")){
                                loginButton.setClickable(false);
                                editor.putString("auth_token", response.getString("auth_token"));
                                editor.putString("email", response.getString("email"));
                                editor.putString("username", response.getString("user_name"));
                                editor.apply();
                                Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
                RequestQueue requestQueue = RequestSingleton.getInstance(LoginActivity.this.getApplicationContext()).getRequestQueue();
                RequestSingleton.getInstance(LoginActivity.this).addToRequestQueue(jsonObjectRequest);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RecoverAccount.class);
                startActivity(intent);
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
