package com.dentist.dokterdent.Info;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dentist.dokterdent.Model.Extras;
import com.dentist.dokterdent.R;

import bg.devlabs.fullscreenvideoview.FullscreenVideoView;

public class DetailTopikActivity extends AppCompatActivity {

    private ImageView iv_image;
    private FullscreenVideoView playerView;
    private TextView tv_judul, narasi, sumber, tv_sumber;

    private com.dentist.halodent.Model.TopikModel topik;
    private String nar, sum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_topik);
        setActionBar();

        //inisialisasi view
        iv_image = findViewById(R.id.image_view);
        playerView = findViewById(R.id.video_player);
        tv_sumber = findViewById(R.id.tv_sumber);
        tv_judul = findViewById(R.id.judul);
        narasi = findViewById(R.id.narasi);
        sumber = findViewById(R.id.sumber);

        topik = (com.dentist.halodent.Model.TopikModel) getIntent().getSerializableExtra(Extras.TOPIK);

        tv_judul.setText(topik.getJudul());

        nar = topik.getNarasi();
        sum = topik.getSumber();

        nar.replaceAll("\n\n", "\n\n");
        nar.replaceAll("\n", "\n");
        sum.replaceAll("\n", "\n");

        narasi.setText(nar);
        sumber.setText(sum);

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