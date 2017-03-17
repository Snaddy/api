package com.flipbook.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.method.MovementMethod;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hayden on 2017-03-08.
 */

public class CameraActivity extends AppCompatActivity {

    private Camera camera;
    private CameraPreview cameraPreview;
    private ImageView imageView;
    private ToggleButton flash, switchCameras;
    private ImageButton snap, exit, next;
    private Context context = this;
    private boolean inPreview;
    private int currentCameraId;
    private LinearLayout imageArrayLayout;
    private HorizontalScrollView scrollView;
    public static ArrayList<Bitmap> imageList;
    private boolean flashOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        flashOn = false;

        flash = (ToggleButton) findViewById(R.id.flash);
        switchCameras = (ToggleButton) findViewById(R.id.switch_cameras);
        snap = (ImageButton) findViewById(R.id.snap);
        exit = (ImageButton) findViewById(R.id.exit);
        next = (ImageButton) findViewById(R.id.next);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.bringToFront();
        imageArrayLayout = (LinearLayout) findViewById(R.id.imageArray);
        scrollView = (HorizontalScrollView) findViewById(R.id.scrollView);

        imageList = new ArrayList<>();
        //camera preview
        currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        camera = cameraPreview.getCameraInstance(currentCameraId);
        if (hasFlash(camera)) {
            System.out.println("this cam has flash");
            flash.setVisibility(View.VISIBLE);
        } else {
            System.out.println("this cam doesnt have flash");
            flash.setVisibility(View.GONE);
        }
        final FrameLayout preview = (FrameLayout) findViewById(R.id.imagePreview);


        // Create our Preview view and set it as the content of our activity.
        cameraPreview = new CameraPreview(context, camera, currentCameraId);
        preview.addView(cameraPreview, 0);
        inPreview = true;


        //click listeners
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProcessingActivity.class);
                startActivity(intent);
                finish();
            }
        });

        snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("take photo!");
                Camera.Parameters p = camera.getParameters();
                if(hasFlash(camera)) {
                    if (flashOn) {
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                        camera.setParameters(p);
                    } else {
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        camera.setParameters(p);
                    }
                } else {
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(p);
                }
                try {
                    camera.takePicture(null, null, mPicture);
                } catch (RuntimeException re){
                    re.getMessage();
                }
            }
        });

        flash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (flash.isChecked()) {
                    flashOn = true;
                    System.out.println("Flash turned on");
                } else {
                    flashOn = false;
                    System.out.println("Flash turned off");
                }
            }
        });

        switchCameras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inPreview) {
                    camera.stopPreview();
                    preview.removeView(cameraPreview);
                }
                camera.release();

                //swap back to front and vice versa
                if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                } else {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                camera = cameraPreview.getCameraInstance(currentCameraId);
                if (hasFlash(camera)) {
                    System.out.println("this cam has flash");
                    flash.setVisibility(View.VISIBLE);
                } else {
                    System.out.println("this cam doesnt have flash");
                    flash.setVisibility(View.GONE);
                }
                cameraPreview = new CameraPreview(context, camera, currentCameraId);
                preview.addView(cameraPreview, 0);
            }
        });
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, final Camera camera) {
            Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Matrix m2 = new Matrix();
                m2.postRotate(270);

                if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    Matrix m = new Matrix();
                    m.preScale(-1, 1);
                    Bitmap image = Bitmap.createBitmap(picture, 0, 0, picture.getWidth(), picture.getHeight(), m2, true);
                    Bitmap flip = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getWidth(), m, true);
                    picture = Bitmap.createScaledBitmap(flip, 1200, 1200, true);
                }
                if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    Bitmap image = Bitmap.createBitmap(picture, 0, 0, picture.getWidth(), picture.getHeight(), matrix, true);
                    Bitmap croppedImage = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getWidth());
                    picture = Bitmap.createScaledBitmap(croppedImage, 1200, 1200, true);
                }
            imageList.add(picture);
            final ImageView view = new ImageView(getApplicationContext());
            final RelativeLayout layout = new RelativeLayout(getApplicationContext());
            final ImageButton close = new ImageButton(getApplicationContext());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            close.setImageResource(R.drawable.close);
            close.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            //imageview config
            view.setImageBitmap(picture);
            int imagePadding = dpToPixels(10);
            view.setScaleType(ImageView.ScaleType.FIT_START);
            view.setHapticFeedbackEnabled(false);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    snap.setEnabled(false);
                    snap.setVisibility(View.INVISIBLE);
                    BitmapDrawable drawable = (BitmapDrawable) view.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    imageView.setImageBitmap(bitmap);
                    return true;
                }
            });
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP){
                        imageView.setImageDrawable(null);
                        snap.setEnabled(true);
                        snap.setVisibility(View.VISIBLE);
                    }
                    return false;
                }
            });
            //add button and image to relative layout
            close.setLayoutParams(params);
            layout.addView(view);
            int buttonSize = dpToPixels(50);
            layout.addView(close, buttonSize, buttonSize);
            layout.setPadding(imagePadding, imagePadding, imagePadding, imagePadding);
            imageArrayLayout.addView(layout, imageArrayLayout.getHeight(), picture.getHeight());

            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageArrayLayout.removeView(layout);
                    BitmapDrawable drawable = (BitmapDrawable) view.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    imageList.remove(bitmap);
                }
            });

            scrollView.postDelayed(new Runnable() {
                public void run() {
                    scrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                }
            }, 100);
        }
    };

    public boolean hasFlash(Camera camera) {
        if (camera == null) {
            return false;
        }
        Camera.Parameters parameters = camera.getParameters();

        if (parameters.getFlashMode() == null) {
            return false;
        }
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
            return false;
        }
        return true;
    }

    public int dpToPixels(int dp){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
