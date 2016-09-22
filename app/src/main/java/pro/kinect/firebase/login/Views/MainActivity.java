package pro.kinect.firebase.login.Views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pro.kinect.firebase.login.R;

/**
 * Created by http://kinect.pro on 22.09.16.
 * Developer Andrew.Gahov@gmail.com
 */

public class MainActivity extends AppCompatActivity {

    private View vDummy;
    private TextView tvStatus;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();

        auth = FirebaseAuth.getInstance();

    }

    private void initUi() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        vDummy = findViewById(R.id.vDummy);
        tvStatus = (TextView) findViewById(R.id.tvStatus);

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    showMessage("You signed in", true);
                    updateStatus("Signed in: " + user.getDisplayName() + " " + user.getUid());
                } else {
                    // User is signed out
                    showMessage("You signed out", true);
                    updateStatus("Signed out.");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    public void showMessage(String message, boolean isLong) {
        if (vDummy != null) {
            Snackbar.make(vDummy, message,
                    isLong ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
    }

    public void updateStatus(String newStatus) {
        if (tvStatus != null) tvStatus.setText(newStatus == null? "" : newStatus);
    }
}
