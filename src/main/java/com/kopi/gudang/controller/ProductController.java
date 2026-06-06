package com.kopi.gudang.controller;

import com.kopi.gudang.entity.Product;
import com.kopi.gudang.entity.User;
import com.kopi.gudang.entity.StockTransaction;
import com.kopi.gudang.service.ProductService;
import com.kopi.gudang.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductService service;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/")
    public String viewDashboard(
            @RequestParam(value = "search", required = false) String search,
            Model model,
            HttpSession session) {
        
        // Get user session info
        User currentUser = (User) session.getAttribute("user");
        model.addAttribute("currentUser", currentUser);

        // Fetch products based on search query
        List<Product> products = service.searchProducts(search);
        model.addAttribute("products", products);
        model.addAttribute("searchQuery", search);

        // Aggregate statistics
        List<Product> allProducts = service.getAllProducts();
        int totalVariants = allProducts.size();
        int totalStock = allProducts.stream().mapToInt(Product::getStock).sum();
        long lowStockCount = allProducts.stream().filter(p -> p.getStock() < 10).count();

        model.addAttribute("totalVariants", totalVariants);
        model.addAttribute("totalStock", totalStock);
        model.addAttribute("lowStockCount", lowStockCount);

        // Fetch low stock products to display warnings in notifications
        List<Product> lowStockProducts = allProducts.stream()
                .filter(p -> p.getStock() < 10)
                .toList();
        model.addAttribute("lowStockProducts", lowStockProducts);

        // Fetch recent transactions
        List<StockTransaction> allTxs = transactionService.getAllTransactions();
        List<StockTransaction> recentTxs = allTxs.stream().limit(5).toList();
        model.addAttribute("recentTransactions", recentTxs);

        return "dashboard"; // dashboard.html
    }

    @PostMapping("/products/add")
    public String addProduct(
            @ModelAttribute Product product,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Akses ditolak! Hanya Administrator yang dapat menambah produk.");
            return "redirect:/";
        }

        try {
            service.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", "Produk berhasil ditambahkan!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Gagal menambahkan produk: " + ex.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/products/edit")
    public String editProduct(
            @ModelAttribute Product product,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Akses ditolak! Hanya Administrator yang dapat mengubah produk.");
            return "redirect:/";
        }

        try {
            Product existing = service.getProductById(product.getId());
            existing.setName(product.getName());
            existing.setType(product.getType());
            existing.setPrice(product.getPrice());
            existing.setStock(product.getStock());
            
            service.saveProduct(existing);
            redirectAttributes.addFlashAttribute("success", "Produk berhasil diperbarui!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Gagal memperbarui produk: " + ex.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(
            @PathVariable("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Akses ditolak! Hanya Administrator yang dapat menghapus produk.");
            return "redirect:/";
        }

        try {
            service.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Produk berhasil dihapus!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus produk: " + ex.getMessage());
        }
        return "redirect:/";
    }
}