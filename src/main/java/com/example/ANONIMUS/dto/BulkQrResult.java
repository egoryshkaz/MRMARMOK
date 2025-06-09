package com.example.ANONIMUS.dto;

import java.util.Objects;

public class BulkQrResult {
    private String inputText;
    private String inputUsername;
    private String qrCodeBase64;
    private String error;

    public BulkQrResult() {
    }

    public BulkQrResult(String inputText, String inputUsername, String qrCodeBase64, String error) {
        this.inputText = inputText;
        this.inputUsername = inputUsername;
        this.qrCodeBase64 = qrCodeBase64;
        this.error = error;
    }

    public static BulkQrResult success(String inputText, String inputUsername, String qrCodeBase64) {
        return new BulkQrResult(inputText, inputUsername, qrCodeBase64, null);
    }

    public static BulkQrResult failure(String inputText, String inputUsername, String error) {
        return new BulkQrResult(inputText, inputUsername, null, error);
    }


    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public String getInputUsername() {
        return inputUsername;
    }

    public void setInputUsername(String inputUsername) {
        this.inputUsername = inputUsername;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }

    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BulkQrResult that = (BulkQrResult) o;
        return Objects.equals(inputText, that.inputText) &&
                Objects.equals(inputUsername, that.inputUsername) &&
                Objects.equals(qrCodeBase64, that.qrCodeBase64) &&
                Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputText, inputUsername, qrCodeBase64, error);
    }

    @Override
    public String toString() {
        return "BulkQrResult{" +
                "inputText='" + inputText + '\'' +
                ", inputUsername='" + inputUsername + '\'' +
                ", qrCodeBase64='" + (qrCodeBase64 != null ? "[present]" : "null") + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}