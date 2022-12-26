package com.dentist.dokterdent.Group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dentist.dokterdent.Utils.Extras;
import com.dentist.dokterdent.Model.Interviews;
import com.dentist.dokterdent.Utils.NodeNames;
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

    private CircleImageView ivProfil;
    private TextView tvKategori,tvNilai;
    private TextView tvUmur,tvEmail,tvNama,tvKelamin,tvAlamat,tvNomor,tvJawaban1,tvJawaban2,tvJawaban3,tvJawaban4;

    private DatabaseReference databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child(Pasiens.class.getSimpleName());
    private DatabaseReference databaseReferenceUserSurvey = FirebaseDatabase.getInstance().getReference().child(NodeNames.SURVEYS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pasien);
        setActionBar();

        ivProfil = findViewById(R.id.iv_user_detail);
        tvUmur = findViewById(R.id.tv_umur_user);
        tvEmail= findViewById(R.id.tv_email_user_detail);
        tvNama = findViewById(R.id.tv_nama_user_detail);
        tvKelamin = findViewById(R.id.tv_kelamin_user);
        tvAlamat = findViewById(R.id.tv_alamat_user);
        tvNomor = findViewById(R.id.tv_nomer_user);
        tvKategori = findViewById(R.id.tv_jenis_karies);
        tvNilai = findViewById(R.id.tv_nilai_karies);

        tvJawaban1 = findViewById(R.id.tv_jawaban_1);
        tvJawaban2 = findViewById(R.id.tv_jawaban_2);
        tvJawaban3 = findViewById(R.id.tv_jawaban_3);
        tvJawaban4 = findViewById(R.id.tv_jawaban_4);

        String id = getIntent().getStringExtra(Extras.USER);

        getDataUser(id);
    }

    private void getDataUser(String id){
        databaseReferenceUser.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Pasiens pasiens = snapshot.getValue(Pasiens.class);
                    try {
                        tvNama.setText(pasiens.getNama());
                        tvUmur.setText(pasiens.getUsia() + " Tahun");
                        tvEmail.setText(pasiens.getEmail());
                        tvKelamin.setText(pasiens.getKelamin());
                        tvAlamat.setText(pasiens.getAlamat());
                        tvNama.setText("0" + pasiens.getPonsel());

                        Glide.with(getApplicationContext()).load(pasiens.getPhoto()).placeholder(R.drawable.ic_user)
                                .into(ivProfil);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
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
        databaseReferenceUserSurvey.child(id).child(NodeNames.INTERVIEW).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Interviews interviews = snapshot.getValue(Interviews.class);
                    tvJawaban1.setText(interviews.getPernah_ke_dokter());
                    tvJawaban2.setText(interviews.getKeluhan());
                    tvJawaban3.setText(interviews.getPengobatan());
                    tvJawaban4.setText(interviews.getRiwayat_penyakit());

                    databaseReferenceUserSurvey.child(id).child("karies").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                tvKategori.setText(snapshot.child("kategori").getValue().toString());
                                tvNilai.setText(snapshot.child("score").getValue().toString());
                            }else{
                                tvKategori.setText(" ");
                                tvNilai.setText(" ");
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