package com.takehome.stayease.security;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.takehome.stayease.entity.Role;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final Key key;
    private final long jwtExpirationInMillis;
    private final JwtParser jwtParser;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, @Value("${jwt.expiration}") long jwtExpirationInMillis) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.jwtExpirationInMillis = jwtExpirationInMillis;
        this.jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    public String createToken(String email, Role role) {
        logger.info("Creating JWT token for email: {}", email);

        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtExpirationInMillis);

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.name())
                .signWith(key, SignatureAlgorithm.HS256)
                .setIssuedAt(now)
                .setExpiration(validity)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            jwtParser.parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired");
            return false;
        } catch (JwtException e) {
            logger.warn("JWT token is invalid");
            return false;
        } catch (Exception e) {
            logger.error("An error occurred while validating JWT token", e);
            return false;
        }
    }

    public String getUsername(String token) {
        try {
            return jwtParser.parseClaimsJws(token).getBody().getSubject();
        } catch (JwtException e) {
            logger.error("An error occurred while extracting username from JWT token", e);
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        try {
            return jwtParser.parseClaimsJws(token).getBody().get("role", String.class);
        } catch (JwtException e) {
            logger.error("An error occurred while extracting role from JWT token", e);
            return null;
        }
    }
}
