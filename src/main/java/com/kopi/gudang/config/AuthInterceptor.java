package com.kopi.gudang.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Check if user session exists
        Object user = request.getSession().getAttribute("user");
        if (user == null) {
            // Redirect to login page
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }
}
