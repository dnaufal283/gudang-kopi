package com.kopi.gudang.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kopi.gudang.entity.LogNotifikasi;
import com.kopi.gudang.entity.Product;
import com.kopi.gudang.entity.StockTransaction;
import com.kopi.gudang.entity.User;
import com.kopi.gudang.entity.ShiftNote;
import com.kopi.gudang.repository.LogNotifikasiRepository;
import com.kopi.gudang.repository.ShiftNoteRepository;
import com.kopi.gudang.service.ProductService;
import com.kopi.gudang.service.TransactionService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProductController {

    @Autowired
    private ShiftNoteRepository shiftNoteRepo;

    @Autowired
    private ProductService service;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LogNotifikasiRepository logNotifikasiRepo;

    // =======================================================
    // --- ENDPOINT DASHBOARD UTAMA ---
    // =======================================================
    @GetMapping("/")
    public String viewDashboard(
            @RequestParam(value = "search", required = false) String search,
            Model model,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentUser", currentUser);

        // Cari produk dengan stok kritis (< 10 Kg)
        List<Product> allProducts = service.getAllProducts();
        List<Product> lowStockProducts = allProducts.stream()
                .filter(p -> p.getStock() < 10)
                .toList();
        model.addAttribute("lowStockProducts", lowStockProducts);

        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            List<Product> products = service.searchProducts(search);
            model.addAttribute("products", products);
            model.addAttribute("searchQuery", search);
            model.addAttribute("totalVariants", allProducts.size());
            model.addAttribute("totalStock", allProducts.stream().mapToInt(Product::getStock).sum());
            model.addAttribute("lowStockCount", lowStockProducts.size());

            List<StockTransaction> allTxs = transactionService.getAllTransactions();
            model.addAttribute("recentTransactions", allTxs.stream().limit(5).toList());

            List<LogNotifikasi> unreadNotifications = logNotifikasiRepo.findByStatusOrderByCreatedAtDesc("UNREAD");
            model.addAttribute("notifications", unreadNotifications);

            return "admin-dashboard";

        } else {
            List<StockTransaction> allTxs = transactionService.getAllTransactions();
            model.addAttribute("recentTransactions", allTxs.stream().limit(5).toList());
            model.addAttribute("shiftNotes", shiftNoteRepo.findTop5ByOrderByWaktuDibuatDesc());

            return "staff-dashboard";
        }
    }

    // =======================================================
    // --- ENDPOINT KOMUNIKASI & NOTIFIKASI ---
    // =======================================================

    // Staff mengirim laporan stok menipis
    @PostMapping("/staff/report-low-stock")
    public String reportLowStock(
            @RequestParam("productName") String productName,
            @RequestParam("currentStock") Integer currentStock,
            RedirectAttributes redirectAttributes) {

        LogNotifikasi notif = new LogNotifikasi();
        notif.setPesan(
                "Laporan Staff: Stok varian '" + productName + "' menipis! Sisa saat ini: " + currentStock + " Kg.");
        notif.setStatus("UNREAD");
        notif.setCreatedAt(LocalDateTime.now());
        logNotifikasiRepo.save(notif);

        redirectAttributes.addFlashAttribute("success", "Laporan berhasil dikirim ke Admin!");
        return "redirect:/";
    }

    // Admin menandai laporan sudah dibaca/selesai
    @PostMapping("/admin/notifications/read/{id}")
    public String markNotificationAsRead(@PathVariable("id") Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            logNotifikasiRepo.findById(id).ifPresent(notif -> {
                notif.setStatus("READ");
                logNotifikasiRepo.save(notif);
            });
        }
        return "redirect:/";
    }

    // Staff menambah catatan operan shift
    @PostMapping("/staff/notes/add")
    public String addShiftNote(@RequestParam("pesan") String pesan, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null && "STAFF".equalsIgnoreCase(currentUser.getRole())) {
            ShiftNote note = new ShiftNote();
            note.setPesan(pesan);
            note.setDibuatOleh(currentUser.getFullName());
            note.setWaktuDibuat(LocalDateTime.now());
            shiftNoteRepo.save(note);
        }
        return "redirect:/";
    }

    // =======================================================
    // --- ENDPOINT KELOLA DATA KOPI (KHUSUS ADMIN) ---
    // =======================================================

    // 1. Menampilkan Halaman Kelola Kopi
    @GetMapping("/products")
    public String viewProductsPage(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return "redirect:/";
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("products", service.getAllProducts());
        return "products";
    }

    // 2. Menyimpan Kopi Baru
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product, RedirectAttributes redirectAttrs, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            try {
                service.saveProduct(product);
                redirectAttrs.addFlashAttribute("success", "Berhasil menambahkan varian kopi baru!");
            } catch (Exception ex) {
                redirectAttrs.addFlashAttribute("error", "Gagal menyimpan: " + ex.getMessage());
            }
        }
        return "redirect:/products";
    }

    // 3. Mengupdate/Edit Data Kopi
    @PostMapping("/products/update/{id}")
    public String updateProduct(@PathVariable("id") Long id, @ModelAttribute Product product,
            RedirectAttributes redirectAttrs, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            try {
                Product existing = service.getProductById(id);
                if (existing != null) {
                    existing.setName(product.getName());
                    existing.setType(product.getType());
                    existing.setPrice(product.getPrice());
                    existing.setStock(product.getStock());
                    service.saveProduct(existing);
                    redirectAttrs.addFlashAttribute("success", "Data kopi berhasil diperbarui!");
                }
            } catch (Exception ex) {
                redirectAttrs.addFlashAttribute("error", "Gagal update: " + ex.getMessage());
            }
        }
        return "redirect:/products";
    }

    // 4. Menghapus Data Kopi
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttrs, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            try {
                service.deleteProduct(id);
                redirectAttrs.addFlashAttribute("success", "Produk kopi berhasil dihapus dari sistem!");
            } catch (Exception ex) {
                redirectAttrs.addFlashAttribute("error", "Gagal hapus: " + ex.getMessage());
            }
        }
        return "redirect:/products";
    }
}