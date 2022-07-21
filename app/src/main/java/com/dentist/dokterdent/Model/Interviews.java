package com.dentist.dokterdent.Model;

public class Interviews {

    private String id,pernah_ke_dokter,keluhan,pengobatan,riwayat_penyakit;

    public Interviews(){
    }

    public Interviews(String pernah_ke_dokter, String keluhan, String pengobatan, String riwayat_penyakit) {
        this.pernah_ke_dokter = pernah_ke_dokter;
        this.keluhan = keluhan;
        this.pengobatan = pengobatan;
        this.riwayat_penyakit = riwayat_penyakit;
    }

    public String getPernah_ke_dokter() {
        return pernah_ke_dokter;
    }

    public void setPernah_ke_dokter(String pernah_ke_dokter) {
        this.pernah_ke_dokter = pernah_ke_dokter;
    }

    public String getKeluhan() {
        return keluhan;
    }

    public void setKeluhan(String keluhan) {
        this.keluhan = keluhan;
    }

    public String getPengobatan() {
        return pengobatan;
    }

    public void setPengobatan(String pengobatan) {
        this.pengobatan = pengobatan;
    }

    public String getRiwayat_penyakit() {
        return riwayat_penyakit;
    }

    public void setRiwayat_penyakit(String riwayat_penyakit) {
        this.riwayat_penyakit = riwayat_penyakit;
    }
}
