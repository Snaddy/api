package com.flipbook.app.Posts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.flipbook.app.Camera.CameraActivity;
import com.flipbook.app.R;

import java.util.ArrayList;

import static java.lang.Math.round;

/**
 * Created by Hayden on 2017-03-15.
 */

public class ProcessingActivity extends AppCompatActivity{

    public static ArrayList<Bitmap> imageArray, processedImages;
    private ImageView imageView;
    private SeekBar speedBar, saturation, contrast, brightness;
    private CustomAnimation animation;
    private float speed, saturationFilter, contrastFilter, brightnessFilter;
    public static int speedInt, speedBarProg;
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
        contrastFilter = contrast.getProgress() / 400;
        brightnessFilter = brightness.getProgress()/4 - 25;

        saturationText.setText("Saturation:  " + (saturation.getProgress() - 100) + "");
        contrastText.setText("Contrast:  " + (contrast.getProgress() - 100) + "");
        brightnessText.setText("Brightness:  " + (brightness.getProgress() - 100) + "");

        System.out.println(getBrightnessFilter());
        System.out.println(getContrastFilter());
        System.out.println(getBrightnessFilter());

        animation = new CustomAnimation();
        for(int i = 0; i < imageArray.size(); i ++){
            Drawable d = new BitmapDrawable(getResources(), imageArray.get(i));
            //30fps
            animation.addFrame(d, 33);
        }
        animation.setOneShot(false);
        imageView.setImageDrawable(animation);
        animation.start();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < imageArray.size(); i ++){
                    processedImages.add(processBitmap(imageArray.get(i)));
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
                setContrast(matrix, (float) contrast.getProgress() / 400 - .25f);
                adjustBrightness(matrix, getBrightnessFilter());
                ColorMatrixColorFilter m = new ColorMatrixColorFilter(matrix);
                imageView.setColorFilter(m);
                setContrastFilter((float) contrast.getProgress() / 400 - .25f);
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

    public Bitmap processBitmap(Bitmap bmpOriginal) {
        Bitmap bmp = Bitmap.createBitmap(bmpOriginal.getWidth(),
                bmpOriginal.getWidth(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint paint = new Paint();
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(getSaturationFilter());
        setContrast(matrix, getContrastFilter());
        adjustBrightness(matrix, getBrightnessFilter());
        ColorMatrixColorFilter m = new ColorMatrixColorFilter(matrix);
        paint.setColorFilter(m);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmp;
    }
}
