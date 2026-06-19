package com.kopi.gudang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kopi.gudang.entity.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    // Kosong saja! Spring Data JPA sudah otomatis menyiapkan fungsi
    // simpan (save), cari (findById), hapus (delete), dll di belakang layar.
}