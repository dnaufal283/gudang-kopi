package com.kopi.gudang.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kopi.gudang.entity.LogNotifikasi;

@Repository
public interface LogNotifikasiRepository extends JpaRepository<LogNotifikasi, Long> {
    // Mengambil notifikasi yang belum dibaca oleh admin, urut dari yang terbaru
    List<LogNotifikasi> findByStatusOrderByCreatedAtDesc(String status);
}