package com.kopi.gudang.controller;

import java.security.MessageDigest;
import java.math.BigInteger;
import com.kopi.gudang.entity.User;
import com.kopi.gudang.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public String viewProfile(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        // Proteksi: Jika belum login, tendang ke halaman login utama
        if (currentUser == null) {
            return "redirect:/";
        }

        model.addAttribute("currentUser", currentUser);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "oldPassword", required = false) String oldPassword,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/";
        }

        try {
            Optional<User> userOptional = userRepository.findById(currentUser.getId());
            if (!userOptional.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Pengguna tidak ditemukan di database!");
                return "redirect:/profile";
            }

            User userToUpdate = userOptional.get();

            // Validasi nama lengkap
            if (fullName != null && !fullName.trim().isEmpty()) {
                userToUpdate.setFullName(fullName.trim());
            } else {
                redirectAttributes.addFlashAttribute("error", "Nama lengkap tidak boleh kosong!");
                return "redirect:/profile";
            }

            // Logika Ganti Password dengan Verifikasi Hash
            boolean isTryingToChangePassword = (newPassword != null && !newPassword.trim().isEmpty());

            if (isTryingToChangePassword) {
                // Pastikan password lama juga diisi
                if (oldPassword == null || oldPassword.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error",
                            "Gagal: Anda harus memasukkan Kata Sandi Lama untuk mengganti kata sandi!");
                    return "redirect:/profile";
                }

                // Ubah password lama yang diinput menjadi Hash agar seimbang saat diadu
                String hashedOldPassword = hashPassword(oldPassword);

                // Cek apakah password lama (yang sudah di-hash) SAMA dengan yang ada di
                // database
                if (!hashedOldPassword.equals(userToUpdate.getPassword())) {
                    redirectAttributes.addFlashAttribute("error", "Gagal: Kata Sandi Lama yang Anda masukkan salah!");
                    return "redirect:/profile";
                }

                // Jika cocok, update dengan password baru yang juga WAJIB di-hash
                userToUpdate.setPassword(hashPassword(newPassword.trim()));
            }

            // Simpan perubahan ke database
            userRepository.save(userToUpdate);

            // Update session agar nama di sidebar langsung berubah
            session.setAttribute("user", userToUpdate);

            redirectAttributes.addFlashAttribute("success", "Profil Anda berhasil diperbarui!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan: " + e.getMessage());
        }

        return "redirect:/profile";
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