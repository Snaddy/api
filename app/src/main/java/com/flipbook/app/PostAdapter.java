package com.flipbook.app;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hayden on 2017-03-06.
 */

public class PostAdapter extends ArrayAdapter{

    List list = new ArrayList();
    private ImageLoader imageLoader;

    public PostAdapter(Context context, int resource) {
        super(context, resource);
        imageLoader = RequestSingleton.getInstance(context).getImageLoader();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        row = convertView;
        PostHolder postHolder;
        if(row == null){
            LayoutInflater layoutInflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.postitem, parent, false);
            postHolder = new PostHolder();
            postHolder.username = (TextView) row.findViewById(R.id.username);
            postHolder.caption = (TextView) row.findViewById(R.id.caption);
            postHolder.likes = (TextView) row.findViewById(R.id.likes);
            postHolder.images = (NetworkImageView) row.findViewById(R.id.images);
            row.setTag(postHolder);
        } else {
            postHolder = (PostHolder)row.getTag();
        }

        Posts posts = (Posts)this.getItem(position);
        postHolder.username.setText(posts.getUsername());
        postHolder.caption.setText(posts.getCaption());
        postHolder.likes.setText(posts.getLikes());
        //imageLoader.get(posts.getImages(), imageLoader.getImageListener(postHolder.images, R.color.colorWhite, R.color.colorWhite));
        //postHolder.images.setImageUrl(posts.getImages(), imageLoader);
        return row;
    }

    static class PostHolder {
        TextView username, caption, likes;
        NetworkImageView images;
    }
}
