package com.example.ANONIMUS.service;

import com.example.ANONIMUS.dao.QrDao;
import com.example.ANONIMUS.model.QrResponse;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class QrService {

    private final QrDao qrDao;

    public QrService(QrDao qrDao) {
        this.qrDao = qrDao;
    }

    public QrResponse generateQrCode(String text) {
        byte[] qrCodeBytes = qrDao.generateQrCode(text);
        String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeBytes);
        return new QrResponse(qrCodeBase64);
    }
}