package com.example.ANONIMUS.repository;

import com.example.ANONIMUS.model.QrEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
@Repository
public interface QrRepository extends JpaRepository<QrEntity, Long> {

    @Query("SELECT q FROM QrEntity q JOIN q.users u WHERE u.username = :username")
    List<QrEntity> findByUsername(@Param("username") String username);
}