/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.amplifyframework.examples.picturethis.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.ResultListener;
import com.amplifyframework.examples.picturethis.R;
import com.amplifyframework.storage.StorageAccessLevel;
import com.amplifyframework.storage.options.StorageDownloadFileOptions;
import com.amplifyframework.storage.options.StorageListOptions;
import com.amplifyframework.storage.options.StorageRemoveOptions;
import com.amplifyframework.storage.result.StorageDownloadFileResult;
import com.amplifyframework.storage.result.StorageListResult;
import com.amplifyframework.storage.result.StorageRemoveResult;

public class GuessClue extends BaseActivity {
    public static final String DOWNLOAD_PHOTO_NAME = "received.jpg";
    private static final String TAG = GuessClue.class.getSimpleName();
    private static final String otherUser = "us-east-1:0777727e-9e09-4186-9035-718231015504";
    private ImageView photoPreview;
    private Button submitGuess;
    private TextView quitGame;
    private View loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_clue);

        photoPreview = findViewById(R.id.photoPreview);
        submitGuess = findViewById(R.id.sendGuessButton);
        quitGame = findViewById(R.id.quitGameLink);
        loadingSpinner = findViewById(R.id.progressOverlay);

        loadingSpinner.setVisibility(View.VISIBLE);

        Amplify.Storage.list(
            "",
            StorageListOptions
                    .builder()
                    .accessLevel(StorageAccessLevel.PROTECTED)
                    .targetIdentityId(otherUser)
                    .build(),
            new ResultListener<StorageListResult>() {
                @Override
                public void onResult(StorageListResult result) {
                    if(result.getItems().size() > 0) {
                        String fullPhotoPath = result.getItems().get(0).getKey();
                        downloadFile(fullPhotoPath.substring(fullPhotoPath.lastIndexOf('/')+1));
                    } else {
                        loadingSpinner.setVisibility(View.GONE);
                        toast("No file found to download", Toast.LENGTH_LONG);
                    }
                }

                /**
                 * Called if a result cannot be obtained, because an
                 * error has occurred.
                 *
                 * @param error An error that prevents determination of a result.
                 */
                @Override
                public void onError(Throwable error) {
                    loadingSpinner.setVisibility(View.GONE);
                    Log.e(TAG, error.getStackTrace().toString());
                }
            }
        );

        submitGuess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Amplify.Storage.remove(
                        SendPhoto.UPLOAD_PHOTO_NAME,
                        StorageRemoveOptions.builder().accessLevel(StorageAccessLevel.PROTECTED).build(),
                        new ResultListener<StorageRemoveResult>() {
                            @Override
                            public void onResult(StorageRemoveResult result) {
                                toast("Successfully deleted your upload", Toast.LENGTH_LONG);
                            }

                            @Override
                            public void onError(Throwable error) {
                                Log.e(TAG, error.getStackTrace().toString());
                            }
                });
            }
        });

        quitGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), StartGame.class));
            }
        });
    }

    private void downloadFile(String path) {
        Amplify.Storage.downloadFile(
                path,
                getExternalFilesDir(Environment.DIRECTORY_PICTURES) + DOWNLOAD_PHOTO_NAME,
                StorageDownloadFileOptions
                        .builder()
                        .accessLevel(StorageAccessLevel.PROTECTED)
                        .targetIdentityId(otherUser)
                        .build(),
                new ResultListener<StorageDownloadFileResult>() {

                    /**
                     * Listener method for reporting success
                     * of an operation.
                     *
                     * @param result represents the object for success
                     */
                    @Override
                    public void onResult(StorageDownloadFileResult result) {
                        photoPreview.setImageURI(android.net.Uri.parse(result.getFile().toURI().toString()));
                        loadingSpinner.setVisibility(View.GONE);
                    }

                    /**
                     * Listener method for reporting failure
                     * of an operation.
                     *
                     * @param error The error that occurred
                     */
                    @Override
                    public void onError(Throwable error) {
                        loadingSpinner.setVisibility(View.GONE);
                        Log.e(TAG, error.getMessage());
                    }
                }
        );
    }

    private void toast(final String message, final int length) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), message, length).show();
            }
        });
    }
}
