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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hayden on 2017-03-19.
 */

public class PostActivity extends AppCompatActivity{

    private final static String CREATE_POSTS_URL = "https://aqueous-river-91475.herokuapp.com/api/v1/posts.json";
    private ArrayList<Drawable> processedArray;
    private ImageView imageView;
    private ImageButton back;
    private EditText caption;
    private Button post;
    private AnimationDrawable animation;
    private String getEmail, getToken;
    private ProgressDialog progressDialog;

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
            Drawable d = processedArray.get(i);
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
                startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        ProcessingActivity.processedImages.clear();
        finish();
    }

    private void upload(){
        progressDialog = new ProgressDialog(PostActivity.this);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, CREATE_POSTS_URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                try {
                    JSONObject result = new JSONObject(resultResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("caption", caption.getText().toString());
                params.put("speed", ProcessingActivity.speedInt + "");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-User-Token", getToken);
                headers.put("X-User-Email", getEmail);
                System.out.println(headers.toString());
                return headers;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                for(int i = 0; i < processedArray.size(); i++) {
                    params.put("images", new DataPart("file_" + processedArray.get(i) + ".jpg", getFileDataFromDrawable(getBaseContext(), processedArray.get(i)), "image/jpeg"));
                }
                return params;
            }
        };
        RequestQueue requestQueue = RequestSingleton.getInstance(PostActivity.this.getApplicationContext()).getRequestQueue();
        RequestSingleton.getInstance(PostActivity.this).addToRequestQueue(multipartRequest);
    }

    public static byte[] getFileDataFromDrawable(Context context, Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

}
