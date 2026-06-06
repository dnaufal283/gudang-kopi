package com.kopi.gudang.service;

import com.kopi.gudang.entity.Product;
import com.kopi.gudang.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository repo;

    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    public Product saveProduct(Product product) {
        if (product.getStock() == null) {
            product.setStock(0);
        }
        return repo.save(product);
    }

    public Product getProductById(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Produk tidak ditemukan dengan ID: " + id));
    }

    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }
        return repo.findByNameContainingIgnoreCaseOrTypeContainingIgnoreCase(query, query);
    }

    public void deleteProduct(Long id) {
        repo.deleteById(id);
    }
}