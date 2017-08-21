package com.flipbook.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import static com.flipbook.app.R.id.email;
import static com.flipbook.app.R.id.username;

/**
 * Created by Hayden on 2017-02-27.
 */

public class RegisterActivity extends Activity {

//    private final static String URL = "https://railsphotoapp.herokuapp.com//api/v1/registrations.json";
    private static final String CHECK_USERNAME_AVAILABILITY = "https://railsphotoapp.herokuapp.com//api/v1/username/";
    private static final String CHECK_EMAIL_AVAILABILITY = "https://railsphotoapp.herokuapp.com//api/v1/email/";

    private EditText email, username, password, confirmPassword;
    private Button registerButton;
    private boolean validEmail, validUsername, validPassword, validConfirmPassword;
    private Drawable x, checkmark;

//    JSONObject data = new JSONObject();
//    JSONObject user = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

//        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
//        final SharedPreferences.Editor editor = prefs.edit();

        email = (EditText) findViewById(R.id.email);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        confirmPassword = (EditText) findViewById(R.id.confirmPassword);

        registerButton = (Button) findViewById(R.id.register);

        username.addTextChangedListener(new TextWatcher() {
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
                if(username.getText().toString().length() > 0) {
                    final int cursorPosition = username.getSelectionStart();
                    username.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                    if (username.getText().toString().contains(" ")) {
                        username.setText(username.getText().toString().replace(" ", "_"));
                        username.setSelection(cursorPosition);
                    }

                    if (!username.getText().toString().matches("^([A-Za-z0-9_\\.](?:(?:[A-Za-z0-9_\\.]|(?:\\.(?!\\.))){0,18}(?:[A-Za-z0-9_\\.]))?)$")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //remove last letter in string
                                username.getText().delete(username.length() - 1, username.length());
                                //set selection back to original position after removing illegal char
                                username.setSelection(username.length());
                                Toast toast = Toast.makeText(RegisterActivity.this, "Username can only use letters, numbers, periods and underscores", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP, 0, 10);
                                toast.show();
                            }
                        });
                    }
                }
                typeDelay.cancel();
                typeDelay = new Timer();
                typeDelay.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                //check if edited usernamw is not empty or not the same as current username
                                if(!username.getText().toString().equals(username)) {
                                    checkAvailability(email.getText().toString(), CHECK_USERNAME_AVAILABILITY);
                                } else {
                                    validUsername = true;
                                }
                            }
                        }, DELAY
                );
            }
        });

        email.addTextChangedListener(new TextWatcher() {
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
                email.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0, 0);
                typeDelay.cancel();
                typeDelay = new Timer();
                typeDelay.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                //check if edited email is not empty or not the same as current email
                                if(!email.getText().toString().equals(email)) {
                                    checkAvailability(email.getText().toString(), CHECK_EMAIL_AVAILABILITY);
                                } else {
                                    validEmail = true;
                                }
                            }
                        }, DELAY
                );
            }
        });

        password.addTextChangedListener(new TextWatcher() {

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
                password.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0, 0);
                typeDelay.cancel();
                typeDelay = new Timer();
                typeDelay.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                //check if edited email is not empty or not the same as current email
                                if(password.getText().length() > 0) {
                                    validatePassword(password.getText().toString());
                                } else {
                                    validPassword = true;
                                }
                            }
                        }, DELAY
                );
            }
        });

        confirmPassword.addTextChangedListener(new TextWatcher() {
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
                confirmPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0, 0);
                typeDelay.cancel();
                typeDelay = new Timer();
                typeDelay.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                //check if edited email is not empty or not the same as current email
                                if(confirmPassword.getText().length() > 0) {
                                    validateConfirmPassword(password.getText().toString(), confirmPassword.getText().toString());
                                } else {
                                    validConfirmPassword = true;
                                }
                            }
                        }, DELAY
                );
            }
        });

