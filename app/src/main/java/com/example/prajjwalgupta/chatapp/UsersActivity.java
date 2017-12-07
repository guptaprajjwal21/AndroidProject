package com.example.prajjwalgupta.chatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

   public Toolbar toolbar;
   public DatabaseReference users_db;
  public   RecyclerView users_list;
    FirebaseAuth mAuth;
    private DatabaseReference mUserRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        toolbar=(Toolbar)findViewById(R.id.users_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        users_db= FirebaseDatabase.getInstance().getReference().child("Users");
        users_db.keepSynced(true);
        users_list=(RecyclerView)findViewById(R.id.users_list);
        users_list.setHasFixedSize(true);
        users_list.setLayoutManager(new LinearLayoutManager(UsersActivity.this));
        mAuth=FirebaseAuth.getInstance();



    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users,Users_ViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users,Users_ViewHolder>
                (Users.class,R.layout.users_layout,Users_ViewHolder.class,users_db) {
            @Override
            protected void populateViewHolder(Users_ViewHolder viewHolder, final Users model, int position) {


            viewHolder.setName(model.getName());
            viewHolder.setStatus(model.getStatus());
            viewHolder.setUserImage(model.getThumb_image(),getApplicationContext());
              final String user_id=getRef(position).getKey();
            viewHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    Intent intent=new Intent(UsersActivity.this,ProfileActivity.class);
                    intent.putExtra("user_id",user_id);

                  //  intent.putExtra("user_name",model.getName());

                    startActivity(intent);

                }
            });
            }
        };
        users_list.setAdapter(firebaseRecyclerAdapter);
    }


    @Override
    protected void onStop() {
        super.onStop();
        FirebaseRecyclerAdapter<Users,Users_ViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users,Users_ViewHolder>
                (Users.class,R.layout.users_layout,Users_ViewHolder.class,users_db) {
            @Override
            protected void populateViewHolder(Users_ViewHolder viewHolder, final Users model, int position) {


                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setUserImage(model.getThumb_image(),getApplicationContext());
                final String user_id=getRef(position).getKey();
                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        Intent intent=new Intent(UsersActivity.this,ProfileActivity.class);
                        intent.putExtra("user_id",user_id);

                        //  intent.putExtra("user_name",model.getName());

                        startActivity(intent);

                    }
                });
            }
        };


    }

    public static class Users_ViewHolder extends  RecyclerView.ViewHolder
    {
        View view;

        public Users_ViewHolder(View itemView) {
            super(itemView);
            view=itemView;
        }

        public void setName(String name) {
            TextView user_name=(TextView)view.findViewById(R.id.user_singleName);
            user_name.setText(name);

        }


        public void setStatus(String status) {
            TextView user_status=(TextView)view.findViewById(R.id.user_singleStatus);
            user_status.setText(status);
        }


        public void setUserImage(final String thumb_image, final Context context) {

            final CircleImageView userImgView=(CircleImageView)view.findViewById(R.id.user_singleImage);

            if (!thumb_image.equals("default")) {

                //    Picasso.with(AccountSettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(profile_image);
                Picasso.with(context).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).
                        placeholder(R.drawable.default_avatar).into(userImgView, new Callback() {
                    @Override
                    public void onSuccess() {



                    }

                    @Override
                    public void onError() {
                        Picasso.with(context).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImgView);

                    }
                });


            }
        }

    }


}
