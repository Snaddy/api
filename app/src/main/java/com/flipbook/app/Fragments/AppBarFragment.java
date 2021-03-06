package com.flipbook.app.Fragments;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.flipbook.app.Camera.CameraActivity;
import com.flipbook.app.Users.ProfileActivity;
import com.flipbook.app.R;
import com.flipbook.app.Users.SearchActivity;
import com.flipbook.app.Welcome.WelcomeActivity;

/**
 * Created by Hayden on 2017-03-06.
 */

public class AppBarFragment extends Fragment {

    ImageButton home, notifications, newPost, search, profile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.appbar, container, false);

        home = (ImageButton) view.findViewById(R.id.home);
        notifications = (ImageButton) view.findViewById(R.id.notifications);
        newPost = (ImageButton) view.findViewById(R.id.newPost);
        search = (ImageButton) view.findViewById(R.id.searchButton);
        profile = (ImageButton) view.findViewById(R.id.profile);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), WelcomeActivity.class));
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ProfileActivity.class));
            }
        });

        newPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CameraActivity.class));
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("title", "Search");
                startActivity(intent);
            }
        });

        return view;
    }
}
