package com.flipbook.app.Posts;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.flipbook.app.R;
import com.flipbook.app.Uploads.MultipartRequest;
import com.flipbook.app.Users.EditProfileActivity;
import com.flipbook.app.Users.ProfileActivity;
import com.flipbook.app.Welcome.WelcomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.lang.Math.round;

/**
 * Created by Hayden on 2017-10-13.
 */

public class EditPostActivity extends AppCompatActivity {

    private static final String EDIT_POST = "https://railsphotoapp.herokuapp.com//api/v1/post/";

    private ArrayList<Bitmap> imageArray;
    private TextView caption;
    private ImageView imageView;
    private ImageButton back, send;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        caption = (TextView) findViewById(R.id.caption);
        imageView = (ImageView) findViewById(R.id.images);
        back = (ImageButton) findViewById(R.id.back);
        send = (ImageButton) findViewById(R.id.send);

        final Bundle bundle = getIntent().getExtras();

        caption.setText(bundle.getString("caption"));
        caption.setImeOptions(EditorInfo.IME_ACTION_DONE);
        caption.setRawInputType(InputType.TYPE_CLASS_TEXT);
        imageArray = bundle.getParcelableArrayList("images");
        postId = bundle.getString("postId");

        new AsyncTask<String, String, AnimationDrawable>() {
            final AnimationDrawable animation = new AnimationDrawable();
            final int speed = Math.round(1.0f / bundle.getInt("speed") * 1000.0f);
            @Override
            protected AnimationDrawable doInBackground(String... params) {
                for (int i = 0; i < imageArray.size(); i++) {
                    Drawable d = null;
                    try {
                        d = Glide.with(getApplicationContext()).load(imageArray.get(i)).into(1080, 1080).get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    //add images to animation
                    animation.addFrame(d, speed);
                }
                return animation;
            }

            @Override
            protected void onPostExecute(final AnimationDrawable animationDrawable) {
                imageView.setBackground(animation);
                animation.setOneShot(false);
                animation.start();
            }
        }.execute();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String encodedCaption = URLEncoder.encode(caption.getText().toString(), "utf-8");
                    update(encodedCaption);
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void update(final String caption) throws JSONException {
        final ProgressDialog pd = new ProgressDialog(this, R.style.dialogStyle);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("Updating post, please wait...");
        pd.show();

        JSONObject update = new JSONObject();
        update.put("caption", caption);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, EDIT_POST + postId + "/update.json", update,new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
                pd.dismiss();
                try {
                    if(response.getString("status").equals("success")){
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Unable update post. Check internet connection", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("X-User-Token", WelcomeActivity.getToken);
                headers.put("X-User-Email", WelcomeActivity.getEmail);
                return headers;
            }
        };
        RequestQueue requestQueue = RequestSingleton.getInstance(EditPostActivity.this.getApplicationContext()).getRequestQueue();
        RequestSingleton.getInstance(getBaseContext()).addToRequestQueue(jsonObjectRequest);
    }
}
