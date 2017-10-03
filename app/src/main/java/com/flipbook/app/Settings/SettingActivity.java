package com.flipbook.app.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.flipbook.app.Posts.RequestSingleton;
import com.flipbook.app.R;
import com.flipbook.app.Users.EditProfileActivity;
import com.flipbook.app.Users.LoginActivity;
import com.flipbook.app.Users.UserList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Hayden on 2017-09-29.
 */

public class SettingActivity extends AppCompatActivity{

    private final static String BLOCKED_USERS = "https://railsphotoapp.herokuapp.com//api/v1/users/blocked.json";
    private final static String REPORT_URL = "https://railsphotoapp.herokuapp.com//api/v1/report.json";
    private final static String SUGGESTION_URL = "https://railsphotoapp.herokuapp.com//api/v1/suggestion.json";
    private final static String LOGOUT_URL = "https://railsphotoapp.herokuapp.com//api/v1/sessions.json";

    private Button editProfile, changePassword, blockedUsers, notifications,
            reportProblem, suggestion, privacyPolicy, tos, licenses, logout;
    private ImageButton back;
    private SharedPreferences prefs;
    private String getEmail, getToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //prefs
        prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        getEmail = prefs.getString("email", "");
        getToken = prefs.getString("auth_token", "");

        editProfile = (Button) findViewById(R.id.edit_profile);
        changePassword = (Button) findViewById(R.id.change_password);
        blockedUsers = (Button) findViewById(R.id.blockedUsers);
        notifications = (Button) findViewById(R.id.notifications);
        reportProblem = (Button) findViewById(R.id.report_problem);
        suggestion = (Button) findViewById(R.id.suggestion);
        privacyPolicy = (Button) findViewById(R.id.privacy_policy);
        logout = (Button) findViewById(R.id.logout);
        back = (ImageButton) findViewById(R.id.back);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        blockedUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserList.class);
                intent.putExtra("url", BLOCKED_USERS);
                startActivity(intent);
            }
        });

        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NotificationSettingsActivity.class);
                startActivity(intent);
            }
        });

        reportProblem.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LayoutInflater inflater = SettingActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.content_dialog, null);
                builder.setView(dialogView);
                TextView title = (TextView) dialogView.findViewById(R.id.title);
                final TextView content = (TextView) dialogView.findViewById(R.id.message);
                Button ok = (Button) dialogView.findViewById(R.id.okButton);
                Button cancel = (Button) dialogView.findViewById(R.id.cancelButton);
                title.setText("Tell us about the problem.");
                final AlertDialog alertDialog = builder.create();
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        send(content.getText().toString(), REPORT_URL);
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();
            }
        });

        suggestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = SettingActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.content_dialog, null);
                builder.setView(dialogView);
                TextView title = (TextView) dialogView.findViewById(R.id.title);
                final TextView content = (TextView) dialogView.findViewById(R.id.message);
                Button ok = (Button) dialogView.findViewById(R.id.okButton);
                Button cancel = (Button) dialogView.findViewById(R.id.cancelButton);
                title.setText("Tell us about your suggestion.");
                final AlertDialog alertDialog = builder.create();
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        send(content.getText().toString(), SUGGESTION_URL);
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });

        //TODO privacy policy, terms of use, licenses (Easy...kinda)

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = SettingActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.logout_dialog, null);
                builder.setView(dialogView);
                TextView title = (TextView) dialogView.findViewById(R.id.title);
                Button ok = (Button) dialogView.findViewById(R.id.okButton);
                Button cancel = (Button) dialogView.findViewById(R.id.cancelButton);
                final AlertDialog alertDialog = builder.create();
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        logout(editor);
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();
            }
        });

        //finish activty on back button click
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void send(final String content, String url){
        String encodedContent = "";
        try {
            encodedContent = URLEncoder.encode(content, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final String finalContent = encodedContent;

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
                try {
                    if(response.getString("status").equals("sent")){

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(ShowActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse == null) {
                    Toast.makeText(SettingActivity.this, "Unable to send message. Check internet connection", Toast.LENGTH_SHORT).show();
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
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("content", finalContent);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-User-Token", getToken);
                headers.put("X-User-Email", getEmail);
                return headers;
            }
        };
        RequestQueue requestQueue = RequestSingleton.getInstance(SettingActivity.this.getApplicationContext()).getRequestQueue();
        RequestSingleton.getInstance(SettingActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private void logout(final SharedPreferences.Editor editor){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, LOGOUT_URL, null ,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
                try {
                    if(response.getBoolean("status")){
                        editor.clear();
                        editor.commit();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
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
                //Toast.makeText(SettingActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = RequestSingleton.getInstance(SettingActivity.this.getApplicationContext()).getRequestQueue();
        RequestSingleton.getInstance(SettingActivity.this).addToRequestQueue(jsonObjectRequest);
    }
}
