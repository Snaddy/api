package com.flipbook.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import static com.flipbook.app.WelcomeActivity.getEmail;

/**
 * Created by Hayden on 2017-06-30.
 */

public class EditProfileActivity extends Activity {

    private static final String CHECK_USERNAME_AVAILABILITY = "https://railsphotoapp.herokuapp.com//api/v1/username/";
    private static final String CHECK_EMAIL_AVAILABILITY = "https://railsphotoapp.herokuapp.com//api/v1/email/";
    private static final String EDIT_USER_URL = "https://railsphotoapp.herokuapp.com//api/v1/update.json";

    private String getEmail, getToken, username, name, email, bio;
    private boolean validEdit;
    private ImageView profilePic;
    private EditText editUsername, editName, editEmail, editBio;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edituser);

        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        getEmail = prefs.getString("email", "");
        getToken = prefs.getString("auth_token", "");

        Bundle bundle = getIntent().getExtras();
        username = bundle.getString("username");
        name = bundle.getString("name");
        email = bundle.getString("email");
        bio = bundle.getString("bio");

        editUsername = (EditText) findViewById(R.id.editUsername);
        editName = (EditText) findViewById(R.id.editName);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editBio = (EditText) findViewById(R.id.editBio);

        editUsername.setText(username);
        editName.setText(name);
        editEmail.setText(email);
        editBio.setText(bio);

        editUsername.addTextChangedListener(new TextWatcher() {
            //setup timer to run api request 0.5 secs after user is done typing
            private Timer typeDelay = new Timer();
            private final int DELAY = 500;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                editUsername.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0, 0);
                typeDelay.cancel();
                typeDelay = new Timer();
                typeDelay.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                //check if edited email is not empty or not the same as current email
                                if(editUsername.length() > 0 && !editUsername.getText().toString().equals(username)) {
                                    checkAvailability(editUsername.getText().toString(), CHECK_USERNAME_AVAILABILITY);
                                } else {
                                    validEdit = false;
                                }
                            }
                        }, DELAY
                );
            }
        });

        editEmail.addTextChangedListener(new TextWatcher() {
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
                editEmail.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0, 0);
                typeDelay.cancel();
                typeDelay = new Timer();
                typeDelay.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                //check if edited email is not empty or not the same as current email
                                if(editEmail.length() > 0 && !editEmail.getText().equals(email)) {
                                    if(isValidEmail(editEmail.getText().toString()) == true) {
                                        checkAvailability(editEmail.getText().toString(), CHECK_EMAIL_AVAILABILITY);
                                    } else {
                                        validEdit = false;
                                        editEmail.setCompoundDrawables(null,null,null,null);
                                        Toast.makeText(EditProfileActivity.this, "Please enter a valid email.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    validEdit = false;
                                }
                            }
                        }, DELAY
                );
            }
        });
    }


    private void checkAvailability(final String string, final String url) {
        //scale image to the height of the editText field
        //scale x drawable
        final Drawable x = getApplicationContext().getResources().getDrawable(R.drawable.x);
        x.setBounds(0, 0, x.getIntrinsicWidth() * editUsername.getMeasuredHeight() / x.getIntrinsicHeight() / 2, editUsername.getMeasuredHeight() / 2);
        //scale checkmark drawable
        final Drawable checkmark = getApplicationContext().getResources().getDrawable(R.drawable.checkmark);
        //editUsername and editEmail are the same dimensions
        checkmark.setBounds(0, 0, checkmark.getIntrinsicWidth() * editUsername.getMeasuredHeight() / checkmark.getIntrinsicHeight() / 2, editUsername.getMeasuredHeight() / 2);
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url + string, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getBoolean("status") == true) {
                            validEdit = false;
                            if (url == CHECK_USERNAME_AVAILABILITY) {
                                editUsername.setCompoundDrawablesRelative(null, null, x, null);
                                Toast.makeText(EditProfileActivity.this, "This username is already taken :(", Toast.LENGTH_SHORT).show();
                            } else {
                                editEmail.setCompoundDrawablesRelative(null, null, x, null);
                                Toast.makeText(EditProfileActivity.this, "This email is already taken :(", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            validEdit = true;
                            if (url == CHECK_USERNAME_AVAILABILITY) {
                                editUsername.setCompoundDrawablesRelative(null, null, checkmark, null);
                            } else {
                                editEmail.setCompoundDrawablesRelative(null, null, checkmark, null);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(EditProfileActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse == null) {
                        Toast.makeText(EditProfileActivity.this, "Can't edit profile. Check internet connection", Toast.LENGTH_SHORT).show();
                    }
                    if (networkResponse != null && (networkResponse.statusCode == HttpsURLConnection.HTTP_UNAUTHORIZED ||
                            networkResponse.statusCode == HttpsURLConnection.HTTP_CLIENT_TIMEOUT)) {
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
            RequestQueue requestQueue = RequestSingleton.getInstance(EditProfileActivity.this.getApplicationContext()).getRequestQueue();
            RequestSingleton.getInstance(EditProfileActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    //verify email method
    public final static boolean isValidEmail(CharSequence target) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
