package com.kopi.gudang.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List; // TAMBAHAN: Import entity Supplier

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // TAMBAHAN: Import repository Supplier

import com.kopi.gudang.entity.Product;
import com.kopi.gudang.entity.StockTransaction;
import com.kopi.gudang.entity.Supplier;
import com.kopi.gudang.repository.ProductRepository;
import com.kopi.gudang.repository.StockTransactionRepository;
import com.kopi.gudang.repository.SupplierRepository;

@Service
public class TransactionService {

    @Autowired
    private StockTransactionRepository transactionRepo;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository; // TAMBAHAN: Autowire repository

    @Transactional
    // TAMBAHAN: Tambah parameter Integer supplierId di akhir
    public StockTransaction recordTransaction(Long productId, String type, Integer quantity, String notes,
            String performedBy, Integer supplierId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produk tidak ditemukan dengan ID: " + productId));

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Jumlah transaksi harus lebih besar dari 0!");
        }

        // TAMBAHAN: Logika validasi dan pencarian data Supplier
        Supplier supplier = null;

        if ("IN".equalsIgnoreCase(type)) {
            // Kalau barang masuk, supplier wajib ada
            if (supplierId == null) {
                throw new IllegalArgumentException("Supplier wajib dipilih untuk transaksi penerimaan barang (IN)!");
            }
            supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new IllegalArgumentException("Data Supplier tidak ditemukan!"));

            product.setStock(product.getStock() + quantity);

        } else if ("OUT".equalsIgnoreCase(type)) {
            if (product.getStock() < quantity) {
                throw new IllegalArgumentException(
                        "Stok tidak mencukupi! Stok saat ini: " + product.getStock() + " Kg.");
            }
            product.setStock(product.getStock() - quantity);

        } else {
            throw new IllegalArgumentException("Tipe transaksi tidak valid! Harus 'IN' atau 'OUT'.");
        }

        // Save updated product
        productRepository.save(product);

        // Record transaction
        StockTransaction transaction = new StockTransaction();
        transaction.setProduct(product);
        transaction.setType(type.toUpperCase());
        transaction.setQuantity(quantity);
        transaction.setNotes(notes);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setPerformedBy(performedBy);

        // TAMBAHAN: Masukkan data supplier ke dalam transaksi
        transaction.setSupplier(supplier);

        if ("OUT".equalsIgnoreCase(type)) {
            // Generate a unique invoice number
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            transaction.setInvoiceNumber("INV-" + timestamp);
        }

        return transactionRepo.save(transaction);
    }

    public List<StockTransaction> getAllTransactions() {
        return transactionRepo.findAllByOrderByTransactionDateDesc();
    }

    public List<StockTransaction> filterTransactions(String type, Long productId, LocalDateTime startDate,
            LocalDateTime endDate) {
        // Normalize empty inputs to null
        String searchType = (type == null || type.trim().isEmpty() || "ALL".equalsIgnoreCase(type)) ? null
                : type.toUpperCase();
        Long searchProductId = (productId == null || productId == 0) ? null : productId;
        return transactionRepo.filterTransactions(searchType, searchProductId, startDate, endDate);
    }

    // Mengambil satu data transaksi spesifik untuk dicetak
    public StockTransaction getTransactionById(Long id) {
        return transactionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Data transaksi tidak ditemukan!"));
    }
}