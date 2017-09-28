package com.flipbook.app.Camera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.flipbook.app.Posts.ProcessingActivity;
import com.flipbook.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hayden on 2017-03-08.
 */

public class CameraActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;

    private Camera camera;
    private CameraPreview cameraPreview;
    private ImageView imageView;
    private FrameLayout preview;
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
        preview = (FrameLayout) findViewById(R.id.imagePreview);
        imageView = (ImageView) findViewById(R.id.editAvatar);
        imageView.bringToFront();
        imageArrayLayout = (LinearLayout) findViewById(R.id.imageArray);
        scrollView = (HorizontalScrollView) findViewById(R.id.scrollView);

        imageList = new ArrayList<>();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //marshmallow camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            //camera preview
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            camera = cameraPreview.getCameraInstance(currentCameraId);
            cameraPreview = new CameraPreview(context, camera, currentCameraId);
            preview.addView(cameraPreview, 0);
            inPreview = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }

        if (hasFlash(camera)) {
            System.out.println("this cam has flash");
            flash.setVisibility(View.VISIBLE);
        } else {
            System.out.println("this cam doesnt have flash");
            flash.setVisibility(View.GONE);
        }

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
                if(imageList.size() <= 1){
                    LayoutInflater inflater = CameraActivity.this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog, null);
                    builder.setView(dialogView);
                    TextView title = (TextView) dialogView.findViewById(R.id.title);
                    TextView message = (TextView) dialogView.findViewById(R.id.message);
                    Button ok = (Button) dialogView.findViewById(R.id.okButton);
                    title.setText("Uh oh! Error...");
                    message.setText("Please make sure your post contains 2 or more photos");
                    final AlertDialog alertDialog = builder.create();
                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.cancel();
                        }
                    });
                    alertDialog.show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), ProcessingActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (imageList.size() > 60) {
                    LayoutInflater inflater = CameraActivity.this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog, null);
                    builder.setView(dialogView);
                    TextView title = (TextView) dialogView.findViewById(R.id.title);
                    TextView message = (TextView) dialogView.findViewById(R.id.message);
                    Button ok = (Button) dialogView.findViewById(R.id.okButton);
                    title.setText("Uh oh! Error...");
                    message.setText("Please make sure your post does not contain more than 60 photos");
                    final AlertDialog alertDialog = builder.create();
                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.cancel();
                        }
                    });
                    alertDialog.show();
                } else {
                    Camera.Parameters p = camera.getParameters();
                    if (hasFlash(camera)) {
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
                    } catch (RuntimeException re) {
                        re.getMessage();
                    }
                }
            }
        });

        flash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (flash.isChecked()) {
                    flashOn = true;
                } else {
                    flashOn = false;
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
                    flash.setVisibility(View.VISIBLE);
                } else {
                    flash.setVisibility(View.GONE);
                }
                cameraPreview = new CameraPreview(context, camera, currentCameraId);
                preview.addView(cameraPreview, 0);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //camera preview
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    camera = cameraPreview.getCameraInstance(currentCameraId);
                    cameraPreview = new CameraPreview(context, camera, currentCameraId);
                    preview.addView(cameraPreview, 0);
                    inPreview = true;

                } else {
                    System.out.println("Permissions --> " + "Permission Denied: ");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(camera == null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                //camera preview
                currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                camera = cameraPreview.getCameraInstance(currentCameraId);
                cameraPreview = new CameraPreview(context, camera, currentCameraId);
                preview.addView(cameraPreview, 0);
                inPreview = true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, final Camera camera) {
            camera.startPreview();
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
                    picture = Bitmap.createScaledBitmap(flip, 1080, 1080, true);
                }
                if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    Bitmap image = Bitmap.createBitmap(picture, 0, 0, picture.getWidth(), picture.getHeight(), matrix, true);
                    Bitmap croppedImage = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getWidth());
                    picture = Bitmap.createScaledBitmap(croppedImage, 1080, 1080, true);
                }
            imageList.add(picture);
            final ImageView view = new ImageView(getApplicationContext());
            final RelativeLayout layout = new RelativeLayout(getApplicationContext());
            final ImageButton close = new ImageButton(getApplicationContext());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            close.setImageResource(R.drawable.close);
            close.setScaleType(ImageView.ScaleType.FIT_CENTER);
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
            layout.addView(close, imageArrayLayout.getHeight() / 4, imageArrayLayout.getHeight() / 4);
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
