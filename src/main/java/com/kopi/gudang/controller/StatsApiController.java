package com.kopi.gudang.controller;

import com.kopi.gudang.entity.Product;
import com.kopi.gudang.entity.StockTransaction;
import com.kopi.gudang.service.ProductService;
import com.kopi.gudang.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsApiController {

    @Autowired
    private ProductService productService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/stock")
    public List<Map<String, Object>> getStockStats() {
        List<Product> products = productService.getAllProducts();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Product p : products) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", p.getName());
            data.put("stock", p.getStock());
            data.put("type", p.getType());
            result.add(data);
        }
        return result;
    }

    @GetMapping("/flow")
    public List<Map<String, Object>> getTransactionFlow() {
        List<StockTransaction> txs = transactionService.getAllTransactions();
        List<Map<String, Object>> result = new ArrayList<>();
        // Get up to 10 most recent transactions (reversing order for logical chronological layout on chart)
        int count = Math.min(txs.size(), 10);
        for (int i = count - 1; i >= 0; i--) {
            StockTransaction tx = txs.get(i);
            Map<String, Object> data = new HashMap<>();
            data.put("date", tx.getTransactionDate().toLocalDate().toString());
            data.put("product", tx.getProduct().getName());
            data.put("type", tx.getType());
            data.put("quantity", tx.getQuantity());
            result.add(data);
        }
        return result;
    }
}
