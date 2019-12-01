/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.amplifyframework.examples.picturethis.Activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.ResultListener;
import com.amplifyframework.examples.picturethis.BuildConfig;
import com.amplifyframework.examples.picturethis.R;
import com.amplifyframework.storage.StorageAccessLevel;
import com.amplifyframework.storage.options.StorageUploadFileOptions;
import com.amplifyframework.storage.result.StorageUploadFileResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class SendPhoto extends BaseActivity {
    public static final String UPLOAD_PHOTO_NAME = "clue.jpg";
    private static final String TAG = SendPhoto.class.getSimpleName();
    private static final int GALLERY_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 1;
    private TextView quitGame;
    private Button choosePhotoButton;
    private Button takePhotoButton;
    private Button sendPhotoButton;
    private File photoFile;
    private ImageView photoPreview;
    private View loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_photo);

        quitGame = findViewById(R.id.quitGameLink);
        choosePhotoButton = findViewById(R.id.choosePhotoButton);
        takePhotoButton = findViewById(R.id.takePhotoButton);
        photoPreview = findViewById(R.id.photoPreview);
        loadingSpinner = findViewById(R.id.progressOverlay);
        sendPhotoButton = findViewById(R.id.sendPhotoButton);
        photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + UPLOAD_PHOTO_NAME);

        sendPhotoButton.setVisibility(View.GONE);

        quitGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), StartGame.class));
            }
        });
        choosePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromGallery();
            }
        });
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureFromCamera();
            }
        });
        sendPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), GuessClue.class));
            }
        });
    }

    private void pickFromGallery() {
        // Create an Intent with action as ACTION_PICK
        Intent intent = new Intent(Intent.ACTION_PICK);

        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");

        // We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);

        // Launching the Intent
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    private void captureFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile));
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    // data.getData returns the content URI for the selected Image
                    Uri selectedImage = data.getData();
                    try {
                        Files.copy(getContentResolver().openInputStream(selectedImage), photoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        uploadFile(photoFile.getAbsolutePath());
                    } catch (Exception error) {
                        Log.e(TAG, error.getMessage());
                    }
                    break;
                case CAMERA_REQUEST_CODE:
                    uploadFile(photoFile.getAbsolutePath());
                    break;
            }
        }
    }

    private void uploadFile(String path) {
        loadingSpinner.setVisibility(View.VISIBLE);

        Amplify.Storage.uploadFile(
                UPLOAD_PHOTO_NAME,
                path,
                StorageUploadFileOptions.builder().accessLevel(StorageAccessLevel.PROTECTED).build(),
                new ResultListener<StorageUploadFileResult>() {
                    @Override
                    public void onResult(StorageUploadFileResult result) {
                        photoPreview.setImageURI(null); // Forces a redraw of the image view
                        photoPreview.setImageURI(android.net.Uri.parse(photoFile.toURI().toString()));
                        photoPreview.setVisibility(View.VISIBLE);
                        sendPhotoButton.setVisibility(View.VISIBLE);
                        loadingSpinner.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG, error.getMessage());
                        loadingSpinner.setVisibility(View.GONE);
                    }
            }
        );
    }
}