//        registerButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    data.put("email", email.getText().toString());
//                    data.put("username", username.getText().toString());
//                    data.put("password", password.getText().toString());
//                    data.put("password_confirmation", confirmPassword.getText().toString());
//                    user.put("user", data);
//                } catch (JSONException e){
//                    e.printStackTrace();
//                }
//                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, user, new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            System.out.println(response.getString("status"));
//                            if(response.getString("status").equals("created")){
//                                editor.putString("auth_token", response.getString("auth_token"));
//                                editor.putString("email", response.getString("email"));
//                                editor.apply();
//                                registerButton.setClickable(false);
//                                Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
//                                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(intent);
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(RegisterActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//                RequestQueue requestQueue = RequestSingleton.getInstance(RegisterActivity.this.getApplicationContext()).getRequestQueue();
//                RequestSingleton.getInstance(RegisterActivity.this).addToRequestQueue(jsonObjectRequest);
//            }
//        });
    }

    private void validatePassword(final String pass){
        //scale image to the height of the editText field
        //scale x drawable
        final Drawable x = getApplicationContext().getResources().getDrawable(R.drawable.x);
        x.setBounds(0, 0, x.getIntrinsicWidth() * username.getMeasuredHeight() / x.getIntrinsicHeight() / 2, username.getMeasuredHeight() / 2);
        //scale checkmark drawable
        final Drawable checkmark = getApplicationContext().getResources().getDrawable(R.drawable.checkmark);
        //username and email are the same dimensions
        checkmark.setBounds(0, 0, checkmark.getIntrinsicWidth() * username.getMeasuredHeight() / checkmark.getIntrinsicHeight() / 2, username.getMeasuredHeight() / 2);
        if (pass.length() < 6) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    password.setCompoundDrawablesRelative(null, null, x, null);
                    Toast toast = Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 10);
                    toast.show();
                    validPassword = false;
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    password.setCompoundDrawablesRelative(null, null, checkmark, null);
                    validPassword = true;
                }
            });
        }
    }

    private void validateConfirmPassword(final String pass, final String confirmPass){
        //scale image to the height of the editText field
        //scale x drawable
        final Drawable x = getApplicationContext().getResources().getDrawable(R.drawable.x);
        x.setBounds(0, 0, x.getIntrinsicWidth() * username.getMeasuredHeight() / x.getIntrinsicHeight() / 2, username.getMeasuredHeight() / 2);
        //scale checkmark drawable
        final Drawable checkmark = getApplicationContext().getResources().getDrawable(R.drawable.checkmark);
        //username and email are the same dimensions
        checkmark.setBounds(0, 0, checkmark.getIntrinsicWidth() * username.getMeasuredHeight() / checkmark.getIntrinsicHeight() / 2, username.getMeasuredHeight() / 2);
        if(!confirmPass.equals(pass) && confirmPass.length() > 0){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    confirmPassword.setCompoundDrawablesRelative(null, null, x, null);
                    Toast toast = Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 10);
                    toast.show();
                    validConfirmPassword = false;
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    password.setCompoundDrawablesRelative(null, null, checkmark, null);
                    validConfirmPassword = true;
                }
            });
        }
    }

    private void checkAvailability(final String string, final String url) {
        //validate email format
        if (url == CHECK_EMAIL_AVAILABILITY && isValidEmail(string) == false) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    email.setCompoundDrawablesRelative(null, null, x, null);
                    Toast toast = Toast.makeText(RegisterActivity.this, "Please enter valid email", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 10);
                    toast.show();
                    validEmail = false;
                }
            });
            //check if username is valid
        } else if (url == CHECK_USERNAME_AVAILABILITY && isValidUsername(string) == false && string.length() > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //if username starts with period
                    if(string.charAt(0) == '.'){
                        username.setCompoundDrawablesRelative(null, null, x, null);
                        Toast toast = Toast.makeText(RegisterActivity.this, "Username can't start with a period", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 10);
                        toast.show();
                        validUsername = false;
                    }
                    //if username ends with period
                    if(string.charAt(string.length() - 1) == '.'){
                        username.setCompoundDrawablesRelative(null, null, x, null);
                        Toast toast = Toast.makeText(RegisterActivity.this, "Username can't end with a period", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 10);
                        toast.show();
                        validUsername = false;
                    }
                    //if username contains illegal substring ("..")
                    if(string.contains("..")){
                        username.setCompoundDrawablesRelative(null, null, x, null);
                        Toast toast = Toast.makeText(RegisterActivity.this, "Username can't have more than two periods in a row", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 10);
                        toast.show();
                        validUsername = false;
                    }
                }
            });
        }
        //check if email or username is empty
        else if(string.length() == 0 && url == CHECK_USERNAME_AVAILABILITY){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    username.setCompoundDrawablesRelative(null, null, x, null);
                    Toast toast = Toast.makeText(RegisterActivity.this, "Please enter a username", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 10);
                    toast.show();
                    validUsername = false;
                }
            });
        } else{
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url + string, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    System.out.println(response);
                    try {
                        if (response.getBoolean("status") == true) {
                            if (url == CHECK_USERNAME_AVAILABILITY) {
                                username.setCompoundDrawablesRelative(null, null, x, null);
                                Toast toast = Toast.makeText(RegisterActivity.this, "This username is already taken", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP, 0, 10);
                                toast.show();
                                validUsername = false;
                            } else {
                                email.setCompoundDrawablesRelative(null, null, x, null);
                                Toast toast =  Toast.makeText(RegisterActivity.this, "This email is already taken", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP, 0, 10);
                                toast.show();
                                validEmail = false;
                            }
                        }
                        //valid email or username
                        else {
                            if (url == CHECK_USERNAME_AVAILABILITY) {
                                username.setCompoundDrawablesRelative(null, null, checkmark, null);
                                validUsername = true;
                            } else {
                                email.setCompoundDrawablesRelative(null, null, checkmark, null);
                                validEmail = true;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(RegisterActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse == null) {
                        Toast.makeText(RegisterActivity.this, "Can't edit profile. Check internet connection", Toast.LENGTH_SHORT).show();
                    }
                    if (networkResponse != null && (networkResponse.statusCode == HttpsURLConnection.HTTP_UNAUTHORIZED ||
                            networkResponse.statusCode == HttpsURLConnection.HTTP_CLIENT_TIMEOUT)) {
                    }
                }
            });
            RequestQueue requestQueue = RequestSingleton.getInstance(RegisterActivity.this.getApplicationContext()).getRequestQueue();
            RequestSingleton.getInstance(RegisterActivity.this).addToRequestQueue(jsonObjectRequest);
        }
    }

    //verify email method
    private final static boolean isValidEmail(CharSequence target) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    //verify username method
    private final static boolean isValidUsername(String target) {
        return target.matches("^([A-Za-z0-9_](?:(?:[A-Za-z0-9_]|(?:\\.(?!\\.))){0,18}(?:[A-Za-z0-9_]))?)$");
    }
}
