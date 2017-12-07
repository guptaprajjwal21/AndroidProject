package com.example.prajjwalgupta.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSettingsActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;
    CircleImageView profile_image;
    private static final int GALLERY_PICK = 1;
    TextView settings_display_name;
    TextView settings_status;
    Button settings_change_image, settings_change_status;
    private ProgressDialog progressDialog;
    private StorageReference image_storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        profile_image = (CircleImageView) findViewById(R.id.profile_image);
        settings_display_name = (TextView) findViewById(R.id.settings_display_name);
        settings_status = (TextView) findViewById(R.id.settings_status);
        settings_change_status = (Button) findViewById(R.id.settings_status_btn);
        settings_change_image = (Button) findViewById(R.id.settings_image_btn);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = firebaseUser.getUid();

        image_storage = FirebaseStorage.getInstance().getReference();


        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        databaseReference.keepSynced(true);


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
              final  String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();


                settings_display_name.setText(name);
                settings_status.setText(status);

                if (!image.equals("default")) {

                //    Picasso.with(AccountSettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(profile_image);
                    Picasso.with(AccountSettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).
                            placeholder(R.drawable.default_avatar).into(profile_image, new Callback() {
                        @Override
                        public void onSuccess() {



                        }

                        @Override
                        public void onError() {
                            Picasso.with(AccountSettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(profile_image);

                        }
                    });


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        settings_change_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String curr_status = settings_status.getText().toString();
                Intent intent = new Intent(AccountSettingsActivity.this, StatusActivity.class);
                intent.putExtra("status", curr_status);
                startActivity(intent);
            }
        });
        settings_change_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery_intent = new Intent();
                gallery_intent.setType("image/*");
                gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery_intent, "SELECT IMAGE"), GALLERY_PICK);


            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);

            //Toast.makeText(SettingsActivity.this, imageUri, Toast.LENGTH_LONG).show();
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                progressDialog = new ProgressDialog(AccountSettingsActivity.this);
                progressDialog.setTitle("Uploading Image....");
                progressDialog.setMessage("Please Wait while we upload and process the image.");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                String curr_uid = firebaseUser.getUid();

                Uri resultUri = result.getUri();

                /*code of thumbnail
                 */
                try
                {
                    File thumb_filePath = new File(resultUri.getPath());
                    Bitmap thumb_bitmap = new Compressor(this).setMaxWidth(200).setMaxHeight(200).setQuality(75).
                            compressToBitmap(thumb_filePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();


                StorageReference filepath = image_storage.child("profile_images").child(curr_uid + ".jpg");
                final StorageReference thumb_filepath=image_storage.child("profile_images").child("thumbs").child(curr_uid + ".jpg");



                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                    @SuppressWarnings("VisibleForTests") final String download_url=task.getResult().getDownloadUrl().toString();
                    UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                     uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                         @Override
                         public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                             @SuppressWarnings("VisibleForTests")
                             String thumb_download_url=thumb_task.getResult().getDownloadUrl().toString();
                            if(thumb_task.isSuccessful())
                            {
                                Map updateHashMap=new HashMap();
                                updateHashMap.put("image",download_url);
                                updateHashMap.put("thumb_image",thumb_download_url);

                                databaseReference.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful())
                                        {
                                            progressDialog.dismiss();
                                            Toast.makeText(AccountSettingsActivity.this, "Successfully Uploaded..", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });


                            }
                            else
                            {
                                Toast.makeText(AccountSettingsActivity.this, "Error In uploading thumbnail...", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }


                         }
                     });


                        }
                        else{
                            Toast.makeText(AccountSettingsActivity.this, "Error In uploading...", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                  });

                }catch(Exception ex)
                {
                    ex.printStackTrace();
                };
                }
               else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }







    }
    /*

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
*/

    }




