package com.example.ANONIMUS.dto;

import java.util.List;
import java.util.Objects;

public class QrGenerationRequest {
    private String text;
    private String username;

    public QrGenerationRequest() {
    }

    public QrGenerationRequest(String text, String username) {
        this.text = text;
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QrGenerationRequest that = (QrGenerationRequest) o;
        return Objects.equals(text, that.text) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, username);
    }

    @Override
    public String toString() {
        return "QrGenerationRequest{" +
                "text='" + text + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}