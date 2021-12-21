package com.dentist.dokterdent.Model;

import java.io.Serializable;

public class KonselorModel extends UserModel implements Serializable {

    private String nim,angkatan,kelamin;

    public KonselorModel(String id, String nama, String email, String photo, String ponsel, String status, String role, String nim, String angkatan, String kelamin) {
        super(id, nama, email, photo, ponsel,status, role);
        this.nim=nim;
        this.angkatan = angkatan;
        this.kelamin=kelamin;
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

    public String getKelamin() {
        return kelamin;
    }

    public void setKelamin(String kelamin) {
        this.kelamin = kelamin;
    }
}
