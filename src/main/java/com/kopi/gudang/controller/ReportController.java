package com.kopi.gudang.controller;

import com.kopi.gudang.entity.StockTransaction;
import com.kopi.gudang.entity.User;
import com.kopi.gudang.service.ProductService;
import com.kopi.gudang.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
public class ReportController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ProductService productService;

    @GetMapping("/reports")
    public String viewReports(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            Model model,
            HttpSession session) {

        // Proteksi: Hanya Admin yang bisa mengakses Laporan
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return "redirect:/";
        }
        model.addAttribute("currentUser", currentUser);

        LocalDateTime start = null;
        LocalDateTime end = null;

        try {
            if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                start = LocalDate.parse(startDateStr).atStartOfDay();
            }
            if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                end = LocalDate.parse(endDateStr).atTime(LocalTime.MAX);
            }
        } catch (Exception ex) {
            model.addAttribute("error", "Format tanggal tidak valid!");
        }

        // Tarik data yang sudah disaring dari Database
        List<StockTransaction> filtered = transactionService.filterTransactions(type, productId, start, end);

        // Kalkulasi Statistik
        int totalIn = filtered.stream()
                .filter(t -> "IN".equalsIgnoreCase(t.getType()))
                .mapToInt(StockTransaction::getQuantity)
                .sum();
        int totalOut = filtered.stream()
                .filter(t -> "OUT".equalsIgnoreCase(t.getType()))
                .mapToInt(StockTransaction::getQuantity)
                .sum();
        int netChange = totalIn - totalOut;

        model.addAttribute("totalIn", totalIn);
        model.addAttribute("totalOut", totalOut);
        model.addAttribute("netChange", netChange);

        // Kembalikan nilai filter ke form agar tidak reset saat halaman dimuat ulang
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedProductId", productId);
        model.addAttribute("selectedStartDate", startDateStr);
        model.addAttribute("selectedEndDate", endDateStr);

        model.addAttribute("transactions", filtered);
        model.addAttribute("products", productService.getAllProducts());

        return "reports";
    }
}