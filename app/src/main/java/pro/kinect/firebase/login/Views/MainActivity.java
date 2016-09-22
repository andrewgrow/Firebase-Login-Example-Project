package pro.kinect.firebase.login.Views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pro.kinect.firebase.login.R;

/**
 * Created by http://kinect.pro on 22.09.16.
 * Developer Andrew.Gahov@gmail.com
 */

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private boolean isSignedIn = false; //it is the simplest method keep information about signed in

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = (TextView) findViewById(R.id.tvStatus);

        auth = FirebaseAuth.getInstance();
        //create a listener of auth events
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    loginStatusMessage("Signed in: "
                            + "user.getDisplayName() " + user.getDisplayName() + " \n"
                            + "user.getUid() " + user.getUid() + " \n"
                            + "user.getProviderId() " + user.getProviderId() + " \n"
                    );
                    isSignedIn = true;
                } else {
                    loginStatusMessage("Signed out.");
                    isSignedIn = false;
                }
            }
        };
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

    public void loginStatusMessage(String newStatus) {
        if (tvStatus != null) tvStatus.setText(newStatus == null ? "" : newStatus);
    }


    //handler of click on buttons
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSignInWithEmail:
                signInWithEmail();
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





    ///// --------  THE BLOCK OF SIGN IN WITH EMAIL STARTED ---------- //////
    //show dialog about sign in
    private void signInWithEmail() {
        if (isSignedIn) return;
        DialogFragment emailDialog = EmailDialog.newInstance();
        emailDialog.show(getSupportFragmentManager(), "emailDialogFragment");
    }

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
                            loginStatusMessage("Your password is wrong or " +
                                    task.getException().getMessage());
                        } else isSignedIn = true;
                    }
                });
    }
    ///// --------  THE BLOCK OF SIGN IN WITH EMAIL FINISHED ---------- //////
}
