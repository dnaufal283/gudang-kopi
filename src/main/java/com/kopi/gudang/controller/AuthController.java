package com.kopi.gudang.controller;

import com.kopi.gudang.entity.User;
import com.kopi.gudang.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginForm(HttpSession session) {
        // If already logged in, redirect to dashboard
        if (session.getAttribute("user") != null) {
            return "redirect:/";
        }
        return "login"; // templates/login.html
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {
        
        if (userService.authenticate(username, password)) {
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isPresent()) {
                session.setAttribute("user", userOpt.get());
                return "redirect:/";
            }
        }
        
        model.addAttribute("error", "Username atau password salah!");
        return "login";
    }

    @GetMapping("/logout")
    public String processLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
