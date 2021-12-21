package com.dentist.dokterdent.Model;

import java.io.Serializable;

public class PasienModel extends UserModel implements Serializable {

    private String usia,kelamin,alamat;

    public PasienModel(String id, String nama, String email, String photo, String ponsel, String status, String role, String usia, String kelamin, String alamat) {
        super(id, nama, email, photo, ponsel, status, role);
        this.usia=usia;
        this.kelamin=kelamin;
        this.alamat=alamat;
    }

    public String getUsia() {
        return usia;
    }

    public void setUsia(String usia) {
        this.usia = usia;
    }

    public String getKelamin() {
        return kelamin;
    }

    public void setKelamin(String kelamin) {
        this.kelamin = kelamin;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }
}
