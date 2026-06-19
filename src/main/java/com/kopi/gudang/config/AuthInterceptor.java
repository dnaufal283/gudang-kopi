package com.kopi.gudang.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.kopi.gudang.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.sendRedirect("/login");
            return false;
        }

        String uri = request.getRequestURI();
        String role = currentUser.getRole();

        // BLOKIR STAFF: Staff dilarang ke Produk, Laporan, dan Supplier.
        // URL "/" SEKARANG DIIZINKAN AGAR BISA MASUK KE STAFF-DASHBOARD
        if ("STAFF".equals(role)) {
            if (uri.startsWith("/products") || uri.startsWith("/reports") || uri.startsWith("/suppliers")) {
                response.sendRedirect("/transactions");
                return false;
            }
        }

        // BLOKIR ADMIN: Admin dilarang ke input transaksi harian
        if ("ADMIN".equals(role)) {
            if (uri.startsWith("/transactions")) {
                response.sendRedirect("/");
                return false;
            }
        }

        return true;
    }
}