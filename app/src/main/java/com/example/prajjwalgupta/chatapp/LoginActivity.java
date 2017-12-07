package com.example.prajjwalgupta.chatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends BaseActivity{

    TextInputLayout log_email,log_pass;
    Button btn_login;
    private FirebaseAuth mAuth;
    private AwesomeValidation awesomeValidation;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

         mAuth=FirebaseAuth.getInstance();
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");

        log_email=(TextInputLayout)findViewById(R.id.email_log);
        log_pass=(TextInputLayout)findViewById(R.id.pass_log);

        btn_login=(Button)findViewById(R.id.btn_log);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email=log_email.getEditText().getText().toString();
                String password=log_pass.getEditText().getText().toString();

                login_user(email,password);

            }
        });




    }

    private void login_user(String email, String password) {

        if (!validateForm(email,password)) {
            return ;
        }

        showProgressDialog();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            String user_id=mAuth.getCurrentUser().getUid();
                            String deviceToken= FirebaseInstanceId.getInstance().getToken();

                            databaseReference.child(user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);

                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                           // updateUI(null);
                        }
                      /*
                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            mStatusTextView.setText(R.string.auth_failed);
                        }

                      */
                        hideProgressDialog();

                    }
                });
        // [END sign_in_with_email]
    }

    private void updateUI(FirebaseUser user) {



        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);




    }



    private boolean validateForm(String email, String password) {
        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            log_email.setError("This is Required.");
            valid = false;
        } else {
            log_email.setError(null);
        }
        if (TextUtils.isEmpty(password)) {
            log_pass.setError("This is Required.");
            valid = false;
        } else {
            log_pass.setError(null);
        }


        return valid;
    }
}
