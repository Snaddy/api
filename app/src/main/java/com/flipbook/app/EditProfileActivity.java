package com.flipbook.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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

/**
 * Created by Hayden on 2017-06-30.
 */

public class EditProfileActivity extends Activity {

    private static final String CHECK_USERNAME_AVAILABILITY = "https://railsphotoapp.herokuapp.com//api/v1/username/";
    private static final String CHECK_EMAIL_AVAILABILITY = "https://railsphotoapp.herokuapp.com//api/v1/email/";
    private static final String EDIT_USER_URL = "https://railsphotoapp.herokuapp.com//api/v1/update.json";

    private String getEmail, getToken, username, name, email, bio;
    private boolean validUsername, validEmail;
    private ImageView profilePic;
    private EditText editUsername, editName, editEmail, editBio;
    private Button saveProfile;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edituser);

        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        getEmail = prefs.getString("email", "");
        getToken = prefs.getString("auth_token", "");

        validUsername = true;
        validEmail = true;

        Bundle bundle = getIntent().getExtras();
        username = bundle.getString("username");
        name = bundle.getString("name");
        email = bundle.getString("email");
        bio = bundle.getString("bio");

        editUsername = (EditText) findViewById(R.id.editUsername);
        editName = (EditText) findViewById(R.id.editName);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editBio = (EditText) findViewById(R.id.editBio);

        //save profile button
        saveProfile = (Button) findViewById(R.id.saveButton);

        editUsername.setText(username);
        editUsername.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editName.setText(name);
        editEmail.setText(email);
        editBio.setText(bio);

        //dialog builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
                if(editUsername.getText().toString().length() > 0) {
                    final int cursorPosition = editUsername.getSelectionStart();
                    editUsername.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                    if (editUsername.getText().toString().contains(" ")) {
                        editUsername.setText(editUsername.getText().toString().replace(" ", "_"));
                        editUsername.setSelection(cursorPosition);
                    }

                    if (!editUsername.getText().toString().matches("^([A-Za-z0-9_\\.](?:(?:[A-Za-z0-9_\\.]|(?:\\.(?!\\.))){0,18}(?:[A-Za-z0-9_\\.]))?)$")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //remove last letter in string
                                editUsername.getText().delete(editUsername.length() - 1, editUsername.length());
                                //set selection back to original position after removing illegal char
                                editUsername.setSelection(editUsername.length());
                                Toast toast = Toast.makeText(EditProfileActivity.this, "Username can only use letters, numbers, periods and underscores", Toast.LENGTH_SHORT);
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
                                //check if edited email is not empty or not the same as current email
                                if(!editUsername.getText().toString().equals(username)) {
                                    checkAvailability(editUsername.getText().toString(), CHECK_USERNAME_AVAILABILITY);
                                } else {
                                    validUsername = true;
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
                                if(!editEmail.getText().toString().equals(email)) {
                                    checkAvailability(editEmail.getText().toString(), CHECK_EMAIL_AVAILABILITY);
                                } else {
                                    validEmail = true;
                                }
                            }
                        }, DELAY
                );
            }
        });

        //save button is clicked
        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if all fields are valid
                if(validEmail == true && validUsername == true){
                    saveProfile.setText("Saving...");
                    editProfileRequest(editUsername.getText().toString(),
                            editName.getText().toString(), editBio.getText().toString(),
                            editEmail.getText().toString(), editor);
                }
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

        //validate email format
        if (url == CHECK_EMAIL_AVAILABILITY && isValidEmail(string) == false) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    editEmail.setCompoundDrawablesRelative(null, null, x, null);
                    Toast toast = Toast.makeText(EditProfileActivity.this, "Please enter valid email", Toast.LENGTH_SHORT);
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
                        editUsername.setCompoundDrawablesRelative(null, null, x, null);
                        Toast toast = Toast.makeText(EditProfileActivity.this, "Username can't start with a period", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 10);
                        toast.show();
                        validUsername = false;
                    }
                    //if username ends with period
                    if(string.charAt(string.length() - 1) == '.'){
                        editUsername.setCompoundDrawablesRelative(null, null, x, null);
                        Toast toast = Toast.makeText(EditProfileActivity.this, "Username can't end with a period", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 10);
                        toast.show();
                        validUsername = false;
                    }
                    //if username contains illegal substring ("..")
                    if(string.contains("..")){
                        editUsername.setCompoundDrawablesRelative(null, null, x, null);
                        Toast toast = Toast.makeText(EditProfileActivity.this, "Username can't have more than two periods in a row", Toast.LENGTH_SHORT);
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
                    editUsername.setCompoundDrawablesRelative(null, null, x, null);
                    Toast toast = Toast.makeText(EditProfileActivity.this, "Please enter a username", Toast.LENGTH_SHORT);
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
                                editUsername.setCompoundDrawablesRelative(null, null, x, null);
                                Toast toast = Toast.makeText(EditProfileActivity.this, "This username is already taken :(", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP, 0, 10);
                                toast.show();
                                validUsername = false;
                            } else {
                                editEmail.setCompoundDrawablesRelative(null, null, x, null);
                                Toast toast =  Toast.makeText(EditProfileActivity.this, "This email is already taken :(", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP, 0, 10);
                                toast.show();
                                validEmail = false;
                            }
                        }
                        //valid email or username
                        else {
                            if (url == CHECK_USERNAME_AVAILABILITY) {
                                editUsername.setCompoundDrawablesRelative(null, null, checkmark, null);
                                validUsername = true;
                            } else {
                                editEmail.setCompoundDrawablesRelative(null, null, checkmark, null);
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
    }

    private void editProfileRequest(final String username, final String name, final String bio, final String email, final SharedPreferences.Editor editor){
        JSONObject data = new JSONObject();
        JSONObject user = new JSONObject();
        try {
            data.put("email", email);
            data.put("username", username);
            data.put("name", name);
            data.put("bio", bio);
            user.put("user", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, EDIT_USER_URL, user, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
                try {
                    if(response.getString("result").equals("success")){
                        ProfileActivity.profileActivity.finish();
                        editor.putString("email", email);
                        editor.apply();
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        finish();
                    } else {
                        Toast toast =  Toast.makeText(EditProfileActivity.this, "Can't save changes", Toast.LENGTH_SHORT);
                        toast.show();
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
    private final static boolean isValidEmail(CharSequence target) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    //verify username method
    private final static boolean isValidUsername(String target) {
        return target.matches("^([A-Za-z0-9_](?:(?:[A-Za-z0-9_]|(?:\\.(?!\\.))){0,18}(?:[A-Za-z0-9_]))?)$");
    }
}
