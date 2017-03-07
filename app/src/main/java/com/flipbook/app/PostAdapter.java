package com.flipbook.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hayden on 2017-03-06.
 */

public class PostAdapter extends ArrayAdapter{

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
            row.setTag(postHolder);
        } else {
            postHolder = (PostHolder)row.getTag();
        }

        Posts posts = (Posts)this.getItem(position);
        postHolder.username.setText(posts.getUsername());
        postHolder.caption.setText(posts.getCaption());
        postHolder.likes.setText(posts.getLikes());
        return row;
    }

    static class PostHolder {
        TextView username, caption, likes;
    }
}
