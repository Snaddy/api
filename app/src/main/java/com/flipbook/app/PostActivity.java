package com.flipbook.app;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Hayden on 2017-03-19.
 */

public class PostActivity extends AppCompatActivity{

    private ArrayList<Drawable> processedArray;
    private ImageView imageView;
    private ImageButton back;
    private Button post;
    private AnimationDrawable animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //get processed array from processing activity
        processedArray = ProcessingActivity.processedImages;

        imageView = (ImageView) findViewById(R.id.imageView);
        back = (ImageButton) findViewById(R.id.back);

        animation = new AnimationDrawable();
        for(int i = 0; i < processedArray.size(); i ++){
            Drawable d = processedArray.get(i);
            animation.addFrame(d, ProcessingActivity.speedInt);
        }
        imageView.setImageDrawable(animation);
        animation.setOneShot(false);
        animation.start();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProcessingActivity.class));
                ProcessingActivity.processedImages.clear();
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        ProcessingActivity.processedImages.clear();
        finish();
    }
}
