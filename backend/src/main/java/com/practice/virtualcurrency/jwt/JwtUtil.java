package com.practice.virtualcurrency.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.practice.virtualcurrency.VirtualCurrencyConst.EXPIRED_TIME;

public class JwtUtil {
    private static String secretKey = "secret-key-virtual-currency-123";

    public static String generateToken(String username,String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() +EXPIRED_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public static Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

    }

    public static String extractEmail(String token) {
        return extractClaims(token).get("email").toString();
    }

    public static String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public static boolean validateToken(String token, UserDetails userDetails) {
        return (userDetails.getUsername().equals(extractUsername(token)) && !isTokenExpired(token));
    }

    public static boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}
