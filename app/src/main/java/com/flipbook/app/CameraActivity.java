package com.flipbook.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private ImageButton snap;
    private Context context = this;
    private boolean inPreview;
    private int currentCameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        flash = (ToggleButton) findViewById(R.id.flash);
        switchCameras = (ToggleButton) findViewById(R.id.switch_cameras);
        snap = (ImageButton) findViewById(R.id.snap);
        imageView = (ImageView) findViewById(R.id.imageView);
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
        cameraPreview = new CameraPreview(context, camera);
        preview.addView(cameraPreview, 0);
        inPreview = true;

        //click listeners
        snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("take photo!");
                camera.takePicture(null, null, mPicture);
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
                cameraPreview = new CameraPreview(context, camera);
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
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Matrix matrix = new Matrix();
            Matrix m = new Matrix();

            matrix.postRotate(270);
            m.preScale(-1,1);

            Bitmap pictureFile = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap image = Bitmap.createBitmap(pictureFile, 0, 0, pictureFile.getHeight(), pictureFile.getHeight(), matrix, true);
            Bitmap image2 = Bitmap.createBitmap(image, 0, 0, image.getHeight(), image.getHeight(), m, true);
            Bitmap resizedFile = Bitmap.createScaledBitmap(image2, 1600, 1600,true);

            imageView.setImageBitmap(resizedFile);
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
