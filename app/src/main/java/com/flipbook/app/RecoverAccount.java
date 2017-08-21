package com.flipbook.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Hayden on 2017-08-19.
 */

public class RecoverAccount extends AppCompatActivity {

    private final static String URL = "https://railsphotoapp.herokuapp.com//api/v1/account/reset_password/";
    private Button send, login;
    private EditText email;
    private TextView title, info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_account);

        send = (Button) findViewById(R.id.send);
        login = (Button) findViewById(R.id.login);
        email = (EditText) findViewById(R.id.email);
        title = (TextView) findViewById(R.id.title);
        info = (TextView) findViewById(R.id.info);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send.setText("sending...");
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL + email.getText().toString(), null,new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        send.setText("resend");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(RecoverAccount.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
                RequestQueue requestQueue = RequestSingleton.getInstance(RecoverAccount.this.getApplicationContext()).getRequestQueue();
                RequestSingleton.getInstance(RecoverAccount.this).addToRequestQueue(jsonObjectRequest);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

}
