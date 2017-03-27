package com.flipbook.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
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
    public static ArrayList<Drawable> processedImages;
    private ImageView imageView;
    private SeekBar speedBar, saturation, contrast, brightness;
    private CustomAnimation animation;
    private float speed;

    private float saturationFilter;
    private float contrastFilter;
    private float brightnessFilter;
    private int speedBarProg;
    public static int speedInt;
    private TextView speedText, saturationText, contrastText, brightnessText;
    private ImageButton back, next;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        imageArray = CameraActivity.imageList;
        processedImages = new ArrayList<>();
        imageView = (ImageView) findViewById(R.id.imageView);
        speedBar = (SeekBar) findViewById(R.id.speedBar);
        speedText = (TextView) findViewById(R.id.speedText);
        saturationText = (TextView) findViewById(R.id.saturationText);
        contrastText = (TextView) findViewById(R.id.contrastText);
        brightnessText = (TextView) findViewById(R.id.brightnessText);
        back = (ImageButton) findViewById(R.id.back);
        next = (ImageButton) findViewById(R.id.next);
        saturation = (SeekBar) findViewById(R.id.saturation);
        contrast = (SeekBar) findViewById(R.id.contrast);
        brightness = (SeekBar) findViewById(R.id.brightness);

        speedBarProg = speedBar.getProgress() + 1;
        speedInt = round(speedBarProg);

        speedText.setText("Speed:  " + (speedBarProg - 1));

        saturationFilter = saturation.getProgress() / 100;
        contrastFilter = contrast.getProgress() / 400 - 0.25f;
        brightnessFilter = brightness.getProgress()/4 - 25;

        saturationText.setText("Saturation:  " + (saturation.getProgress() - 100) + "");
        contrastText.setText("Contrast:  " + (contrast.getProgress() - 100) + "");
        brightnessText.setText("Brightness:  " + (brightness.getProgress() - 100) + "");

        animation = new CustomAnimation();
        for(int i = 0; i < imageArray.size(); i ++){
            Drawable d = new BitmapDrawable(getResources(), imageArray.get(i));
            animation.addFrame(d, 33);
        }
        animation.setOneShot(false);
        imageView.setImageDrawable(animation);
        animation.start();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(getSaturationFilter());
                setContrast(matrix, getContrastFilter());
                ColorMatrixColorFilter m = new ColorMatrixColorFilter(matrix);
                adjustBrightness(matrix, getBrightnessFilter());
                for(int i = 0; i < imageArray.size(); i ++){
                    Drawable d = new BitmapDrawable(getResources(), imageArray.get(i));
                    d.setColorFilter(m);
                    processedImages.add(d);
                }
                startActivity(new Intent(getApplicationContext(), PostActivity.class));
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CameraActivity.class));
                finish();
            }
        });

        saturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation((float)saturation.getProgress() / 100);
                setContrast(matrix, getContrastFilter());
                adjustBrightness(matrix, getBrightnessFilter());
                ColorMatrixColorFilter m = new ColorMatrixColorFilter(matrix);
                imageView.setColorFilter(m);
                setSaturationFilter((float)saturation.getProgress() / 100);
                saturationText.setText("Saturation:  " + (saturation.getProgress() - 100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        contrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(getSaturationFilter());
                setContrast(matrix, (float) contrast.getProgress() / 400 - 0.25f);
                adjustBrightness(matrix, getBrightnessFilter());
                ColorMatrixColorFilter m = new ColorMatrixColorFilter(matrix);
                imageView.setColorFilter(m);
                setContrastFilter((float) contrast.getProgress() / 400 - 0.25f);
                contrastText.setText("Contrast:  " + (contrast.getProgress() - 100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(getSaturationFilter());
                setContrast(matrix, getContrastFilter());
                adjustBrightness(matrix, brightness.getProgress()/4 - 25);
                ColorMatrixColorFilter m = new ColorMatrixColorFilter(matrix);
                imageView.setColorFilter(m);
                setBrightnessFilter(brightness.getProgress() / 4 - 25);
                brightnessText.setText("Brightness: " + (brightness.getProgress() - 100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speedBarProg = speedBar.getProgress() + 1;
                speed = 1.0f/speedBarProg * 1000.0f;
                speedInt = round(speed);
                animation.setDuration(speedInt);
                speedText.setText("Speed:  " + speedBarProg);
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
        float[] mat = new float[] {
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0 };
        cm.postConcat(new ColorMatrix(mat));
        System.out.println("contrast: " + scale + "\n" + "translate: " + translate);
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
        System.out.println("brightness: " + value);
    }

    protected static float cleanValue(float p_val, float p_limit) {
        return Math.min(p_limit, Math.max(-p_limit, p_val));
    }

    public float getSaturationFilter() {
        return saturationFilter;
    }

    public void setSaturationFilter(float saturationFilter) {
        this.saturationFilter = saturationFilter;
    }

    public float getContrastFilter() {
        return contrastFilter;
    }

    public void setContrastFilter(float contrastFilter) {
        this.contrastFilter = contrastFilter;
    }

    public float getBrightnessFilter() {
        return brightnessFilter;
    }

    public void setBrightnessFilter(float brightnessFilter) {
        this.brightnessFilter = brightnessFilter;
    }
}