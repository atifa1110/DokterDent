package com.dentist.dokterdent.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.dentist.dokterdent.Chat.ChatFragment;
import com.dentist.dokterdent.Info.InfoFragment;
import com.dentist.dokterdent.Model.NodeNames;
import com.dentist.dokterdent.Profile.ProfileFragment;
import com.dentist.dokterdent.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private BottomNavigationView bottomNavigationView;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        bottomNavigationView = findViewById(R.id.bottom_nav_main);
        bottomNavigationView.setOnNavigationItemSelectedListener(monNavigationItemSelectedListener);

        loadFragment(new ChatFragment());

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.DOKTERS).child(currentUser.getUid());
        databaseReference.child(NodeNames.ONLINE).setValue("Online");

        databaseReference.child(NodeNames.ONLINE).onDisconnect().setValue("Offline");
    }

    private BottomNavigationView.OnNavigationItemSelectedListener monNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.menu_chat:
                    actionBar.setTitle("Dokter Dent");
                    fragment = new ChatFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.menu_info:
                    actionBar.setTitle("Information");
                    fragment = new InfoFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.menu_profile:
                    actionBar.setTitle("Profile");
                    fragment = new ProfileFragment();
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment){
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_main_main, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}