package com.dentist.dokterdent.Model;

import java.io.Serializable;

public class Topiks implements Serializable {

    private String judul,photo,narasi,sumber,tipe;
    private Long timestamp;

    public Topiks(){ }

    public Topiks(String judul, String photo, String narasi, String sumber, Long timestamp, String tipe) {
        this.judul = judul;
        this.photo = photo;
        this.narasi = narasi;
        this.sumber = sumber;
        this.timestamp = timestamp;
        this.tipe = tipe;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getNarasi() {
        return narasi;
    }

    public void setNarasi(String narasi) {
        this.narasi = narasi;
    }

    public String getSumber() {
        return sumber;
    }

    public void setSumber(String sumber) {
        this.sumber = sumber;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }
}
