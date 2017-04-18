package com.flipbook.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by Hayden on 2017-03-07.
 */

public class TitleBarFragment extends Fragment {

    private TextView title;
    private Typeface rounded;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.titlebar, container, false);

        title = (TextView) view.findViewById(R.id.titlebar);

        rounded = Typeface.createFromAsset(getActivity().getAssets(), "fonts/ARIALN.TTF");
        title.setTypeface(rounded);

        return view;
    }
}
