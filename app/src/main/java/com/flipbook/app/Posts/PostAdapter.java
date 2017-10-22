package com.flipbook.app.Posts;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.flipbook.app.Comments.Comment;
import com.flipbook.app.R;
import com.flipbook.app.Settings.SettingActivity;
import com.flipbook.app.Users.ProfileActivity;
import com.flipbook.app.Users.UserActivity;
import com.flipbook.app.Users.UserItem;
import com.flipbook.app.Users.UserList;
import com.flipbook.app.Welcome.WelcomeActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Hayden on 2017-03-06.
 */

public class PostAdapter extends ArrayAdapter {

    private final String LIKE_URL = "https://railsphotoapp.herokuapp.com//api/v1/like/";
    private final String UNLIKE_URL = "https://railsphotoapp.herokuapp.com//api/v1/unlike/";
    private final String DELETE_POST = "https://railsphotoapp.herokuapp.com//api/v1/post/";
    private final static String REPORT_URL = "https://railsphotoapp.herokuapp.com//api/v1/report.json";
    private final static String LIKES_URL = "https://railsphotoapp.herokuapp.com//api/v1/likes/";

    public List list = new ArrayList();

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
            row = layoutInflater.inflate(R.layout.post_item, parent, false);
            postHolder = new PostHolder();
            postHolder.username = (TextView) row.findViewById(R.id.username);
            postHolder.info = (RelativeLayout) row.findViewById(R.id.info);
            postHolder.caption = (TextView) row.findViewById(R.id.caption);
            postHolder.likes = (TextView) row.findViewById(R.id.likes);
            postHolder.comments = (TextView) row.findViewById(R.id.comments);
            postHolder.images = (ImageView) row.findViewById(R.id.images);
            postHolder.avatarImageView = (ImageView) row.findViewById(R.id.userAvatar);
            postHolder.likeButton = (ImageButton) row.findViewById(R.id.imageButton);
            postHolder.commentButton = (ImageButton) row.findViewById(R.id.commentButton);
            postHolder.options = (ImageButton) row.findViewById(R.id.options);
            postHolder.postDate = (TextView) row.findViewById(R.id.postDate);
            postHolder.loader = (ProgressBar) row.findViewById(R.id.loader);
            row.setTag(postHolder);
            postHolder.loader.setVisibility(View.INVISIBLE);
        } else {
            postHolder = (PostHolder) row.getTag();
        }

        final Posts posts = (Posts) this.getItem(position);
        posts.setClicked(false);
        postHolder.images.setClickable(true);
        postHolder.position = position;
        postHolder.username.setText(posts.getUsername());
        postHolder.postDate.setText(posts.getPostDate());

        //download first display image :)
        Glide.with(getContext()).load(posts.getImages().get(0)).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA)).into(postHolder.images);

        //set user profile picture
        Glide.with(getContext()).load(posts.getUserAvatar()).apply(new RequestOptions().circleCrop()).into(postHolder.avatarImageView);

        //load images
        createPhotoAnimation(posts, postHolder, position);

        postHolder.images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                posts.setClicked(true);
                postHolder.images.setClickable(false);
                System.out.println("clicked: " + posts.isClicked() + ", loaded: " + posts.isLoaded());
                if(posts.isLoaded()){
                    playAnimation(createPhotoAnimation(posts, postHolder, position));
                } else {
                    postHolder.loader.setVisibility(View.VISIBLE);
                }
            }
        });

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

        //show comments
        if (posts.getCommentCount() == 0){
            postHolder.comments.setText("");
        } else {
            postHolder.comments.setText(posts.getCommentCount() + "");
        }

        //if post is liked
        if(posts.getLiked()){
            postHolder.likeButton.setImageDrawable(getContext().getResources().getDrawable(R.drawable.liked));
        } else {
            postHolder.likeButton.setImageDrawable(getContext().getResources().getDrawable(R.drawable.unliked));
        }
        //set click listener for imageview

        postHolder.info.setOnClickListener(new View.OnClickListener() {
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
                    postHolder.likeButton.setSelected(!postHolder.likeButton.isSelected());
                    postHolder.likeButton.setImageDrawable(getContext().getResources().getDrawable(R.drawable.liked));
                    posts.setLikesCount(posts.getLikesCount() + 1);
                    postHolder.likes.setText(posts.getLikesCount() + "");
                    posts.setLiked(true);
                    postHolder.likeButton.setEnabled(true);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, LIKE_URL + posts.getId(), null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (!response.getString("info").equals("liked")) {
                                    postHolder.likeButton.setImageDrawable(getContext().getResources().getDrawable(R.drawable.unliked));
                                    posts.setLikesCount(posts.getLikesCount() - 1);
                                    postHolder.likes.setText(posts.getLikesCount() + "");
                                    posts.setLiked(false);
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
                    postHolder.likeButton.setImageDrawable(getContext().getResources().getDrawable(R.drawable.unliked));
                    posts.setLikesCount(posts.getLikesCount() - 1);
                    if(posts.getLikesCount() == 0) {
                        postHolder.likes.setText("");
                    } else {
                        postHolder.likes.setText(posts.getLikesCount() + "");
                    }
                    posts.setLikedByUser(false);
                    postHolder.likeButton.setEnabled(true);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, UNLIKE_URL + posts.getId(), null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (!response.getString("info").equals("unliked")) {
                                    postHolder.likeButton.setImageDrawable(getContext().getResources().getDrawable(R.drawable.unliked));
                                    posts.setLikesCount(posts.getLikesCount() + 1);
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

        postHolder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UserList.class);
                intent.putExtra("url", LIKES_URL + posts.getId() + "");
                intent.putExtra("title", "Likes");
                getContext().startActivity(intent);
            }
        });

        postHolder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ShowActivity.class);
                intent.putExtra("postId", posts.getId());
                intent.putExtra("username", posts.getUsername());
                getContext().startActivity(intent);
            }
        });

        postHolder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (posts.getUsername().equals(WelcomeActivity.prefs.getString("username", ""))) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.options_dialog, null);
                    builder.setView(dialogView);
                    TextView edit = (TextView) dialogView.findViewById(R.id.title);
                    TextView delete = (TextView) dialogView.findViewById(R.id.delete);
                    TextView cancel = (TextView) dialogView.findViewById(R.id.cancel);
                    final AlertDialog alertDialog = builder.create();
                    edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            Intent intent = new Intent(getContext(), EditPostActivity.class);
                            intent.putExtra("postId", posts.getId());
                            intent.putExtra("speed", posts.getSpeed());
                            intent.putExtra("caption", posts.getCaption());
                            intent.putExtra("images", posts.getImages());
                            getContext().startActivity(intent);
                        }
                    });

                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.ok_dialog, null);
                            builder.setView(dialogView);
                            TextView title = (TextView) dialogView.findViewById(R.id.title);
                            Button ok = (Button) dialogView.findViewById(R.id.okButton);
                            Button cancel = (Button) dialogView.findViewById(R.id.cancelButton);
                            title.setText("Are you sure you want to delete this post?");
                            final AlertDialog alertDialog = builder.create();
                            ok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deletePost(posts, position);
                                    alertDialog.dismiss();
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

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.content_dialog, null);
                    builder.setView(dialogView);
                    TextView title = (TextView) dialogView.findViewById(R.id.title);
                    final TextView content = (TextView) dialogView.findViewById(R.id.message);
                    Button ok = (Button) dialogView.findViewById(R.id.okButton);
                    Button cancel = (Button) dialogView.findViewById(R.id.cancelButton);
                    title.setText("Why are you reporting this post?");
                    final AlertDialog alertDialog = builder.create();
                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendReport(content.getText().toString(), REPORT_URL);
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
            }
        });

        return row;
    }

    static class PostHolder {
        TextView username, caption, likes, postDate, comments;
        ImageButton likeButton, commentButton, options;
        ImageView images, avatarImageView;
        ProgressBar loader;
        RelativeLayout info;
        int position;
    }

    private void deletePost(final Posts post, final int position){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, DELETE_POST + post.getId(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
                try {
                    if (response.getString("status").equals("destroyed")) {
                        list.remove(position);
                        notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Unable to delete post. Check internet connection", Toast.LENGTH_SHORT).show();
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

    private void sendReport(final String content, String url){
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
                    Toast.makeText(getContext(), "Unable to send report. Check internet connection", Toast.LENGTH_SHORT).show();
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
                headers.put("X-User-Token", WelcomeActivity.getToken);
                headers.put("X-User-Email", WelcomeActivity.getToken);
                return headers;
            }
        };
        RequestQueue requestQueue = RequestSingleton.getInstance(getContext().getApplicationContext().getApplicationContext()).getRequestQueue();
        RequestSingleton.getInstance(getContext().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    //download all images in a post and make it into an animation
    private AnimationDrawable createPhotoAnimation(final Posts posts, final PostHolder postHolder, final int position) {
        final AnimationDrawable animation = new AnimationDrawable();
        final int speed = Math.round(1.0f / posts.getSpeed() * 1000.0f);

        //async task to cache all images
            new AsyncTask<String, String, AnimationDrawable>() {
                @Override
                protected AnimationDrawable doInBackground(String... params) {
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    //iterate through all images and convert into bitmaps
                    for (int i = 0; i < posts.getImages().size(); i++) {
                        Drawable d = null;
                        try {
                            d = Glide.with(getContext()).load(posts.getImages().get(i)).into(1080, 1080).get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                        //add images to animation
                        animation.addFrame(d, speed);
                    }
                    return animation;
                }

                //after all images are downloaded and animation is complete, start animation on loop
                //set animation to image view
                @Override
                protected void onPostExecute(final AnimationDrawable animationDrawable) {
                    if(postHolder.position == position) {
                        postHolder.images.setImageDrawable(animation);
                        if(posts.isClicked()){
                            animation.start();
                        }
                        posts.setLoaded(true);
                    }
                    postHolder.loader.setVisibility(View.GONE);
                }
            }.execute();
        return animation;
    }

    private void playAnimation(AnimationDrawable animation){
        animation.start();
    }
}