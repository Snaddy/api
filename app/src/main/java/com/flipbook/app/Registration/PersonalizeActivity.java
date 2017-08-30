package com.flipbook.app.Registration;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.flipbook.app.R;

import java.io.IOException;

/**
 * Created by Hayden on 2017-08-21.
 */

public class PersonalizeActivity extends AppCompatActivity{

    private int PICK_IMAGE_REQUEST = 1;
    private Bitmap centeredBitmap, resizedBitmap;
    private ImageView imageView;
    private EditText name, bio;
    private RelativeLayout changePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personalize);
        imageView = (ImageView) findViewById(R.id.imageView);
        name = (EditText) findViewById(R.id.name);
        bio = (EditText) findViewById(R.id.bio);
        changePic = (RelativeLayout) findViewById(R.id.changePicture);

        changePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

            String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
            Cursor cur = managedQuery(uri, orientationColumn, null, null, null);
            int orientation = -1;
            if (cur != null && cur.moveToFirst()) {
                orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
            }
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                //center bitmap image
                if (bitmap.getWidth() >= bitmap.getHeight()){
                    centeredBitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth()/2 - bitmap.getHeight()/2, 0, bitmap.getHeight(), bitmap.getHeight(), matrix, true);
                }else{
                    centeredBitmap = Bitmap.createBitmap(bitmap, 0, bitmap.getHeight()/2 - bitmap.getWidth()/2, bitmap.getWidth(), bitmap.getWidth(), matrix, true);
                }

                Bitmap resizedBitmap;

                imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(centeredBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
