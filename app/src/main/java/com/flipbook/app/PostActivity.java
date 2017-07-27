package com.flipbook.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hayden on 2017-03-19.
 */

public class PostActivity extends AppCompatActivity{

    private final static String CREATE_POSTS_URL = "https://railsphotoapp.herokuapp.com//api/v1/posts.json";
    private ArrayList<Bitmap> processedArray;
    private ImageView imageView;
    private ImageButton back;
    private EditText caption;
    private Button post;
    private AnimationDrawable animation;
    private String getEmail, getToken, encodedCaption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        getEmail = prefs.getString("email", "");
        getToken = prefs.getString("auth_token", "");

        //get processed array from processing activity
        processedArray = ProcessingActivity.processedImages;

        imageView = (ImageView) findViewById(R.id.imageView);
        back = (ImageButton) findViewById(R.id.back);
        post = (Button) findViewById(R.id.post);
        caption = (EditText) findViewById(R.id.caption);

        animation = new AnimationDrawable();
        for(int i = 0; i < processedArray.size(); i ++){
            Drawable d = new BitmapDrawable(getResources(), processedArray.get(i));
            animation.addFrame(d, ProcessingActivity.speedInt);
        }
        imageView.setImageDrawable(animation);
        animation.setOneShot(false);
        animation.start();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProcessingActivity.class));
                ProcessingActivity.processedImages.clear();
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
                InputMethodManager inputManager = (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
    }

    @Override
    public void onBackPressed() {
        ProcessingActivity.processedImages.clear();
        finish();
    }

    private void upload(){
        final ProgressDialog pd = new ProgressDialog(this, R.style.dialogStyle);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("Uploading, please wait...");
        pd.show();
        try {
            encodedCaption = URLEncoder.encode(caption.getText().toString(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        PostMultipartRequest multipartRequest = new PostMultipartRequest(Request.Method.POST, CREATE_POSTS_URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                pd.dismiss();
                startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                error.printStackTrace();
                pd.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("speed", String.valueOf(ProcessingActivity.speedBarProg));
                params.put("caption", encodedCaption);
                return params;
            }

            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> headers = new HashMap<>();
                headers.put("X-User-Token", getToken);
                headers.put("X-User-Email", getEmail);
                return headers;
            }

            @Override
            protected Map<String, ArrayList<DataPart>> getByteData() {
                Map<String, ArrayList<DataPart>> dataParams = new HashMap<>();
                ArrayList<DataPart> arrayList = new ArrayList<>();
                for (int i = 0; i < processedArray.size(); i++) {
                    arrayList.add(new DataPart("image" + i + ".jpg", getFileDataFromDrawable(getBaseContext(), processedArray.get(i)), "image/jpeg"));
                }
                dataParams.put("images[]", arrayList);
                return dataParams;
            }
        };
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = RequestSingleton.getInstance(PostActivity.this.getApplicationContext()).getRequestQueue();
        RequestSingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);
    }

    public byte[] getFileDataFromDrawable(Context context, Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
