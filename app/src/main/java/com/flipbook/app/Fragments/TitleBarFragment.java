package com.flipbook.app.Fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flipbook.app.R;

/**
 * Created by Hayden on 2017-03-07.
 */

public class TitleBarFragment extends Fragment {

    private TextView title;
    private Typeface font;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.titlebar, container, false);

        title = (TextView) view.findViewById(R.id.title);

        font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/default.otf");
      title.setTypeface(font);

      return view;
    }
}
