package com.flipbook.app.Comments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.flipbook.app.Posts.RequestSingleton;
import com.flipbook.app.R;
import com.flipbook.app.Users.UserAdapter;
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

    List list = new ArrayList();

    private final static String DELETE_COMMENT = "https://railsphotoapp.herokuapp.com//api/v1/comments/";

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
            commentHolder.avatar = (ImageView) row.findViewById(R.id.avatar);
            commentHolder.commentLayout = (RelativeLayout) row.findViewById(R.id.container);
            row.setTag(commentHolder);
        } else {
            commentHolder = (CommentHolder) row.getTag();
        }
        Comment comment = (Comment) this.getItem(position);
        commentHolder.text.setText(comment.getText());
        commentHolder.username.setText(comment.getUsername());
        Glide.with(getContext()).load(comment.getUserAvatar()).into(commentHolder.avatar);

        //option to delete comment
        if(comment.getUsername().equals(WelcomeActivity.prefs.getString("username", ""))){
            deleteComment(comment, position);
        }

        return row;
    }

    static class CommentHolder{
        RelativeLayout commentLayout;
        ImageView avatar;
        TextView username, text;
    }

    public void deleteComment(final Comment comment, final int position){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, DELETE_COMMENT + comment.getId(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getString("status").equals("destroyed")) {
                        list.remove(position);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
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
