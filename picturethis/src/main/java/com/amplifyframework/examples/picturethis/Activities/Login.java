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
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amplifyframework.api.graphql.GraphQLResponse;
import com.amplifyframework.api.graphql.MutationType;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.ResultListener;
import com.amplifyframework.datastore.generated.model.User;
import com.amplifyframework.examples.picturethis.PictureThis;
import com.amplifyframework.examples.picturethis.R;

import java.util.HashMap;
import java.util.Map;

public class Login extends BaseActivity {
    private static final String TAG = Login.class.getSimpleName();
    public static final String USER_NAME_KEY = "given_name";
    public static final String USER_ID_KEY = "sub";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Start by initializing AWSMobileClient for authentication capabilities and information
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                // If the user is logged out, this section of code will be hit and trigger the drop in auth popup.
                if(userStateDetails.getUserState().equals(UserState.SIGNED_OUT)) {
                    signIn();
                } else {
                    try {
                        // If the user previously opened the app and logged in or just completed the drop in auth
                        // popup process, this section will store the user name in the app state and go to the
                        // start game screen.
                        PictureThis app = ((PictureThis)getApplicationContext());
                        Map<String, String> userAttributes = AWSMobileClient.getInstance().getUserAttributes();
                        app.setUsername(userAttributes.get(USER_NAME_KEY));
                        app.setUserId(userAttributes.get(USER_ID_KEY));

                        User user = User.builder().username(userAttributes.get(USER_NAME_KEY)).build();

                        Amplify.API.mutate(
                                "mygraphql",
                                user,
                                MutationType.CREATE,
                                new ResultListener<GraphQLResponse<User>>() {
                                    @Override
                                    public void onResult(GraphQLResponse<User> result) {
                                        goToStartGame();
                                    }

                                    @Override
                                    public void onError(Throwable error) {

                                    }
                                }
                        );
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to retrieve user attributes");
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Initialization error.", e);
            }
        });
    }

    /**
     * Triggers the drop in auth popup which handles user registration/login/forgot password
     */
    private void signIn() {
        try {
            // By default, this will reload the current view after sign in is complete.
            // You could also include a SignInUIOptions object to specify a view to go to.
            AWSMobileClient.getInstance().showSignIn(
                    this,
                    SignInUIOptions.builder().logo(R.drawable.picturethis_logo).backgroundColor(R.color.colorPrimaryDark).build()
            );
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void goToStartGame() {
        Intent intent = new Intent(getApplicationContext(), StartGame.class);
        startActivity(intent);
    }
}
