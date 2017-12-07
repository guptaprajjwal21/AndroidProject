package com.example.prajjwalgupta.chatapp;

import android.app.ProgressDialog;
import android.icu.text.DateFormat;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.text.DateFormat.getDateTimeInstance;

public class ProfileActivity extends AppCompatActivity{

    private ImageView profile_imageView;
    private TextView profile_name,profile_status,profile_friendsCount;
    private Button profile_sendRequest,profile_declineRequest_btn;
    private DatabaseReference friend_request_database,friend_database,notification_database,root_ref;
    private FirebaseUser current_user;
    private String current_state;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);



        final String uid=getIntent().getStringExtra("user_id");

        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);


        notification_database=FirebaseDatabase.getInstance().getReference().child("Notifications");
        root_ref=FirebaseDatabase.getInstance().getReference();

        // databaseReference.keepSynced(true);
        friend_request_database=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        current_user= FirebaseAuth.getInstance().getCurrentUser();
        friend_database=FirebaseDatabase.getInstance().getReference().child("Friends");
        profile_imageView=(ImageView)findViewById(R.id.profile_image);
        profile_name=(TextView)findViewById(R.id.profile_displayName);
        profile_status=(TextView)findViewById(R.id.profile_status);
        profile_friendsCount=(TextView)findViewById(R.id.profile_totalFriends);
        profile_sendRequest=(Button)findViewById(R.id.profile_send_req_btn);
        profile_declineRequest_btn=(Button)findViewById(R.id.profile_decline_btn);



        current_state="not_friends";
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();



       // profile_name.setText(user_id);
         databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                  String display_name=dataSnapshot.child("name").getValue().toString();
                  String status=dataSnapshot.child("status").getValue().toString();
                  final String image=dataSnapshot.child("image").getValue().toString();

                  profile_name.setText(display_name);
                  profile_status.setText(status);


                if (!image.equals("default")) {

                    //    Picasso.with(AccountSettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(profile_image);
                    Picasso.with(ProfileActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).
                            placeholder(R.drawable.default_avatar).into(profile_imageView, new Callback() {
                        @Override
                        public void onSuccess() {



                        }

                        @Override
                        public void onError() {
                            Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(profile_imageView);

                        }
                    });


                }

                //---FRIEND LIST / REQUEST FEATURE---

                friend_request_database.child(current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(uid))
                        {
                            String req_type=dataSnapshot.child(uid).child("request_type").getValue().toString();
                            if(req_type.equals("received"))
                            {
                                current_state="req_received";
                                profile_sendRequest.setText("Accept Friend Request");
                                profile_declineRequest_btn.setVisibility(View.VISIBLE);
                                profile_declineRequest_btn.setEnabled(true);

                            }
                            else if(req_type.equals("sent"))
                            {
                            current_state="req_sent";
                            profile_sendRequest.setText("Cancel Friend Request");
                            profile_declineRequest_btn.setVisibility(View.INVISIBLE);
                                profile_declineRequest_btn.setEnabled(false);
                            }

                            mProgressDialog.dismiss();

                        }
                        else {
                            friend_database.child(current_user.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(uid))
                                    {

                                        current_state="friends";
                                        profile_sendRequest.setText("Unfriend this person");
                                        profile_declineRequest_btn.setVisibility(View.INVISIBLE);
                                        profile_declineRequest_btn.setEnabled(false);
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });


                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {



                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        profile_sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //------------NOT FRIENDS STATE------------

                if(current_state.equals("not_friends"))
                {


                    friend_request_database.child(current_user.getUid()).child(uid).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                friend_request_database.child(uid).child(current_user.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {

                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mProgressDialog.dismiss();
                                        Toast.makeText(ProfileActivity.this, "Request Sent Successfully..", Toast.LENGTH_SHORT).show();
                                        profile_sendRequest.setText("Cancel Friend Request");

                                        HashMap<String,String> notificationData=new HashMap<String, String>();
                                        notificationData.put("from",current_user.getUid());
                                        notificationData.put("type","request");

                                        //Notifications Code//
                                        notification_database.child(uid).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                current_state="req_sent";
                                                profile_declineRequest_btn.setVisibility(View.INVISIBLE);
                                                profile_declineRequest_btn.setEnabled(false);

                                            }
                                        });


                                    }
                                });

                            }
                            else{

                                Toast.makeText(ProfileActivity.this, "Failed Sending Request....", Toast.LENGTH_SHORT).show();

                            }
                            profile_sendRequest.setEnabled(true);

                        }
                    });



                }
              // ----END OF NOT FRIENDS STATE----

                // -----CANCEL REQUEST STATE-----
                if(current_state.equals("req_sent"))
                {
                    friend_request_database.child(current_user.getUid()).child(uid).
                            removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friend_request_database.child(uid).child(current_user.getUid()).
                                    removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {


                                    current_state="not_friends";
                                    profile_sendRequest.setText("Send Friend Request");
                                    profile_declineRequest_btn.setVisibility(View.INVISIBLE);
                                    profile_declineRequest_btn.setEnabled(false);


                                }
                            });


                        }
                    });

                }

                //----REQ RECEIVED STATE---
                if(current_state.equals("req_received"))
                {
                    final String current_date= java.text.DateFormat.getDateTimeInstance().format(new Date());

                    friend_database.child(current_user.getUid()).child(uid).child("date").
                            setValue(current_date).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friend_database.child(uid).child(current_user.getUid()).child("date").
                                    setValue(current_date).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    friend_request_database.child(current_user.getUid()).child(uid).
                                            removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            friend_request_database.child(uid).child(current_user.getUid()).
                                                    removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    profile_sendRequest.setEnabled(true);
                                                    current_state="friends";
                                                    profile_sendRequest.setText("Unfriend this person");
                                                    profile_declineRequest_btn.setVisibility(View.INVISIBLE);
                                                    profile_declineRequest_btn.setEnabled(false);


                                                }
                                            });


                                        }
                                    });
                                }
                            });
                        }
                 });
                }

              //Unfriend a Person


                if(current_state.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + current_user.getUid() + "/" + uid, null);
                    unfriendMap.put("Friends/" + uid + "/" + current_user.getUid(), null);

                    root_ref.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null){

                                current_state = "not_friends";
                                profile_sendRequest.setText("Send Friend Request");

                                profile_declineRequest_btn.setVisibility(View.INVISIBLE);
                                profile_declineRequest_btn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }

                            profile_sendRequest.setEnabled(true);

                        }
                    });

                }


          }
       });
    }

}
