package com.flipbook.app.Posts;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.flipbook.app.R;
import java.util.ArrayList;
import java.util.List;

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
        Glide.with(getContext()).load(gridImage.getImage()).override(200, 200).dontAnimate().into(gridHolder.imageView);

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
