package com.dentist.dokterdent.SignIn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.widget.Toast;

import com.dentist.dokterdent.Model.Dokters;
import com.dentist.dokterdent.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    private TextInputLayout tilEmail, tilName, tilPassword, tilConfirm, tilNomor;
    private TextInputEditText etEmail, etName, etPassword, etConfirmPassword, etNomor;
    private Button btn_daftar;
    private MaterialButton btn_masuk;
    private Toolbar toolbar;
    private String email, nama, password, confirmPassword, nomor;
    private ProgressDialog progress;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //inisialisasi view
        tilEmail = findViewById(R.id.til_email_signup);
        tilName = findViewById(R.id.til_nama_signup);
        tilPassword = findViewById(R.id.til_password_signup);
        tilConfirm = findViewById(R.id.til_confirm_signup);
        tilNomor = findViewById(R.id.til_nomer_signup);
        etEmail = findViewById(R.id.et_email_signup);
        etName = findViewById(R.id.et_nama_signup);
        etPassword = findViewById(R.id.et_password_signup);
        etConfirmPassword = findViewById(R.id.et_confirm_signup);
        etNomor = findViewById(R.id.et_nomer_signup);
        btn_daftar = findViewById(R.id.btn_daftar);
        btn_masuk = findViewById(R.id.btn_masuk);
        toolbar = findViewById(R.id.toolbar);

        progress = new ProgressDialog(this);
        progress.setMessage("Sign Up .. Silahkan Tunggu..");

        String text = "Sudah punya akun? Masuk disini";
        SpannableString spannableString = new SpannableString(text);
        ForegroundColorSpan blue = new ForegroundColorSpan(ContextCompat.getColor(getApplication(), R.color.blue));
        ForegroundColorSpan gray = new ForegroundColorSpan(Color.GRAY);

        // It is used to set the span to the string
        spannableString.setSpan(gray, 0, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(blue, 18, 29, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        btn_masuk.setText(spannableString);

        //set button click
        btn_daftar.setOnClickListener(this);
        btn_masuk.setOnClickListener(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_daftar:
                signUp();
                break;
            case R.id.btn_masuk:
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                break;
        }
    }

    public void updateDatabase() {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString().trim())
                .build();

        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()) {
                    String userId = firebaseUser.getUid();

                    databaseReference = FirebaseDatabase.getInstance().getReference().child(Dokters.class.getSimpleName());

                    Dokters dokters = new Dokters(userId, etName.getText().toString(), etEmail.getText().toString(), "", etNomor.getText().toString(), "Online", "Dokter Pengawas", "", "", "", "");

                    databaseReference.child(userId).setValue(dokters).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progress.dismiss();
                                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                                progress.dismiss();
                            } else {
                                progress.dismiss();
                                Toast.makeText(SignUpActivity.this,
                                        getString(R.string.failed_to_update, task.getException()), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    public void signUp() {
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();

        if (inputValidated()) {
            progress.show();
            final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        firebaseUser = firebaseAuth.getCurrentUser();
                        updateDatabase();
                    } else {
                        progress.dismiss();
                        Toast.makeText(SignUpActivity.this, getString(R.string.sign_up_failed, task.getException()), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public boolean inputValidated() {
        boolean res = true;

        email = etEmail.getText().toString().trim();
        nama = etName.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        confirmPassword = etConfirmPassword.getText().toString().trim();
        nomor = etNomor.getText().toString().trim();

        if (email.isEmpty()) {
            res = false;
            tilEmail.setError("Error : Email Kosong");
        } else if (nama.isEmpty()) {
            res = false;
            tilName.setError("Error : Name Kosong");
        } else if (password.isEmpty()) {
            res = false;
            tilPassword.setError("Error : Password Kosong");
        } else if (confirmPassword.isEmpty()) {
            res = false;
            tilConfirm.setError("Error : Password Kosong");
        } else if (nomor.isEmpty()) {
            res = false;
            tilNomor.setError("Error : Nomor Kosong");
        } else if (!password.equals(confirmPassword)) {
            res = false;
            tilPassword.setError("Error : Password tidak cocok");
            tilConfirm.setError("Error : Password tidak cocok");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            res = false;
            tilEmail.setError("Error : Email salah");
        }
        return res;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirm.setError(null);
        tilNomor.setError(null);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
