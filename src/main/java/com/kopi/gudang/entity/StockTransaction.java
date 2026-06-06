package com.kopi.gudang.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transactions")
@Data
public class StockTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private Product product;
    
    private String type; // "IN" or "OUT"
    private Integer quantity;
    private String notes;
    private LocalDateTime transactionDate;
    private String performedBy; // Username of user performing transaction
    private String invoiceNumber; // Generated invoice number for OUT transactions
}
