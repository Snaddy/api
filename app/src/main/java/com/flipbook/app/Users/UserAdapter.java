package com.flipbook.app.Users;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.flipbook.app.R;
import com.flipbook.app.Welcome.WelcomeActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hayden on 2017-09-04.
 */

public class UserAdapter extends ArrayAdapter{
    List list = new ArrayList();

    public UserAdapter(Context context, int resource) {
        super(context, resource);
    }

    public void add(UserItem object) {
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
        final UserHolder userHolder;
        if (row == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.user_item, parent, false);
            userHolder = new UserHolder();
            userHolder.username = (TextView) row.findViewById(R.id.username);
            userHolder.name = (TextView) row.findViewById(R.id.name);
            userHolder.avatar = (ImageView) row.findViewById(R.id.avatar);
            userHolder.searchItem = (RelativeLayout) row.findViewById(R.id.searchItem);
            row.setTag(userHolder);
        } else {
            userHolder = (UserHolder) row.getTag();
        }
        final UserItem user = (UserItem) this.getItem(position);
        userHolder.username.setText(user.getUsername());
        userHolder.name.setText(user.getName());
        //show user profile picture
        Glide.with(getContext()).load(user.getAvatar()).dontAnimate().error(R.drawable.profile_selected).into(userHolder.avatar);

        userHolder.searchItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user.getUsername().equals(WelcomeActivity.prefs.getString("username", ""))) {
                    Intent intent = new Intent(getContext(), ProfileActivity.class);
                    getContext().startActivity(intent);
                } else {
                    Intent intent = new Intent(getContext(), UserActivity.class);
                    intent.putExtra("userId", user.getId());
                    getContext().startActivity(intent);
                }
            }
        });
        return row;
    }
    static class UserHolder {
        RelativeLayout searchItem;
        TextView username, name;
        ImageView avatar;
    }
}
