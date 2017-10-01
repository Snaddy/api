package com.flipbook.app.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.flipbook.app.Posts.RequestSingleton;
import com.flipbook.app.R;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Hayden on 2017-09-29.
 */

public class ChangePasswordActivity extends AppCompatActivity {

    private final static String NEW_PASS = "https://railsphotoapp.herokuapp.com//api/v1/edit/password.json";

    private ImageButton back;
    private Button save;
    private EditText newPass, confirmNewPass, currentPass;
    private String getEmail, getToken;
    private boolean validPassword, validConfirmPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        getEmail = prefs.getString("email", "");
        getToken = prefs.getString("auth_token", "");

        back = (ImageButton) findViewById(R.id.back);
        save = (Button) findViewById(R.id.save);

        newPass = (EditText) findViewById(R.id.new_password);
        confirmNewPass = (EditText) findViewById(R.id.confirm_password);
        currentPass = (EditText) findViewById(R.id.confirm_password);

        newPass.addTextChangedListener(new TextWatcher() {
            //setup timer to run api request 0.5 secs after user is done typing
            private Timer typeDelay = new Timer();
            private final int DELAY = 1000;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                validPassword = false;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validPassword = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
                validPassword = false;
                newPass.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);
                typeDelay.cancel();
                typeDelay = new Timer();
                typeDelay.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                if(newPass.getText().length() > 0) {
                                    validatePassword(newPass.getText().toString(), newPass);
                                } else {
                                    validPassword = false;
                                }
                            }
                        }, DELAY
                );
            }
        });

        confirmNewPass.addTextChangedListener(new TextWatcher() {
            //setup timer to run api request 0.5 secs after user is done typing
            private Timer typeDelay = new Timer();
            private final int DELAY = 1000;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                validConfirmPassword = false;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validConfirmPassword = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
                validConfirmPassword = false;
                confirmNewPass.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);
                typeDelay.cancel();
                typeDelay = new Timer();
                typeDelay.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                if(confirmNewPass.getText().length() > 0) {
                                    validatePassword(confirmNewPass.getText().toString(), confirmNewPass);
                                } else {
                                    validConfirmPassword = false;
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

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validConfirmPassword && validPassword) {
                    newPassword(newPass.getText().toString(), confirmNewPass.getText().toString(), currentPass.getText().toString());
                }
            }
        });
    }

    public void newPassword(final String newPass, final String confirmPass, final String currentPass) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, NEW_PASS, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equals("success")) {
                        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
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
                //Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("new_password", newPass);
                params.put("confirm_password", confirmPass);
                params.put("current_password", currentPass);
                return params;
            }

            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> headers = new HashMap<>();
                headers.put("X-User-Token", getToken);
                headers.put("X-User-Email", getEmail);
                return headers;
            }};
        RequestQueue requestQueue = RequestSingleton.getInstance(ChangePasswordActivity.this.getApplicationContext()).getRequestQueue();
        RequestSingleton.getInstance(ChangePasswordActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private void validatePassword(final String pass, final EditText editPass){
        final Drawable x = getApplicationContext().getResources().getDrawable(R.drawable.x);
        final Drawable checkmark = getApplicationContext().getResources().getDrawable(R.drawable.checkmark);
        //scale image to the height of the editText field
        //scale x drawable
        x.setBounds(0, 0, x.getIntrinsicWidth() * newPass.getMeasuredHeight() / x.getIntrinsicHeight() / 2, newPass.getMeasuredHeight() / 2);
        //scale checkmark drawable
        checkmark.setBounds(0, 0, checkmark.getIntrinsicWidth() * newPass.getMeasuredHeight() / checkmark.getIntrinsicHeight() / 2, newPass.getMeasuredHeight() / 2);
        if (pass.length() < 6) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    editPass.setCompoundDrawablesRelative(null, null, x, null);
                    Toast toast = Toast.makeText(ChangePasswordActivity.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 10);
                    toast.show();
                    validPassword = false;
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    editPass.setCompoundDrawablesRelative(null, null, checkmark, null);
                    validPassword = true;
                }
            });
        }
    }
}
