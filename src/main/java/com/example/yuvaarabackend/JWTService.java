package com.example.yuvaarabackend;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JWTService {
    private final static String SECRETKEY = "vKyEUpmj9jysvXovhZ4U0hxYyAjJY4F7z6Ob1yhrQKQ=";

    public static Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRETKEY);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        return key;
    }

    public static String create(String email, Integer id) {
        try {
            String jwt = Jwts.builder()
                    .claim("id", id)
                    .claim("email", email)
                    .setIssuedAt(new Date())
                    .signWith(getKey())
                    .compact();

            return jwt;
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
