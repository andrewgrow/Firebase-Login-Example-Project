package pro.kinect.firebase.login.Views;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import pro.kinect.firebase.login.R;

/**
 * Created by http://kinect.pro on 22.09.16.
 * Developer Andrew.Gahov@gmail.com
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GOOGLE_REQUEST = 101;
    private TextView tvStatus;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private boolean isSignedIn = false; //it is the simplest method keep information about signed in
    private LoginButton btnFacebook;
    private CallbackManager facebookCallbackManager;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = (TextView) findViewById(R.id.tvStatus);
        btnFacebook = (LoginButton) findViewById(R.id.btnFacebook);
        findViewById(R.id.btnGoogle).setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        isSignedIn = FirebaseAuth.getInstance().getCurrentUser() != null; //true = User is signed in
        facebookCallbackManager = CallbackManager.Factory.create();

        initEntities();
    }

    private void initEntities() {
        // ----- For sign in with email ----- //
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    signInStatusMessage("Signed in: "
                            + "user.getDisplayName() " + user.getDisplayName() + " \n"
                            + "user.getUid() " + user.getUid() + " \n"
                            + "user.getProviderId() " + user.getProviderId() + " \n"
                    );
                    isSignedIn = true;
                } else {
                    signInStatusMessage("Signed out.");
                    isSignedIn = false;
                }
            }
        };


        // ----- For sign in with facebook ----- //
        btnFacebook.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>(){
            @Override
            public void onSuccess(LoginResult loginResult) {
                isSignedIn = true;
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                isSignedIn = false;
            }

            @Override
            public void onError(FacebookException error) {
                isSignedIn = false;
            }
        });

        // ----- For sign OUT with facebook ----- //
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken2) {
                if (accessToken2 == null)  signOut();
            }
        };



        // ----- For sign in with google ----- //
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        // nothing
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    //here we adding the listener
    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    //the listener will be removed
    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null) auth.removeAuthStateListener(authListener);
    }

    //show messages on the Phone's Screen
    public void signInStatusMessage(String newStatus) {
        if (tvStatus != null) tvStatus.setText(newStatus == null ? "" : newStatus);
    }


    //handler of click on buttons
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSignInWithEmail:
                if (isSignedIn) return;
                //start email sign-in dialog
                DialogFragment emailDialog = EmailDialog.newInstance();
                emailDialog.show(getSupportFragmentManager(), "emailDialogFragment");
                break;
            case R.id.btnGoogle:
                //start Google sign-in activity
                if (isSignedIn) return;
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, GOOGLE_REQUEST);
                break;
            case R.id.btnSignOut :
                signOut();
                break;
            default:
                break;
        }
    }

    //for all providers
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        isSignedIn = false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_REQUEST) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                isSignedIn = false;
                signOut();
            }
        } else {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }



    ///// --------  THE BLOCK OF SIGN IN WITH EMAIL STARTED ---------- //////
    //the first we will try sign in
    public void sendSignIn(final String email, final String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            sendRegistration(email, password);
                        } else isSignedIn = true;
                    }
                });
    }

    //if we have error after sign in we will try register
    private void sendRegistration(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            signInStatusMessage("Your password is wrong or " +
                                    task.getException().getMessage());
                            isSignedIn = false;
                        } else isSignedIn = true;
                    }
                });
    }
    ///// --------  THE BLOCK OF SIGN IN WITH EMAIL FINISHED ---------- //////





    ///// --------  THE BLOCK OF SIGN IN WITH FACEBOOK STARTED ---------- //////
    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            signInStatusMessage("Facebook authentication failed.");
                            signOut();
                        }
                    }
                });
    }
    ///// --------  THE BLOCK OF SIGN IN WITH FACEBOOK FINISHED ---------- //////





    ///// --------  THE BLOCK OF SIGN IN WITH GOOGLE STARTED ---------- //////
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            signInStatusMessage("Google authentication failed.");
                            signOut();
                        }
                    }
                });
    }
    ///// --------  THE BLOCK OF SIGN IN WITH GOOGLE FINISHED ---------- //////
}
