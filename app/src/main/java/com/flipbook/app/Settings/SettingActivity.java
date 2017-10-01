package com.flipbook.app.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import com.flipbook.app.R;
import com.flipbook.app.Users.EditProfileActivity;
import com.flipbook.app.Users.UserList;

/**
 * Created by Hayden on 2017-09-29.
 */

public class SettingActivity extends AppCompatActivity{

    private final static String BLOCKED_USERS = "https://railsphotoapp.herokuapp.com//api/v1/users/blocked";

    private Button editProfile, changePassword, blockedUsers, notifications,
            reportProblem, suggestion, privacyPolicy, tos, licenses, logout;
    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editProfile = (Button) findViewById(R.id.edit_profile);
        changePassword = (Button) findViewById(R.id.change_password);
        blockedUsers = (Button) findViewById(R.id.blockedUsers);
        notifications = (Button) findViewById(R.id.notifications);
        reportProblem = (Button) findViewById(R.id.report_problem);
        suggestion = (Button) findViewById(R.id.suggestion);
        privacyPolicy = (Button) findViewById(R.id.privacy_policy);
        back = (ImageButton) findViewById(R.id.back);

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        blockedUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserList.class);
                intent.putExtra("url", BLOCKED_USERS);
                startActivity(intent);
            }
        });

        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NotificationSettingsActivity.class);
                startActivity(intent);
            }
        });

        //TODO notifcations

        //TODO reporting problems

        //TODO suggestings things to add to app

        //TODO privacy policy, terms of use, licenses (Easy...kinda)

        //TODO logout (Easy)

        //finish activty on back button click
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
