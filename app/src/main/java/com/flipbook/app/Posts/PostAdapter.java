package com.flipbook.app.Posts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.flipbook.app.R;
import com.flipbook.app.Users.ProfileActivity;
import com.flipbook.app.Users.UserActivity;
import com.flipbook.app.Welcome.WelcomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Hayden on 2017-03-06.
 */

public class PostAdapter extends ArrayAdapter {

    private final String likeURL = "https://railsphotoapp.herokuapp.com//api/v1/like/";
    private final String unlikeURL = "https://railsphotoapp.herokuapp.com//api/v1/unlike/";

    List list = new ArrayList();

    public PostAdapter(Context context, int resource) {
        super(context, resource);
    }

    public void add(Posts object) {
        super.add(object);
        list.add(object);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View row;
        row = convertView;
        final PostHolder postHolder;
        if (row == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.postitem, parent, false);
            postHolder = new PostHolder();
            postHolder.username = (TextView) row.findViewById(R.id.username);
            postHolder.caption = (TextView) row.findViewById(R.id.caption);
            postHolder.likes = (TextView) row.findViewById(R.id.likes);
            postHolder.images = (ImageView) row.findViewById(R.id.images);
            postHolder.likeButton = (ImageButton) row.findViewById(R.id.imageButton);
            postHolder.postDate = (TextView) row.findViewById(R.id.postDate);
            row.setTag(postHolder);
        } else {
            postHolder = (PostHolder) row.getTag();
        }

        final Posts posts = (Posts) this.getItem(position);
        postHolder.username.setText(posts.getUsername());
        postHolder.postDate.setText(posts.getPostDate());
        //if no caption
        if(posts.getCaption().length() == 0){
            postHolder.caption.setVisibility(View.GONE);
        } else {
            postHolder.caption.setVisibility(View.VISIBLE);
            postHolder.caption.setText(posts.getCaption());
        }

        //showing 0 likes
        if (posts.getLikesCount() == 0) {
            postHolder.likes.setText("");
        } else {
            postHolder.likes.setText(posts.getLikesCount() + "");
        }

        //if post is liked
        if(posts.getLiked() == true){
            postHolder.likeButton.setImageDrawable(getContext().getResources().getDrawable(R.drawable.liked));
        } else {
            postHolder.likeButton.setImageDrawable(getContext().getResources().getDrawable(R.drawable.unliked));
        }

        //download first display image :)
        if(posts.getImages().size() > 0) {
            Glide.with(getContext()).load(posts.getImages().get(0)).downloadOnly(1600, 1600);
            Glide.with(getContext()).load(posts.getImages().get(0)).diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(R.color.colorWhite).dontAnimate().into(postHolder.images);
        }
            //set click listener for imageview
        postHolder.images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPhotoAnimation(posts, postHolder.images);
            }
        });

        postHolder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(posts.getUsername().equals(WelcomeActivity.prefs.getString("username", ""))) {
                    Intent intent = new Intent(getContext(), ProfileActivity.class);
                    getContext().startActivity(intent);
                } else {
                    Intent intent = new Intent(getContext(), UserActivity.class);
                    intent.putExtra("userId", posts.getUserId());
                    getContext().startActivity(intent);
                }
            }
        });

        postHolder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //initially unliked
                if(posts.getLiked() == false) {
                    postHolder.likeButton.setEnabled(false);
                    postHolder.likeButton.setSelected(!postHolder.likeButton.isSelected());
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, likeURL + posts.getId(), null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getString("info").equals("liked")) {
                                    postHolder.likeButton.setImageDrawable(getContext().getResources().getDrawable(R.drawable.liked));
                                    posts.setLikesCount(posts.getLikesCount() + 1);
                                    postHolder.likes.setText(posts.getLikesCount() + "");
                                    posts.setLiked(true);
                                    postHolder.likeButton.setEnabled(true);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }) {

                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("X-User-Token", WelcomeActivity.getToken);
                            headers.put("X-User-Email", WelcomeActivity.getEmail);
                            return headers;
                        }
                    };
                    RequestQueue requestQueue = RequestSingleton.getInstance(getContext().getApplicationContext()).getRequestQueue();
                    RequestSingleton.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
                    System.out.println(jsonObjectRequest);
                } else {
                    postHolder.likeButton.setEnabled(false);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, unlikeURL + posts.getId(), null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getString("info").equals("unliked")) {
                                    postHolder.likeButton.setImageDrawable(getContext().getResources().getDrawable(R.drawable.unliked));
                                    posts.setLikesCount(posts.getLikesCount() - 1);
                                    if(posts.getLikesCount() == 0) {
                                        postHolder.likes.setText("");
                                    } else {
                                        postHolder.likes.setText(posts.getLikesCount() + "");
                                    }
                                    posts.setLikedByUser(false);
                                    postHolder.likeButton.setEnabled(true);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }) {

                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("X-User-Token", WelcomeActivity.getToken);
                            headers.put("X-User-Email", WelcomeActivity.getEmail);
                            return headers;
                        }
                    };
                    RequestQueue requestQueue = RequestSingleton.getInstance(getContext().getApplicationContext()).getRequestQueue();
                    RequestSingleton.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
                }
            }
        });

        return row;
    }

    static class PostHolder {
        TextView username, caption, likes, postDate;
        ImageButton likeButton;
        ImageView images;
    }

    //method to download all images in a post and making it into an animation
    public void createPhotoAnimation(final Posts posts, final ImageView imageView) {
        final AnimationDrawable animation = new AnimationDrawable();
        final int speed = Math.round(1.0f / posts.getSpeed() * 1000.0f);

        //async task to cache all images
        new AsyncTask<String, String, AnimationDrawable>() {
            @Override
            protected AnimationDrawable doInBackground(String... params) {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                //iterate through all images and convert into bitmaps
                for (int i = 0; i < posts.getImages().size(); i++) {
                    Bitmap b = null;
                    try {
                        b = Glide.with(getContext()).load(posts.getImages().get(i)).asBitmap().into(1600, 1600).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    //add images to animation
                    Drawable d = new BitmapDrawable(getContext().getResources(), b);
                    animation.addFrame(d, speed);
                }
                return animation;
            }

            //after all images are downloaded and animation is complete, start animation on loop
            //set animation to image view
            @Override
            protected void onPostExecute(AnimationDrawable animationDrawable) {
                animation.start();
                animation.setOneShot(false);
                imageView.setImageDrawable(animation);
            }
        }.execute();
    }
}