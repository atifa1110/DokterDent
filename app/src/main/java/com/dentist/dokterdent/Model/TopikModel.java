package com.dentist.halodent.Model;

import java.io.Serializable;

public class TopikModel implements Serializable {

    private String id,judul,photo,narasi,sumber,timestamp,tipe;

    public TopikModel(String id,String judul, String photo, String narasi, String sumber, String timestamp, String tipe) {
        this.id=id;
        this.judul = judul;
        this.photo = photo;
        this.narasi = narasi;
        this.sumber = sumber;
        this.timestamp = timestamp;
        this.tipe = tipe;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }
}
