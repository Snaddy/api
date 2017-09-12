package com.flipbook.app.Users;

import android.app.Activity;
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
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.flipbook.app.Posts.RequestSingleton;
import com.flipbook.app.Uploads.MultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    private int PICK_IMAGE_REQUEST = 1;

    private String getEmail, getToken, username, name, email, bio, avatarUrl;
    private boolean validUsername, validEmail;
    private ImageView profilePic;
    private RelativeLayout changePic;
    private EditText editUsername, editName, editEmail, editBio;
    private Button saveProfile;
    private Bitmap centeredBitmap, resizedBitmap, image;
    private Spinner spinner;
    int gender;

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
        try {
            name = URLDecoder.decode(name, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        email = bundle.getString("email");
        bio = bundle.getString("bio");
        try {
            bio = URLDecoder.decode(bio, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        gender = bundle.getInt("gender");
        avatarUrl = bundle.getString("avatar");

        editUsername = (EditText) findViewById(R.id.editUsername);
        editName = (EditText) findViewById(R.id.editName);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editBio = (EditText) findViewById(R.id.editBio);
        profilePic = (ImageView) findViewById(R.id.editAvatar);
        changePic = (RelativeLayout) findViewById(R.id.changePicture);

        //save profile button
        saveProfile = (Button) findViewById(R.id.saveButton);

        editUsername.setText(username);
        editUsername.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editName.setText(name);
        editEmail.setText(email);
        editBio.setText(bio);
        Glide.with(getApplicationContext()).load(avatarUrl).into(profilePic);
        spinner = (Spinner) findViewById(R.id.gender);

        //start image picking activity
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

        //custom spinner
            String[] genders = new String[]{
                    "Gender",
                    "Male",
                    "Female",
                    "Other",
                    "Not specified"
            };

            final List<String> genderList = new ArrayList<>(Arrays.asList(genders));

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.profile_spinner_item, genderList){
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
                    } else {
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
                    TextView tv = (TextView) selectedItemView;
                    tv.setTextColor(getResources().getColor(R.color.textColor));
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {}
            });
        if(spinner.getSelectedItemPosition() == 0) {
            //set gender to not specified
            spinner.setSelection(4);
        } else {
            spinner.setSelection(gender);
        }

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
                    editProfileRequest(editor, image);
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

    private void editProfileRequest(final SharedPreferences.Editor editor, final Bitmap bitmap){
        final ProgressDialog pd = new ProgressDialog(this, R.style.dialogStyle);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("Updating, please wait...");
        pd.show();
        MultipartRequest multipartRequest = new MultipartRequest(Request.Method.PUT, EDIT_USER_URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                pd.dismiss();
                String result = new String(response.data);
                try {
                    JSONObject json = new JSONObject(result);
                    if(json.getString("result").equals("success")) {
                        ProfileActivity.profileActivity.finish();
                        editor.putString("email", editEmail.getText().toString());
                        editor.putString("username", editUsername.getText().toString());
                        editor.apply();
                        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
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
                params.put("email", editEmail.getText().toString());
                params.put("username", editUsername.getText().toString());
                params.put("name", editName.getText().toString());
                params.put("bio", editBio.getText().toString());
                params.put("gender", String.valueOf(gender));
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> dataParams = new HashMap<>();
                if(bitmap != null) {
                    dataParams.put("avatar", new DataPart("avatar.jpg", getFileDataFromDrawable(getBaseContext(), bitmap), "image/jpeg"));
                }
                return dataParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-User-Token", getToken);
                headers.put("X-User-Email", getEmail);
                return headers;
            }
        };
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = RequestSingleton.getInstance(EditProfileActivity.this.getApplicationContext()).getRequestQueue();
        RequestSingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);
    }

    public byte[] getFileDataFromDrawable(Context context, Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    //image selection
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
                image = resizedBitmap;

                profilePic = (ImageView) findViewById(R.id.editAvatar);
                profilePic.setImageBitmap(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
