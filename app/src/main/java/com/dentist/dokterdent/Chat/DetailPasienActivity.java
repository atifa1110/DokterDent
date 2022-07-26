package com.dentist.dokterdent.Chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dentist.dokterdent.Model.Extras;
import com.dentist.dokterdent.Model.Interviews;
import com.dentist.dokterdent.Model.NodeNames;
import com.dentist.dokterdent.Model.Pasiens;
import com.dentist.dokterdent.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetailPasienActivity extends AppCompatActivity {

    private CircleImageView iv_profil;
    private TextView tv_umur,tv_email,tv_nama,tv_kelamin,tv_alamat,tv_nomor,tv_jawaban_1,tv_jawaban_2,tv_jawaban_3,tv_jawaban_4;

    private TextView tv_kategori;
    private TextView tv_nilai;
    private DatabaseReference databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child(Pasiens.class.getSimpleName());
    private DatabaseReference databaseReferenceUserSurvey = FirebaseDatabase.getInstance().getReference().child(NodeNames.SURVEYS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pasien);
        setActionBar();

        iv_profil = findViewById(R.id.iv_user_detail);
        tv_umur = findViewById(R.id.tv_umur_user);
        tv_email= findViewById(R.id.tv_email_user_detail);
        tv_nama = findViewById(R.id.tv_nama_user_detail);
        tv_kelamin = findViewById(R.id.tv_kelamin_user);
        tv_alamat = findViewById(R.id.tv_alamat_user);
        tv_nomor = findViewById(R.id.tv_nomer_user);
        tv_kategori = findViewById(R.id.tv_jenis_karies);
        tv_nilai = findViewById(R.id.tv_nilai_karies);

        tv_jawaban_1 = findViewById(R.id.tv_jawaban_1);
        tv_jawaban_2 = findViewById(R.id.tv_jawaban_2);
        tv_jawaban_3 = findViewById(R.id.tv_jawaban_3);
        tv_jawaban_4 = findViewById(R.id.tv_jawaban_4);

        String id = getIntent().getStringExtra(Extras.USER);

        getDataUser(id);
    }

    private void getDataUser(String id){
        databaseReferenceUser.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Pasiens pasiens = snapshot.getValue(Pasiens.class);
                    tv_nama.setText(pasiens.getNama());
                    tv_umur.setText(pasiens.getUsia()+" Tahun");
                    tv_email.setText(pasiens.getEmail());
                    tv_kelamin.setText(pasiens.getKelamin());
                    tv_alamat.setText(pasiens.getAlamat());
                    tv_nomor.setText("0"+pasiens.getPonsel());

                    Glide.with(getApplicationContext()).load(pasiens.getPhoto()).placeholder(R.drawable.ic_user)
                            .into(iv_profil);
                }else{
                    Toast.makeText(DetailPasienActivity.this,R.string.failed_to_read_data,Toast.LENGTH_SHORT);
                }
                getDataSurvey(id);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(DetailPasienActivity.this,R.string.failed_to_read_data,Toast.LENGTH_SHORT);
            }
        });
    }

    private void getDataSurvey(String id){
        databaseReferenceUserSurvey.child(id).child("interview").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Interviews interviews = snapshot.getValue(Interviews.class);
                    tv_jawaban_1.setText(interviews.getPernah_ke_dokter());
                    tv_jawaban_2.setText(interviews.getKeluhan());
                    tv_jawaban_3.setText(interviews.getPengobatan());
                    tv_jawaban_4.setText(interviews.getRiwayat_penyakit());

                    databaseReferenceUserSurvey.child(id).child("karies").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                tv_kategori.setText(snapshot.child("kategori").getValue().toString());
                                tv_nilai.setText(snapshot.child("score").getValue().toString());
                            }else{
                                tv_kategori.setText(" ");
                                tv_nilai.setText(" ");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });

                }else{
                    Toast.makeText(DetailPasienActivity.this,R.string.failed_to_read_data,Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(DetailPasienActivity.this,R.string.failed_to_read_data,Toast.LENGTH_SHORT);
            }
        });
    }

    private void setActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(" ");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setElevation(0);
            actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_CUSTOM);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}