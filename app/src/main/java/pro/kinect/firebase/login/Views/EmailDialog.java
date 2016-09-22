package pro.kinect.firebase.login.Views;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import pro.kinect.firebase.login.R;

/**
 * Created by http://kinect.pro on 22.09.16.
 * Developer Andrew.Gahov@gmail.com
 */

public class EmailDialog extends DialogFragment {

    private EditText etEmail;
    private EditText etPassword;

    public static DialogFragment newInstance() {
        return new EmailDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Sign in with email & password");
        builder.setView(makeView(getActivity().getLayoutInflater()));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    dialogInterface.cancel();
                } else {
                    ((MainActivity) getActivity()).sendSignIn(email, password);
                    dialogInterface.cancel();
                }
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        return builder.create();
    }

    private View makeView(LayoutInflater layoutInflater) {
        View view = layoutInflater.inflate(R.layout.email_dialog, null);
        etEmail = (EditText) view.findViewById(R.id.etEmail);
        etPassword = (EditText) view.findViewById(R.id.etPassword);
        return view;
    }
}
