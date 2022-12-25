package com.dentist.dokterdent.Chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.dentist.dokterdent.Model.Konselors;
import com.dentist.dokterdent.Utils.NodeNames;
import com.dentist.dokterdent.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class KonselorActivity extends AppCompatActivity {

    private TextInputEditText search;
    private RecyclerView rv_konselor;
    private List<Konselors> konselorList;
    private KonselorAdapter konselorAdapter;

    private DatabaseReference databaseReferenceKonselor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konselor);
        setActionBar();

        search = findViewById(R.id.searchView);
        search.setHint("Cari Konselor...");

        rv_konselor = findViewById(R.id.rv_all_konselor);

        konselorList = new ArrayList<>();
        konselorAdapter = new KonselorAdapter(KonselorActivity.this,konselorList);

        rv_konselor.setLayoutManager(new LinearLayoutManager(getApplication()));
        rv_konselor.setAdapter(konselorAdapter);

        databaseReferenceKonselor = FirebaseDatabase.getInstance().getReference().child(NodeNames.KONSELORS);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().isEmpty()){
                    rv_konselor.setAdapter(konselorAdapter);
                    konselorAdapter.notifyDataSetChanged();
                }else{
                    searchKonselor(s.toString());
                }
            }
        });

        getDataKonselor();
    }

    private void getDataKonselor(){
        Query query = databaseReferenceKonselor.orderByChild(NodeNames.NAME);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                konselorList.clear();
                for (final DataSnapshot ds : snapshot.getChildren()){
                    if(ds.exists()) {
                        Konselors konselor = ds.getValue(Konselors.class);
                        konselorList.add(konselor);
                        konselorAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void searchKonselor(final String query) {
        databaseReferenceKonselor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                konselorList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
                        Konselors konselor = ds.getValue(Konselors.class);
                        if(konselor.getNama().toLowerCase().contains(query.toLowerCase())){
                            konselorList.add(konselor);
                            konselorAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


    //set action bar
    private void setActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(" ");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setElevation(0);
            actionBar.setDisplayOptions(actionBar.getDisplayOptions());
        }
    }

    // this event will enable the back , function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}