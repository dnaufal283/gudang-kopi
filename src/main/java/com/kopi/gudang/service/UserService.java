package com.kopi.gudang.service;

import com.kopi.gudang.entity.User;
import com.kopi.gudang.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    public Optional<User> findByUsername(String username) {
        return repo.findByUsername(username);
    }

    public User registerUser(User user) {
        user.setPassword(hashPassword(user.getPassword()));
        return repo.save(user);
    }

    public boolean authenticate(String username, String password) {
        Optional<User> optUser = repo.findByUsername(username);
        if (optUser.isPresent()) {
            User user = optUser.get();
            return user.getPassword().equals(hashPassword(password));
        }
        return false;
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error hashing password", ex);
        }
    }
}
