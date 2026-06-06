package com.kopi.gudang.controller;

import com.kopi.gudang.entity.StockTransaction;
import com.kopi.gudang.entity.User;
import com.kopi.gudang.repository.StockTransactionRepository;
import com.kopi.gudang.service.ProductService;
import com.kopi.gudang.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockTransactionRepository transactionRepo;

    @GetMapping("/transactions")
    public String viewTransactions(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("transactions", transactionService.getAllTransactions());
        model.addAttribute("products", productService.getAllProducts());
        return "transactions"; // templates/transactions.html
    }

    @PostMapping("/transactions")
    public String addTransaction(
            @RequestParam("productId") Long productId,
            @RequestParam("type") String type,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("notes") String notes,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("user");
        String performedBy = (currentUser != null) ? currentUser.getUsername() : "unknown";

        try {
            StockTransaction tx = transactionService.recordTransaction(productId, type, quantity, notes, performedBy);
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
}
