package com.example.ANONIMUS.model;

public class QrResponse {
    private String qrCode;

    public QrResponse(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}