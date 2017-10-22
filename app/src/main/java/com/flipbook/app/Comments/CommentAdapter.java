package com.flipbook.app.Comments;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.flipbook.app.Posts.RequestSingleton;
import com.flipbook.app.Posts.ShowActivity;
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

/**
 * Created by Hayden on 2017-09-11.
 */

public class CommentAdapter extends ArrayAdapter {

    public List list = new ArrayList();

    private final static String DELETE_COMMENT = "https://railsphotoapp.herokuapp.com//api/v1/post/";

    public CommentAdapter(Context context, int resource){
        super(context, resource);
    }

    public void add(Comment object){
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
    public View getView(final int position, View convertView, final ViewGroup parent){
        View row;
        row = convertView;
        final CommentHolder commentHolder;

        if (row == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.comment_item, parent, false);
            commentHolder = new CommentHolder();
            commentHolder.username = (TextView) row.findViewById(R.id.username);
            commentHolder.text = (TextView) row.findViewById(R.id.text);
            commentHolder.avatar = (ImageView) row.findViewById(R.id.profilePicture);
            commentHolder.commentLayout = (RelativeLayout) row.findViewById(R.id.container);
            commentHolder.posted = (TextView) row.findViewById(R.id.posted);
            row.setTag(commentHolder);
        } else {
            commentHolder = (CommentHolder) row.getTag();
        }
        final Comment comment = (Comment) this.getItem(position);
        commentHolder.text.setText(comment.getText());
        commentHolder.username.setText(comment.getUsername());
        commentHolder.posted.setText(comment.getPostedAt());
        Glide.with(getContext()).load(comment.getUserAvatar()).apply(new RequestOptions().circleCrop()).into(commentHolder.avatar);

        commentHolder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(comment.getUsername().equals(WelcomeActivity.prefs.getString("username", ""))) {
                    Intent intent = new Intent(getContext(), ProfileActivity.class);
                    getContext().startActivity(intent);
                } else {
                    Intent intent = new Intent(getContext(), UserActivity.class);
                    intent.putExtra("userId", comment.getUserId());
                    getContext().startActivity(intent);
                }
            }
        });


        commentHolder.commentLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                commentHolder.commentLayout.setBackgroundResource(R.color.dividerColor);
                if(comment.getUsername().equals(WelcomeActivity.prefs.getString("username", "")) ||
                        ShowActivity.username.equals(WelcomeActivity.prefs.getString("username", ""))){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.ok_dialog, null);
                    builder.setView(dialogView);
                    TextView title = (TextView) dialogView.findViewById(R.id.title);
                    Button ok = (Button) dialogView.findViewById(R.id.okButton);
                    Button cancel = (Button) dialogView.findViewById(R.id.cancelButton);
                    title.setText("Would you like to delete this comment?");
                    final AlertDialog alertDialog = builder.create();
                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteComment(comment, position);
                            alertDialog.dismiss();
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            commentHolder.commentLayout.setBackgroundResource(R.color.colorWhite);
                        }
                    });

                    alertDialog.show();
                }
                return false;
            }
        });

        return row;
    }

    static class CommentHolder{
        RelativeLayout commentLayout;
        ImageView avatar;
        TextView username, text, posted;
    }

    private void deleteComment(final Comment comment, final int position){
        //comment doesn't really exist in the comment adapter yet
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, DELETE_COMMENT + comment.getPostId() + "/comments/" + comment.getId(), null, new Response.Listener<JSONObject>() {
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
                    Toast.makeText(getContext(), "Unable to delete comment. Check internet connection", Toast.LENGTH_SHORT).show();
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
