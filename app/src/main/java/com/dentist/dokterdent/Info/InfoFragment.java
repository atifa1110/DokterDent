package com.dentist.dokterdent.Info;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dentist.dokterdent.Model.NodeNames;
import com.dentist.dokterdent.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InfoFragment extends Fragment {

    private RecyclerView rvTopik;
    private TopikAdapter topikAdapter;
    private List<com.dentist.halodent.Model.TopikModel> topikModelList;
    private TextInputEditText searchText;
    private View progress_bar;

    private String topik_id,topik_nama,topik_narasi,topik_photo,topik_sumber,topik_time,topik_tipe;
    private DatabaseReference databaseReferenceTopik;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progress_bar = view.findViewById(R.id.progress_bar);
        searchText = view.findViewById(R.id.searchView);
        searchText.setHint("Search Information");

        rvTopik = view.findViewById(R.id.rv_info);
        databaseReferenceTopik = FirebaseDatabase.getInstance().getReference().child(NodeNames.TOPIKS);

        topikModelList = new ArrayList<>();
        rvTopik.setLayoutManager(new LinearLayoutManager(getContext()));
        topikAdapter = new TopikAdapter(getContext(), topikModelList);
        rvTopik.setAdapter(topikAdapter);

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    rvTopik.setAdapter(topikAdapter);
                    topikAdapter.notifyDataSetChanged();
                } else {
                    searchTopik(s.toString());
                }
            }
        });

        readTopikDatabase();
        //progress_bar.setVisibility(View.VISIBLE);
    }

    private void readTopikDatabase() {
        //progress_bar.setVisibility(View.GONE);
        databaseReferenceTopik.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                topikModelList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
                        final String topik_id = ds.getKey();
                        final String topik_nama = ds.child(NodeNames.TOPIK_NAME).getValue().toString();
                        topik_narasi = ds.child(NodeNames.TOPIK_NARASI).getValue().toString();

                        topik_photo = "";
                        if (ds.child(NodeNames.TOPIK_PHOTO).getValue() != null) {
                            topik_photo = ds.child(NodeNames.TOPIK_PHOTO).getValue().toString();
                            Log.d("topik_photo", topik_photo);
                        } else {
                            topik_photo = "";
                        }

                        topik_sumber = ds.child(NodeNames.TOPIK_SUMBER).getValue().toString();
                        topik_time = ds.child(NodeNames.TOPIK_TIME).getValue().toString();
                        topik_tipe = ds.child(NodeNames.TOPIK_TYPE).getValue().toString();
                        topikModelList.add(new com.dentist.halodent.Model.TopikModel(topik_id, topik_nama, topik_photo, topik_narasi, topik_sumber, topik_time, topik_tipe));
                        topikAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), getContext().getString(R.string.failed_to_read_data, error.getMessage())
                        , Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchTopik(final String query) {
        progress_bar.setVisibility(View.GONE);
        databaseReferenceTopik.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                topikModelList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    topik_id = ds.getKey();
                    Log.d("id_topik", topik_id);
                    if (ds.exists()) {
                        if (ds.child(NodeNames.TOPIK_NAME).toString().toLowerCase().contains(query.toLowerCase()) ||
                                ds.child(NodeNames.TOPIK_NARASI).toString().toLowerCase().contains(query.toLowerCase())) {
                            topik_nama = ds.child(NodeNames.TOPIK_NAME).getValue().toString();
                            Log.d("namaTopik", topik_nama);
                            topik_photo = ds.child(NodeNames.TOPIK_PHOTO).getValue().toString();
                            topik_narasi = ds.child(NodeNames.TOPIK_NARASI).getValue().toString();
                            topik_time = ds.child(NodeNames.TOPIK_TIME).getValue().toString();
                            topik_tipe = ds.child(NodeNames.TOPIK_TYPE).getValue().toString();
                            com.dentist.halodent.Model.TopikModel topikModel = new com.dentist.halodent.Model.TopikModel(topik_id, topik_nama, topik_narasi, topik_photo, topik_sumber, topik_time, topik_tipe);
                            topikModelList.add(topikModel);
                        } else {
                            Toast.makeText(getContext(), "Data failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    topikAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}