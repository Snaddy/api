package com.flipbook.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hayden on 2017-03-08.
 */

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "APP" ;
    private Camera camera;
    private CameraPreview cameraPreview;
    private ImageView imageView;
    private ToggleButton flash, switchCameras;
    private ImageButton snap, close;
    private Context context = this;
    private boolean inPreview;
    private int currentCameraId;
    private LinearLayout imageArrayLayout;
    private HorizontalScrollView scrollView;
    private ArrayList<Bitmap> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        flash = (ToggleButton) findViewById(R.id.flash);
        switchCameras = (ToggleButton) findViewById(R.id.switch_cameras);
        snap = (ImageButton) findViewById(R.id.snap);
        close = (ImageButton) findViewById(R.id.close);
        imageView = (ImageView) findViewById(R.id.imageView);
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
        snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("take photo!");
                try {
                    camera.takePicture(null, null, mPicture);
                } catch (RuntimeException re){
                    re.getMessage();
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
                cameraPreview = new CameraPreview(context, camera, currentCameraId);
                preview.addView(cameraPreview, 0);
                if (hasFlash(camera)) {
                    System.out.println("this cam has flash");
                    flash.setVisibility(View.VISIBLE);
                } else {
                    System.out.println("this cam doesnt have flash");
                    flash.setVisibility(View.GONE);
                }
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close.setEnabled(false);
                close.setVisibility(View.GONE);
                imageView.setImageDrawable(null);
                snap.setEnabled(true);
                snap.setVisibility(View.VISIBLE);
                if(hasFlash(camera)){
                    flash.setEnabled(true);
                    flash.setVisibility(View.VISIBLE);
                }
                switchCameras.setEnabled(true);
                switchCameras.setVisibility(View.VISIBLE);
            }
        });
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);

                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Matrix m2 = new Matrix();
                m2.postRotate(270);
                Matrix m = new Matrix();
                m.preScale(-1, 1);

                if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    Bitmap image = Bitmap.createBitmap(picture, 0, 0, picture.getWidth(), picture.getHeight(), m2, true);
                    Bitmap flip = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getWidth(), m, true);
                    picture = Bitmap.createScaledBitmap(flip, 1600, 1600, true);
                }
                if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    Bitmap image = Bitmap.createBitmap(picture, 0, 0, picture.getWidth(), picture.getHeight(), matrix, true);
                    Bitmap croppedImage = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getWidth());
                    picture = Bitmap.createScaledBitmap(croppedImage, 1200, 1200, true);
                }
            imageList.add(picture);
            final ImageView view = new ImageView(getApplicationContext());
            view.setScaleType(ImageView.ScaleType.FIT_START);
            view.setImageBitmap(picture);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snap.setEnabled(false);
                    snap.setVisibility(View.INVISIBLE);
                    flash.setEnabled(false);
                    flash.setVisibility(View.GONE);
                    switchCameras.setEnabled(false);
                    switchCameras.setVisibility(View.GONE);
                    close.setVisibility(View.VISIBLE);
                    close.setEnabled(true);
                    BitmapDrawable drawable = (BitmapDrawable) view.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    imageView.setImageBitmap(bitmap);
                }
            });
            imageArrayLayout.addView(view, imageArrayLayout.getHeight(), picture.getHeight());

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
}
