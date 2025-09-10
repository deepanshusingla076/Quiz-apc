package com.qwizz.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/jwt")
public class JwtSecretController {

    @GetMapping("/generate-secret")
    public ResponseEntity<Map<String, String>> generateJwtSecret() {
        // Generate a secure random 256-bit (32 bytes) secret key
        SecureRandom secureRandom = new SecureRandom();
        byte[] secretBytes = new byte[32];
        secureRandom.nextBytes(secretBytes);
        
        // Encode to Base64 for easy storage
        String base64Secret = Base64.getEncoder().encodeToString(secretBytes);
        
        // Create response
        Map<String, String> response = new HashMap<>();
        response.put("secret", base64Secret);
        response.put("message", "Copy this secret key to your application.properties file");
        response.put("property", "jwt.secret=" + base64Secret);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/generate-hex-secret")
    public ResponseEntity<Map<String, String>> generateHexJwtSecret() {
        // Generate a secure random 256-bit (32 bytes) secret key
        SecureRandom secureRandom = new SecureRandom();
        byte[] secretBytes = new byte[32];
        secureRandom.nextBytes(secretBytes);
        
        // Convert to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : secretBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        String hexSecret = hexString.toString();
        
        // Create response
        Map<String, String> response = new HashMap<>();
        response.put("secret", hexSecret);
        response.put("message", "Copy this hex secret key to your application.properties file");
        response.put("property", "jwt.secret=" + hexSecret);
        
        return ResponseEntity.ok(response);
    }
}
