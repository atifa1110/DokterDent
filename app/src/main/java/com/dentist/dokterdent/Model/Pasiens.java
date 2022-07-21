package com.dentist.dokterdent.Model;

import java.io.Serializable;

public class Pasiens extends Users implements Serializable {

    private String usia,alamat;

    public Pasiens(){

    }
    public Pasiens(String id, String nama, String email, String photo, String ponsel, String status, String role, String kelamin, String usia, String alamat) {
        super(id, nama, email, photo, ponsel, status, role,kelamin);
        this.usia=usia;
        this.alamat=alamat;
    }

    public String getUsia() {
        return usia;
    }

    public void setUsia(String usia) {
        this.usia = usia;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }
}
