package com.kopi.gudang.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping; // TAMBAHAN 1: Import repository supplier
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kopi.gudang.entity.StockTransaction;
import com.kopi.gudang.entity.User;
import com.kopi.gudang.repository.StockTransactionRepository;
import com.kopi.gudang.repository.SupplierRepository;
import com.kopi.gudang.service.ProductService;
import com.kopi.gudang.service.TransactionService;

import jakarta.servlet.http.HttpSession;

@Controller
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockTransactionRepository transactionRepo;

    @Autowired
    private SupplierRepository supplierRepo; // TAMBAHAN 1: Autowire repository supplier

    @GetMapping("/transactions")
    public String viewTransactions(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        model.addAttribute("currentUser", currentUser);
        // Tarik semua data dari database
        List<StockTransaction> allTxs = transactionService.getAllTransactions();

        // LOGIKA FILTER KHUSUS STAFF
        if ("STAFF".equalsIgnoreCase(currentUser.getRole())) {
            // Saring (Filter) menggunakan Java Stream: Cocokkan nama di kolom "Oleh" dengan
            // Username yang sedang login
            List<StockTransaction> staffTxs = allTxs.stream()
                    .filter(tx -> currentUser.getUsername().equalsIgnoreCase(tx.getPerformedBy()))
                    .toList();
            model.addAttribute("transactions", staffTxs);
        } else {
            // Admin bebas melihat semua riwayat
            model.addAttribute("transactions", allTxs);
        }
        model.addAttribute("products", productService.getAllProducts());

        // TAMBAHAN 2: Lempar data semua supplier ke dropdown di HTML
        model.addAttribute("suppliers", supplierRepo.findAll());

        return "transactions"; // templates/transactions.html
    }

    @PostMapping("/transactions")
    public String addTransaction(
            @RequestParam("productId") Long productId,
            @RequestParam("type") String type,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("notes") String notes,
            // TAMBAHAN 3: Tangkap id supplier dari form (required=false karena transaksi
            // OUT gak butuh supplier)
            @RequestParam(value = "supplierId", required = false) Integer supplierId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("user");
        String performedBy = (currentUser != null) ? currentUser.getUsername() : "unknown";

        try {
            // TAMBAHAN 3: Masukkan supplierId ke dalam fungsi pencatatan transaksi
            StockTransaction tx = transactionService.recordTransaction(productId, type, quantity, notes, performedBy,
                    supplierId);
            redirectAttributes.addFlashAttribute("success", "Transaksi berhasil dicatat!");
            if ("OUT".equalsIgnoreCase(type)) {
                // If it is an OUT transaction, we can provide a link to view/print the invoice
                redirectAttributes.addFlashAttribute("invoiceId", tx.getId());
                redirectAttributes.addFlashAttribute("invoiceNum", tx.getInvoiceNumber());
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/transactions";
    }

    @GetMapping("/transactions/invoice/{id}")
    public String viewInvoice(@PathVariable("id") Long id, Model model) {
        StockTransaction transaction = transactionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi tidak ditemukan!"));

        if (!"OUT".equalsIgnoreCase(transaction.getType())) {
            throw new IllegalArgumentException("Invoice hanya tersedia untuk transaksi OUT (Keluar)!");
        }

        model.addAttribute("tx", transaction);
        return "invoice"; // templates/invoice.html
    }

    @GetMapping("/transactions/print/{id}")
    public String printInvoice(@PathVariable("id") Long id, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        StockTransaction transaction = transactionService.getTransactionById(id);

        // Pastikan hanya transaksi OUT (Keluar) yang bisa dicetak invoice-nya
        if (!"OUT".equalsIgnoreCase(transaction.getType())) {
            return "redirect:/transactions";
        }

        model.addAttribute("tx", transaction);
        return "invoice";
    }

    // --- ENDPOINT UNTUK CETAK REKAP SHIFT STAFF ---
    @GetMapping("/transactions/recap")
    public String printStaffRecap(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        // Pastikan hanya Staff yang bisa mengakses halaman ini
        if (currentUser == null || !"STAFF".equalsIgnoreCase(currentUser.getRole())) {
            return "redirect:/";
        }

        // Tarik data dan saring khusus milik Staff yang sedang login
        List<StockTransaction> allTxs = transactionService.getAllTransactions();
        List<StockTransaction> staffTxs = allTxs.stream()
                .filter(tx -> currentUser.getUsername().equalsIgnoreCase(tx.getPerformedBy()))
                .toList();

        model.addAttribute("transactions", staffTxs);
        model.addAttribute("staffName", currentUser.getFullName());
        model.addAttribute("printDate", java.time.LocalDateTime.now());

        return "staff-recap"; // Memanggil file HTML baru
    }
}