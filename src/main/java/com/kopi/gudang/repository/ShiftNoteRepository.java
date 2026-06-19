package com.kopi.gudang.repository;

import com.kopi.gudang.entity.ShiftNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShiftNoteRepository extends JpaRepository<ShiftNote, Long> {
    // Otomatis mengambil 5 catatan operan paling baru
    List<ShiftNote> findTop5ByOrderByWaktuDibuatDesc();
}