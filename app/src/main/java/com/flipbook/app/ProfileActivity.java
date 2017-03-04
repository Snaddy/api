package com.flipbook.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by Hayden on 2017-03-03.
 */

public class ProfileActivity extends AppCompatActivity {

    ImageButton home, notifications, newPost, search, profile;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        title = (TextView) findViewById(R.id.title);
        Typeface face=Typeface.createFromAsset(getAssets(),"fonts/futura.ttf");
        title.setTypeface(face);

        home = (ImageButton) findViewById(R.id.home);
        notifications = (ImageButton) findViewById(R.id.notifications);
        newPost = (ImageButton) findViewById(R.id.newPost);
        search = (ImageButton) findViewById(R.id.search);
        profile = (ImageButton) findViewById(R.id.profile);

        profile.setImageResource(R.drawable.profile_selected);
        profile.setClickable(false);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                overridePendingTransition(0, 0);
            }
        });
    }

    public void onPause(){
        super.onPause();
        overridePendingTransition(0,0);
    }
}
