package com.flipbook.app.Settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.Switch;

import com.flipbook.app.R;

/**
 * Created by Hayden on 2017-09-30.
 */

public class NotificationSettingsActivity extends AppCompatActivity {

    private Switch likes, comments, mentions, newFollowers;
    private ImageButton back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_settings);

        likes = (Switch) findViewById(R.id.likes_swtich);
        comments = (Switch) findViewById(R.id.likes_swtich);
        mentions = (Switch) findViewById(R.id.mentions_switch);
        newFollowers = (Switch) findViewById(R.id.follows_switch);
    }
}
