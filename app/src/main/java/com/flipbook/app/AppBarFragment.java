package com.flipbook.app;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

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
        search = (ImageButton) view.findViewById(R.id.search);
        profile = (ImageButton) view.findViewById(R.id.profile);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                home.setImageResource(R.drawable.home_selected);
                home.setClickable(false);
                startActivity(new Intent(getActivity(), WelcomeActivity.class));
                getActivity().overridePendingTransition(0, 0);
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.setImageResource(R.drawable.profile_selected);
                profile.setClickable(false);
                startActivity(new Intent(getActivity(), ProfileActivity.class));
                getActivity().overridePendingTransition(0, 0);
            }
        });
    }


}
