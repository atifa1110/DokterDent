package com.dentist.dokterdent.Model;

import java.io.Serializable;

public class Konselors extends Users implements Serializable {

    private String nim,angkatan;

    public Konselors(){

    }
    public Konselors(String id, String nama, String email, String photo, String ponsel, String status, String role, String kelamin, String nim, String angkatan) {
        super(id, nama, email, photo, ponsel,status, role,kelamin);
        this.nim=nim;
        this.angkatan = angkatan;
    }

    public String getNim() {
        return nim;
    }

    public void setNim(String nim) {
        this.nim = nim;
    }

    public String getAngkatan() {
        return angkatan;
    }

    public void setAngkatan(String angkatan) {
        this.angkatan = angkatan;
    }
}
