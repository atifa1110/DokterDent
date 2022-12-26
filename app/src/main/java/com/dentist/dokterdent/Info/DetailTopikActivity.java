package com.dentist.dokterdent.Info;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dentist.dokterdent.Model.Topiks;
import com.dentist.dokterdent.Utils.Extras;
import com.dentist.dokterdent.R;
import com.dentist.dokterdent.Utils.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

import bg.devlabs.fullscreenvideoview.FullscreenVideoView;

public class DetailTopikActivity extends AppCompatActivity {

    private ImageView iv_image,iv_fullscreen;
    private FullscreenVideoView playerView;
    private TextView tv_judul,tv_narasi,tv_sumber,tv_tanggal,sumber;
    private Topiks topik;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_topik);
        setActionBar();

        //inisialisasi view
        iv_fullscreen = findViewById(R.id.fullscreen);
        iv_image = findViewById(R.id.image_view);
        playerView = findViewById(R.id.video_player);
        sumber = findViewById(R.id.tv_smb_detail_topik);
        tv_judul = findViewById(R.id.tv_judul_detail_topik);
        tv_narasi = findViewById(R.id.tv_narasi_detail_topik);
        tv_sumber = findViewById(R.id.tv_sumber_detail_topik);
        tv_tanggal = findViewById(R.id.tv_tanggal_detail_topik);

        topik = (Topiks) getIntent().getSerializableExtra(Extras.TOPIK);

        iv_fullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(topik.getPhoto());
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                intent.setDataAndType(uri,"image/jpg");
                startActivity(intent);
            }
        });

        String nar = topik.getNarasi().toString();
        String sum = topik.getSumber().toString();

        nar = nar.replaceAll("/n","\n");
        nar = nar.replaceAll("/n/n","\n\n");
        sum = sum.replaceAll("/n", "\n");

        tv_judul.setText(topik.getJudul());
        tv_narasi.setText(nar);
        tv_sumber.setText(sum);
        tv_tanggal.setText(Util.getDay(topik.getTimestamp()));

        if (topik.getTipe().equals("photo")) {
            if (topik.getPhoto().equals("")) {
                iv_image.setImageResource(R.drawable.ic_add_photo);
            } else {
                Glide.with(DetailTopikActivity.this)
                        .load(topik.getPhoto())
                        .placeholder(R.drawable.ic_add_photo)
                        .fitCenter().into(iv_image);
            }
        } else if (topik.getTipe().equals("video")) {
            iv_image.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
            tv_sumber.setVisibility(View.GONE);
            sumber.setVisibility(View.GONE);
            //inizializePlayer();
            setVideoPlayer(topik.getPhoto());
        }
    }

    private void setVideoPlayer(String URL) {
        playerView.videoUrl(URL)
                .addSeekBackwardButton()
                .addSeekForwardButton()
                .enableAutoStart();
    }

    //set action bar
    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
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