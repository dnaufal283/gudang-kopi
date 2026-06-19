package com.kopi.gudang.repository;

import com.kopi.gudang.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    List<StockTransaction> findAllByOrderByTransactionDateDesc();

    @Query("SELECT t FROM StockTransaction t WHERE " +
            "(:type IS NULL OR t.type = :type) AND " +
            "(:productId IS NULL OR t.product.id = :productId) AND " +
            "(:startDate IS NULL OR t.transactionDate >= :startDate) AND " +
            "(:endDate IS NULL OR t.transactionDate <= :endDate) " +
            "ORDER BY t.transactionDate DESC")
    List<StockTransaction> filterTransactions(
            @Param("type") String type,
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}