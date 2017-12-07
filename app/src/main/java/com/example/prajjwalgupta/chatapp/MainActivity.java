package com.example.prajjwalgupta.chatapp;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ViewPager viewPager;
    private SectionPagerAdapter sectionPagerAdapter;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private DatabaseReference mUserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();


        if (mAuth.getCurrentUser() != null) {


            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }
      toolbar=(Toolbar)findViewById(R.id.main_page_toolbar);
      viewPager=(ViewPager)findViewById(R.id.main_tabpager);
        sectionPagerAdapter=new SectionPagerAdapter(getSupportFragmentManager());
        setSupportActionBar(toolbar);
       getSupportActionBar().setTitle("Chat App");
       viewPager.setAdapter(sectionPagerAdapter);
        tabLayout=(TabLayout)findViewById(R.id.tabLayout);
       tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();

        if(currentUser==null)
        {
             sendToStart();
        }
        else
        {
            mUserRef.child("online").setValue("true");
        }



    }

    @Override
    protected void onStop()
    {

        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser==null)
        {
            sendToStart();
        }

        if (currentUser != null) {

            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_list,menu);
       return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

      if(item.getItemId()==R.id.log_out)
      {
          sendToStart();

      }
        if(item.getItemId()==R.id.acc_setting)
        {
            Intent intent=new Intent(MainActivity.this,AccountSettingsActivity.class);
            startActivity(intent);

        }
        if(item.getItemId()==R.id.all_users)
        {
            Intent intent=new Intent(MainActivity.this,UsersActivity.class);
            startActivity(intent);

        }





        return super.onOptionsItemSelected(item);
    }

    private void sendToStart() {

        Intent intent=new Intent(MainActivity.this,StartActivity.class);
        startActivity(intent);

    }
}
