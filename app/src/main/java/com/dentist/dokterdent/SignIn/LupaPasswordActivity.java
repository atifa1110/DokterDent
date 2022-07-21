package com.dentist.dokterdent.SignIn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dentist.dokterdent.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class LupaPasswordActivity extends AppCompatActivity implements TextWatcher {

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private Button btn_kirim;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lupa_password);

        tilEmail = findViewById(R.id.til_email_lupa);
        etEmail = findViewById(R.id.et_email_lupa);
        btn_kirim = findViewById(R.id.btn_kirim);
        toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle("");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        etEmail.addTextChangedListener(this);
        btn_kirim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword(){
        String email = etEmail.getText().toString().trim();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if(inputValidated()){
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Intent intent = new Intent(LupaPasswordActivity.this, LupaPasswordSendActivity.class);
                        startActivity(intent);
                        Toast.makeText(LupaPasswordActivity.this, "Periksa Email Anda", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(LupaPasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
        }
        return res;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        tilEmail.setError(null);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
