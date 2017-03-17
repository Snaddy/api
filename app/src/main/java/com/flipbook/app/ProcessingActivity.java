package com.flipbook.app;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import static java.lang.Math.round;

/**
 * Created by Hayden on 2017-03-15.
 */

public class ProcessingActivity extends AppCompatActivity{

    private ArrayList<Bitmap> imageArray;
    private ImageView imageView;
    private SeekBar speedBar;
    private CustomAnimation animation;
    private float speed;
    private int speedInt, speedBarProg;
    private TextView speedText;
    private ImageButton normal, blackWhite, sepia, special;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        imageArray = CameraActivity.imageList;
        imageView = (ImageView) findViewById(R.id.imageView);
        speedBar = (SeekBar) findViewById(R.id.speedBar);
        speedText = (TextView) findViewById(R.id.speedText);
        normal = (ImageButton) findViewById(R.id.normal);
        blackWhite = (ImageButton) findViewById(R.id.blackWhite);
        sepia = (ImageButton) findViewById(R.id.sepia);
        special = (ImageButton) findViewById(R.id.special);


        speedBarProg = speedBar.getProgress() + 1;

        speedText.setText(speedBarProg + "");

        animation = new CustomAnimation();
        for(int i = 0; i < imageArray.size(); i ++){
            Drawable d = new BitmapDrawable(getResources(), imageArray.get(i));
            animation.addFrame(d, 1000);
        }
        animation.setOneShot(false);
        imageView.setImageDrawable(animation);
        animation.start();

        normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setColorFilter(Color.TRANSPARENT);
            }
        });

        blackWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            }
        });

        sepia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setColorFilter(Color.GRAY, PorterDuff.Mode.DARKEN);
            }
        });

        special.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
            }
        });

        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speedBarProg = speedBar.getProgress() + 1;
                speed = 1.0f/speedBarProg * 1000.0f;
                speedInt = round(speed);
                animation.setDuration(speedInt);
                speedText.setText(speedBarProg + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
