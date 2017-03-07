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

    private ImageButton profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profile = (ImageButton) findViewById(R.id.profile);
        profile.setImageResource(R.drawable.profile_selected);
    }

    public void onPause(){
        super.onPause();
        overridePendingTransition(0,0);
    }
}
