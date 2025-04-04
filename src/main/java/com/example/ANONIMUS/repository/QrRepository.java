package com.example.ANONIMUS.repository;

import com.example.ANONIMUS.model.QrEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QrRepository extends JpaRepository<QrEntity, Long> {
}