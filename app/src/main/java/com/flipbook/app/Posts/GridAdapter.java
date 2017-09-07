package com.flipbook.app.Posts;

import android.content.Context;
import android.content.Intent;
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
 * Created by Hayden on 2017-09-06.
 */

public class GridAdapter extends ArrayAdapter {

    List list = new ArrayList();

    public GridAdapter(Context context, int resource) {
        super(context, resource);
    }

    public void add(GridImage object) {
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
        final GridHolder gridHolder;
        if (row == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.grid_item, parent, false);
            gridHolder = new GridHolder();
            gridHolder.imageView = (ImageView) row.findViewById(R.id.imageView);
            row.setTag(gridHolder);
        } else {
            gridHolder = (GridHolder) row.getTag();
        }

        final GridImage gridImage = (GridImage) this.getItem(position);

        //download first display image :)
        Glide.with(getContext()).load(gridImage.getImage()).override(400, 400).into(gridHolder.imageView);

        gridHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.print("Clicked");
                Intent intent = new Intent(getContext(), ShowActivity.class);
                intent.putExtra("postId", gridImage.getId());
                getContext().startActivity(intent);
            }
        });

        return row;
    }

    static class GridHolder {
        ImageView imageView;
    }
}
