package com.flipbook.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.toolbox.ImageLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Hayden on 2017-03-06.
 */

public class PostAdapter extends ArrayAdapter {

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
    public View getView(int position, View convertView, final ViewGroup parent) {
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
            //postHolder.progress = (ProgressBar) row.findViewById(R.id.loader);
            row.setTag(postHolder);
        } else {
            postHolder = (PostHolder) row.getTag();
        }

        final Posts posts = (Posts) this.getItem(position);
        postHolder.username.setText(posts.getUsername());
        postHolder.caption.setText(posts.getCaption());
        postHolder.likes.setText(posts.getLikes());

        Glide.with(getContext()).load(posts.getImages().get(0)).downloadOnly(1024, 1024);
        Glide.with(getContext()).load(posts.getImages().get(0)).diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(R.color.hintColor).dontAnimate().into(postHolder.images);
        //set click listener for imageview
        postHolder.images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPhotoAnimation(posts, postHolder.images);
            }
        });

        return row;
    }

    static class PostHolder {
        //ProgressBar progress;
        TextView username, caption, likes;
        ImageView images;
    }

    public void createPhotoAnimation(final Posts posts, final ImageView imageView) {
        final AnimationDrawable animation = new AnimationDrawable();
        final int speed = Math.round(1.0f / posts.getSpeed() * 1000.0f);

        new AsyncTask<String, String, AnimationDrawable>() {
            @Override
            protected AnimationDrawable doInBackground(String... params) {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                for (int i = 0; i < posts.getImages().size(); i++) {

                    Bitmap b = null;
                    try {
                        b = Glide.with(getContext()).load(posts.getImages().get(i)).asBitmap().into(1024, 1024).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    Drawable d = new BitmapDrawable(getContext().getResources(), b);
                    animation.addFrame(d, speed);
                }
                return animation;
            }

            @Override
            protected void onPostExecute(AnimationDrawable animationDrawable) {
                animation.start();
                animation.setOneShot(false);
                imageView.setImageDrawable(animation);
            }
        }.execute();
    }
}