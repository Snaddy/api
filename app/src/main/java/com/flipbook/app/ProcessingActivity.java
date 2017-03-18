package com.flipbook.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

    public static ArrayList<Bitmap> imageArray;
    private ImageView imageView;
    private SeekBar speedBar;
    private CustomAnimation animation;
    private float speed;
    private int speedInt, speedBarProg;
    private TextView speedText;
    private ImageButton normal, blackWhite, special1, special2, back, next;
    private Drawable firstImage, bwImage, special1Image, special2Image;

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
        special1 = (ImageButton) findViewById(R.id.special1);
        special2 = (ImageButton) findViewById(R.id.special2);
        back = (ImageButton) findViewById(R.id.back);
        next = (ImageButton) findViewById(R.id.next);

        speedBarProg = speedBar.getProgress() + 1;

        speedText.setText(speedBarProg + "");

        //setting buttons to show filters

        //normal filter
        firstImage = new BitmapDrawable(getResources(), imageArray.get(0));
        normal.setImageDrawable(firstImage);

        //black white filter
        bwImage = new BitmapDrawable(getResources(), imageArray.get(0));
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter m = new ColorMatrixColorFilter(matrix);
        bwImage.setColorFilter(m);
        blackWhite.setImageDrawable(bwImage);

        //special1 filter
        special1Image = new BitmapDrawable(getResources(), imageArray.get(0));
        ColorMatrix specMatrix = new ColorMatrix();
        adjustBrightness(specMatrix, 25f);
        ColorMatrixColorFilter  specMatrixColorFilter= new ColorMatrixColorFilter(specMatrix);
        special1Image.setColorFilter(specMatrixColorFilter);
        special1.setImageDrawable(special1Image);

        special2Image = new BitmapDrawable(getResources(), imageArray.get(0));
        ColorMatrix spec2Matrix = new ColorMatrix();
        setContrast(spec2Matrix, 0.2f);
        ColorMatrixColorFilter spec2MatrixColorFilter= new ColorMatrixColorFilter(spec2Matrix);
        special2Image.setColorFilter(spec2MatrixColorFilter);
        special2.setImageDrawable(special2Image);

        animation = new CustomAnimation();
        for(int i = 0; i < imageArray.size(); i ++){
            Drawable d = new BitmapDrawable(getResources(), imageArray.get(i));
            animation.addFrame(d, 1000);
        }
        animation.setOneShot(false);
        imageView.setImageDrawable(animation);
        animation.start();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CameraActivity.class));
                finish();
            }
        });

        normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setColorFilter(Color.TRANSPARENT);
            }
        });

        blackWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                ColorMatrixColorFilter m = new ColorMatrixColorFilter(matrix);
                imageView.setColorFilter(m);
            }
        });

        special1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorMatrix matrix = new ColorMatrix();
                adjustBrightness(matrix, 25f);
                ColorMatrixColorFilter m = new ColorMatrixColorFilter(matrix);
                imageView.setColorFilter(m);
            }
        });

        special2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorMatrix matrix = new ColorMatrix();
                setContrast(matrix, 0.2f);
                ColorMatrixColorFilter m = new ColorMatrixColorFilter(matrix);
                imageView.setColorFilter(m);
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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, CameraActivity.class));
        finish();
    }

    private static void setContrast(ColorMatrix cm, float contrast) {
        float scale = contrast + 1.f;
        float translate = (-.5f * scale + .5f) * 255.f;
        cm.set(new float[] {
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0 });
    }

    public static void adjustBrightness(ColorMatrix cm, float value) {
        value = cleanValue(value, 100);
        if (value == 0) {
            return;
        }
        float[] mat = new float[] {
                        1,0,0,0,value,
                        0,1,0,0,value,
                        0,0,1,0,value,
                        0,0,0,1,0,
                        0,0,0,0,1
                };
        cm.postConcat(new ColorMatrix(mat));
    }

    protected static float cleanValue(float p_val, float p_limit) {
        return Math.min(p_limit, Math.max(-p_limit, p_val));
    }

}
