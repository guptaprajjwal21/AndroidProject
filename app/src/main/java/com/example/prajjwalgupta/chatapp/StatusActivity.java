package com.example.prajjwalgupta.chatapp;

import android.app.ProgressDialog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StatusActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout status_input;
    private Button change_status;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private EditText input_et_status;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid=firebaseUser.getUid();

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);


        toolbar=(Toolbar)findViewById(R.id.status_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        status_input=(TextInputLayout)findViewById(R.id.status_input);
        change_status=(Button)findViewById(R.id.btn_save_changes);
        input_et_status=(EditText)findViewById(R.id.input_et_status);


        String status_value=getIntent().getStringExtra("status");

        status_input.getEditText().setText(status_value);



        change_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* not working
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String status=dataSnapshot.child("status").getValue().toString();
                        input_et_status.setText(status);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                */

                progressDialog=new ProgressDialog(StatusActivity.this);
                progressDialog.setTitle("Saving Changes");
                progressDialog.setMessage("Please Wait while we save the Changes");
                progressDialog.show();





                String status=status_input.getEditText().getText().toString();

                databaseReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            progressDialog.dismiss();
                        }
                        else
                        {
                            Toast.makeText(StatusActivity.this, "There was some error in saving changes", Toast.LENGTH_LONG).
                                    show();
                            
                        }



                    }
                });







            }
        });






    }
}
