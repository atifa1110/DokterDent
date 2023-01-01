package com.dentist.dokterdent.SignIn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dentist.dokterdent.Utils.Util;
import com.dentist.dokterdent.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.messaging.FirebaseMessaging;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    private TextInputLayout tilEmail,tilPassword;
    private TextInputEditText etEmail, etPassword;
    private String email, password;
    private Button btn_masuk,btn_daftar;
    private TextView tv_lupa_pass;

    private FirebaseAuth mAuth;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //inisialisasi view
        etEmail = findViewById(R.id.et_email_signin);
        etPassword = findViewById(R.id.et_pass_signin);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        btn_masuk = findViewById(R.id.btn_masuk);;
        btn_daftar = findViewById(R.id.btn_daftar);;
        tv_lupa_pass = findViewById(R.id.tv_lupa_pass);;

        //inisialisasi database auth
        mAuth = FirebaseAuth.getInstance();

        //inisialisasi progress bar
        progress = new ProgressDialog(this);
        progress.setMessage("Sign In .. Silahkan Tunggu..");

        etPassword.addTextChangedListener(this);
        etEmail.addTextChangedListener(this);

        String text = "Belum punya akun? Daftar disini";
        SpannableString spannableString = new SpannableString(text);
        ForegroundColorSpan blue = new ForegroundColorSpan(ContextCompat.getColor(getApplication(), R.color.blue));
        ForegroundColorSpan gray = new ForegroundColorSpan(Color.GRAY);

        // It is used to set the span to the string
        spannableString.setSpan(gray, 0, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(blue, 18, 29, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        btn_daftar.setText(spannableString);

        //inisialisasi view yang di click
        btn_daftar.setOnClickListener(this);
        btn_masuk.setOnClickListener(this);
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
            case R.id.btn_daftar:
                signUp();
                break;
            case R.id.tv_lupa_pass:
                lupaPassword();
                break;
        }
    }

    private void lupaPassword(){
        Intent intent = new Intent(SignInActivity.this, LupaPasswordActivity.class);
        startActivity(intent);
    }

    private void signUp(){
        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

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
            //jika email kosong
            tilEmail.setError("Error : Email Kosong");
            //tilEmail.requestFocus();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches()){
            res = false;
            //jika pattern email tidak sesuai dengan text email
            tilEmail.setError("Error : Email salah");
            //tilEmail.requestFocus();
        }else if (etPassword.getText().toString().isEmpty() || etPassword.length()<6){
            res = false;
            //jika password yang dimasukkan kurang dari 6 dan kosong
            tilPassword.setError("Error : Minimal 6 Karakter");
            //tilPassword.requestFocus();
        }
        return res;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}