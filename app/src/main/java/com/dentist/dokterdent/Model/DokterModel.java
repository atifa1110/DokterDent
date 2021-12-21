package com.dentist.dokterdent.Model;

import java.io.Serializable;

public class DokterModel extends UserModel implements Serializable {

    private String nip,str,sip;

    public DokterModel(String id, String nama, String email, String photo, String ponsel, String status, String role, String nip, String str, String sip) {
        super(id, nama, email, photo, ponsel, status, role);
        this.nip=nip;
        this.str=str;
        this.sip=sip;
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public String getSip() {
        return sip;
    }

    public void setSip(String sip) {
        this.sip = sip;
    }
}
