package com.kopi.gudang.controller;

import com.kopi.gudang.entity.User;
import com.kopi.gudang.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.MessageDigest;
import java.math.BigInteger;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 1. Menampilkan Halaman Kelola Akun
    @GetMapping("/users")
    public String viewUsers(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        // Proteksi: Hanya Admin yang boleh masuk
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return "redirect:/";
        }

        // Ambil semua data pengguna dari database
        List<User> userList = userRepository.findAll();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", userList);
        return "users"; // Mengarah ke file users.html nanti
    }

    // 2. Menyimpan Akun Baru
    @PostMapping("/users/add")
    public String addUser(
            @RequestParam String fullName,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,
            HttpSession session,
            RedirectAttributes redirectAttrs) {

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return "redirect:/";
        }

        // PERBAIKAN: Sesuaikan dengan Optional<User> dari UserRepository
        java.util.Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            redirectAttrs.addFlashAttribute("error", "Gagal! Username '" + username + "' sudah terdaftar.");
            return "redirect:/users";
        }

        // Buat dan simpan akun baru
        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setUsername(username);
        // PERBAIKAN: Hash password sebelum disimpan ke database!
        newUser.setPassword(hashPassword(password.trim()));
        newUser.setRole(role.toUpperCase());

        userRepository.save(newUser);

        redirectAttrs.addFlashAttribute("success",
                "Akun " + role + " atas nama " + fullName + " berhasil didaftarkan!");
        return "redirect:/users";
    }

    // 3. Menghapus Akun
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttrs) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return "redirect:/";
        }

        // Proteksi: Admin tidak boleh menghapus akunnya sendiri yang sedang dipakai
        // login
        if (currentUser.getId().equals(id)) {
            redirectAttrs.addFlashAttribute("error", "Ditolak! Anda tidak dapat menghapus akun Anda sendiri.");
            return "redirect:/users";
        }

        userRepository.deleteById(id);
        redirectAttrs.addFlashAttribute("success", "Akun berhasil dihapus dari sistem.");
        return "redirect:/users";
    }

    // Fungsi untuk mengubah teks biasa menjadi kode Hash SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(password.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
