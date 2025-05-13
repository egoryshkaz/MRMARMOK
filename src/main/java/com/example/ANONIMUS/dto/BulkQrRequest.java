package com.example.ANONIMUS.dto;

import java.util.List;
import java.util.Objects;

public class BulkQrRequest {
    private List<QrGenerationRequest> requests;

    public BulkQrRequest() {
    }

    public BulkQrRequest(List<QrGenerationRequest> requests) {
        this.requests = requests;
    }

    public List<QrGenerationRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<QrGenerationRequest> requests) {
        this.requests = requests;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BulkQrRequest that = (BulkQrRequest) o;
        return Objects.equals(requests, that.requests);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requests);
    }

    @Override
    public String toString() {
        return "BulkQrRequest{" +
                "requests=" + requests +
                '}';
    }
}