package group22.quikschedule.Friends;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import group22.quikschedule.R;

/**
 * Class: Friends Fragment
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 11/7/16
 *
 * Description: Activity that utilizes fragments and Facebook SDK to connect to and authenticate
 *              with Firebase by using the Facebook login.
 *
 * @author Tynan Dewes
 * @author David Thomson
 */
public class FriendsFragment extends Fragment {

    LoginButton loginButton;
    CallbackManager callbackManager;

    private String TAG = FriendsFragment.this.getTag();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    /**
     * Description: Default constructor for FriendsFragment
     */
    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Description: Default starting method called when starting a new FriendsFragment
     * @param inflater - indicates which layer is to be brought to the front
     * @param container - indicates the current context of the layer
     * @param savedInstanceState - indicates the context from which the method was called
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(this.getContext());

        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        //Beginning FacebookSDK utilizing current context


        //Beginning Firebase connection in current context
        callbackManager = CallbackManager.Factory.create();
        mAuth = FirebaseAuth.getInstance();

        //Writing Facebook login button to screen with appropriate permissions (see XML for layout)
        loginButton = (LoginButton) view.findViewById(R.id.fb_login);
        loginButton.setReadPermissions("email", "public_profile", "user_friends");
        loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            /**
             * Description: Successful operation of the Facebook login button
             * @param loginResult - indicates the status of the Facebook login attempt
             */
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            /**
             * Description: Facebook login attempt cancelled while processing
             */
            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            /**
             * Description: Facebook login attempt failed to execute successfully
             * @param exception - used to determine what error occurred in logging in
             */
            @Override
            public void onError(FacebookException exception) {
                Log.d(TAG, "facebook:onError", exception);
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            /**
             * Description: Checking for a change to the Firebase connection, determines if
             *              logged in and securely authenticated with the Firebase
             * @param firebaseAuth - used to determine current status of Firebase connection
             */
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        return view;
    }

    /**
     * Description: Determines the current status of the activity upon competion of methods
     * @param requestCode - determines which activity is sending data
     * @param resultCode - determines matching code for receiving data
     * @param data - returned data from the launched intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Description: On the beginning of the Firebase authentication, creates a listener
     */
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    /**
     * Description: At the end of Firebase authentication, removes a listener
     */
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Description: Utilizes the Facebook SDK to read and handle an access token
     * @param token - Facebook access token with identifier to sign into Firebase using unique
     *                user information.
     */
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        //Get Firebase credentials using Facebook token
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        //Signing into Firebase
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this.getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        //For unsuccessful attempts
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(getContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}