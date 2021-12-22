package com.dentist.dokterdent.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dentist.dokterdent.Model.Util;
import com.dentist.dokterdent.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.messaging.FirebaseMessaging;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener{

    private TextInputEditText etEmail, etPassword;
    private String email, password;
    private Button btnMasuk;
    private TextView tvDaftar, tv_lupa_pass;

    private FirebaseAuth mAuth;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //inisialisasi view
        etEmail = findViewById(R.id.et_email_signin);
        etPassword = findViewById(R.id.et_pass_signin);
        btnMasuk = findViewById(R.id.btn_masuk);
        tvDaftar = findViewById(R.id.tv_daftar);
        tv_lupa_pass = findViewById(R.id.tv_lupa_pass);

        //inisialisasi database auth
        mAuth = FirebaseAuth.getInstance();

        //inisialisasi progress bar
        progress = new ProgressDialog(this);
        progress.setMessage("Sign In .. please wait..");

        //inisialisasi view yang di click
        tvDaftar.setOnClickListener(this);
        btnMasuk.setOnClickListener(this);
        tv_lupa_pass.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        //dalam keaadan sudah start
        super.onStart();
        //cek apakah sudah login atau belum sesuai dengan user yang masuk
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //jika sudah login maka pindah ke main activity
        if (currentUser!=null){
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<String> task) {
                    String message = task.getResult();
                    Util.updateDeviceToken(SignInActivity.this,message);
                }
            });
//            FirebaseMessaging.getInstance().subscribeToTopic("messages").addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull @NotNull Task<Void> task) {
//                    String msg = getString(R.string.msg_subscribed);
//                    if(task.isSuccessful()){
//                        Util.updateDeviceToken(SignInActivity.this,msg);
//                    }else{
//                        msg = getString(R.string.msg_failed_subscribed);
//                        Util.updateDeviceToken(SignInActivity.this,msg);
//                    }
//                }
//            });
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_masuk:
                progress.show();
                signIn(v);
                break;
            case R.id.tv_daftar:
               // signUp();
                break;
            case R.id.tv_lupa_pass:
              //  lupaPassword();
                break;
        }
    }

//    private void lupaPassword(){
//        Intent intent = new Intent(SignInActivity.this, LupaPasswordActivity.class);
//        startActivity(intent);
//    }
//
//    private void signUp(){
//        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
//        startActivity(intent);
//    }

    private void signIn(View view){
        // jika input data sudah benar
        if (inputValidated()) {
            //set text menjadi string supayawwa bisa dibaca dalam bentuk string
            email = etEmail.getText().toString().trim();
            password = etPassword.getText().toString().trim();

            //signin dengan email dan password
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        //progress ilang
                        progress.dismiss();
                        //jika berhasil masuk akan muncul komen bahwa login berhasil
                        Toast.makeText(getApplicationContext(), R.string.login_success, Toast.LENGTH_SHORT).show();
                        //pindah ke mainActivity
                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                        startActivity(intent);
                    }else{
                        //progress ilang
                        progress.dismiss();
                        //keluar error
                        Toast.makeText(SignInActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private boolean inputValidated(){
        boolean res = true;
        //jika text email kosong
        if (etEmail.getText().toString().isEmpty()){
            res = false;
            etEmail.setError("Silahkan isi field");
            //jika text password kosong
        }else if (etPassword.getText().toString().isEmpty()){
            res = false;
            etPassword.setError("Silahkan isi field");
            //jika pattern email tidak sesuai dengan text email
        }else if(!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches()){
            res = false;
            etEmail.setError(getString(R.string.masukkan_email_benar));
        }
        return res;
    }
}