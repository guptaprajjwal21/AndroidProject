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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends BaseActivity {

    TextInputLayout reg_email,reg_dn,reg_pass;
    Button reg_btn;
    private DatabaseReference database;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase Auth
        mAuth=FirebaseAuth.getInstance();


       // Android Fields
        reg_dn=(TextInputLayout)findViewById(R.id.reg_dn);
        reg_email=(TextInputLayout)findViewById(R.id.reg_email);
        reg_pass=(TextInputLayout)findViewById(R.id.reg_pass);
        reg_btn=(Button)findViewById(R.id.reg_btn);

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name=reg_dn.getEditText().getText().toString();
                String email=reg_email.getEditText().getText().toString();
                String password=reg_pass.getEditText().getText().toString();
                 
                register_user(display_name,email,password);



            }
        });



    }

    private void register_user(final String display_name, String email, String password) {

        if (!validateForm(display_name,email,password)) {
            return ;
        }

        showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
               if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                 final   FirebaseUser user = mAuth.getCurrentUser();
                  String user_id=user.getUid();
                   database=FirebaseDatabase.getInstance().getReference().child("Users").
                                    child(user_id);
                   String device_token = FirebaseInstanceId.getInstance().getToken();

                   HashMap<String,String> userMap=new HashMap<String, String>();
                   userMap.put("name",display_name);
                   userMap.put("status","Hi There I'm using Yatter Chat App.");
                   userMap.put("image","default");
                   userMap.put("thumb_image","default");
                   userMap.put("device_token", device_token);


                   database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {

                           if(task.isSuccessful())
                           {
                               updateUI(user);
                           }

                       }
                   });





                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]







    }

    private void updateUI(FirebaseUser user) {

        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);


    }

    private boolean validateForm(String display_name,String email,String password) {

        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            reg_email.setError("Required.");
            valid = false;
        } else {
            reg_email.setError(null);
        }
        if (TextUtils.isEmpty(password)) {
               reg_pass.setError("Required.");
            valid = false;
        } else {
            reg_pass.setError(null);
        }
        if (TextUtils.isEmpty(display_name)) {
            reg_dn.setError("Required.");
            valid = false;
        } else {
            reg_dn.setError(null);
        }


        return valid;


    }

}
