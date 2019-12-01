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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignOutOptions;
import com.amplifyframework.examples.picturethis.PictureThis;
import com.amplifyframework.examples.picturethis.R;

public class StartGame extends BaseActivity {
    private static final String TAG = StartGame.class.getSimpleName();
    private TextView logout;
    private Button startGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);
        logout = findViewById(R.id.logoutLink);
        startGame = findViewById(R.id.startGameButton);

        PictureThis app = ((PictureThis)getApplicationContext());
        logout.setText(getString(R.string.logout_button) + " " + app.getUsername());

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AWSMobileClient.getInstance().signOut(SignOutOptions.builder().build(), new Callback<Void>() {
                    @Override
                    public void onResult(Void result) {
                        startActivity(new Intent(getApplicationContext(), Login.class));
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                });
            }
        });

        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SendPhoto.class));
            }
        });
    }
}
