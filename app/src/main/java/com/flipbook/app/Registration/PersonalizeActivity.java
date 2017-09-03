package com.flipbook.app.Registration;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.flipbook.app.Posts.RequestSingleton;
import com.flipbook.app.R;
import com.flipbook.app.Welcome.WelcomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Hayden on 2017-08-21.
 */

public class PersonalizeActivity extends AppCompatActivity{

    private final static String URL = "https://railsphotoapp.herokuapp.com//api/v1/registrations.json";

    private int PICK_IMAGE_REQUEST = 1;
    private Bitmap centeredBitmap, resizedBitmap;
    private ImageView imageView;
    private EditText bio;
    private RelativeLayout changePic;
    private Button registerButton;
    private String encodedBio, username, name, email, password;
    private boolean spinnerInitialized;
    int gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personalize);
        imageView = (ImageView) findViewById(R.id.imageView);
        bio = (EditText) findViewById(R.id.bio);
        changePic = (RelativeLayout) findViewById(R.id.changePicture);
        registerButton = (Button) findViewById(R.id.register);
        Spinner spinner = (Spinner) findViewById(R.id.gender);

        String[] genders = new String[]{
                "Gender",
                "Male",
                "Female",
                "Other",
                "Not specifying"
        };

        final List<String> genderList = new ArrayList<>(Arrays.asList(genders));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, genderList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0) {
                    // disable first item
                    return false;
                } else {
                    return true;
                }
            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position==0) {
                    // Set the disable item text color
                    tv.setTextColor(getResources().getColor(R.color.hintColor));
                }
                else {
                    tv.setTextColor(getResources().getColor(R.color.textColor));
                }
                return view;
            }

        };
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!spinnerInitialized) {
                    spinnerInitialized = true;
                    return;
                }else {
                    TextView tv = (TextView) selectedItemView;
                    tv.setTextColor(getResources().getColor(R.color.textColor));
                    gender = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        Bundle bundle = getIntent().getExtras();
        username = bundle.getString("username");
        name = bundle.getString("name");
        email = bundle.getString("email");
        password = bundle.getString("password");

        changePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(editor);
                InputMethodManager inputManager = (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
    }

    private void register(final SharedPreferences.Editor editor){
        try {
            encodedBio = URLEncoder.encode(bio.getText().toString(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final ProgressDialog pd = new ProgressDialog(this, R.style.dialogStyle);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("Loggin in, please wait...");
        pd.show();
        try {
            encodedBio = URLEncoder.encode(bio.getText().toString(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        AvatarMultipartRequest multipartRequest = new AvatarMultipartRequest(Request.Method.POST, URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                pd.dismiss();
                String result = new String(response.data);
                System.out.print(result.toString());
                try {
                    JSONObject json = new JSONObject(result);
                    System.out.print(json.toString());
                    if(json.getString("status" ).equals("created")) {
                        editor.putString("auth_token", json.getString("auth_token"));
                        editor.putString("email", json.getString("email"));
                        editor.putString("username", json.getString("user_name"));
                        editor.apply();
                        registerButton.setClickable(false);
                        Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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
                params.put("email", email);
                params.put("username", username);
                params.put("name", name);
                params.put("password", password);
                params.put("bio", encodedBio);
                params.put("gender", String.valueOf(gender));
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> dataParams = new HashMap<>();
                dataParams.put("avatar", new DataPart("avatar.jpg", getFileDataFromDrawable(getBaseContext(), resizedBitmap), "image/jpeg"));
                return dataParams;
            }
        };
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = RequestSingleton.getInstance(PersonalizeActivity.this.getApplicationContext()).getRequestQueue();
        RequestSingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);
    }

    public byte[] getFileDataFromDrawable(Context context, Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

            //correctly orientate images from gallery
            String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
            Cursor cur = managedQuery(uri, orientationColumn, null, null, null);
            int orientation = -1;
            if (cur != null && cur.moveToFirst()) {
                orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
            }
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                //center bitmap image
                if (bitmap.getWidth() >= bitmap.getHeight()){
                    centeredBitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth()/2 - bitmap.getHeight()/2, 0, bitmap.getHeight(), bitmap.getHeight(), matrix, true);
                }else{
                    centeredBitmap = Bitmap.createBitmap(bitmap, 0, bitmap.getHeight()/2 - bitmap.getWidth()/2, bitmap.getWidth(), bitmap.getWidth(), matrix, true);
                }

                resizedBitmap = Bitmap.createScaledBitmap(centeredBitmap, 200, 200, true);

                imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(resizedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
