package com.dentist.dokterdent.Model;

import java.io.Serializable;

public class Users implements Serializable {

    private String id,nama,email,photo,ponsel,status,role,timestamp,kelamin;

    public Users(){

    }
    public Users(String id, String timestamp, String role) {
        this.id = id;
        this.role = role;
        this.timestamp = timestamp;
    }

    public Users(String id, String nama, String email, String photo, String ponsel, String status, String role, String kelamin) {
        this.id = id;
        this.nama = nama;
        this.email = email;
        this.photo = photo;
        this.ponsel = ponsel;
        this.status = status;
        this.role = role;
        this.kelamin = kelamin;
    }

    public String getKelamin() {
        return kelamin;
    }

    public void setKelamin(String kelamin) {
        this.kelamin = kelamin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPonsel() {
        return ponsel;
    }

    public void setPonsel(String ponsel) {
        this.ponsel = ponsel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
