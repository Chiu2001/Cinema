package com.example.backend.Security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.backend.Exception.InvalidTokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    
    private static final String SECRET_KEY = 
        "T8qJvKSoH/kU7vDWIQ/YFqkti03oRdSN4X+2vSF/b1D9JLfMqx5EiUNrOnNV5hZm";

    
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    
    public String generateToken(String email) {
        return generateToken(Map.of(), email, 1000 * 60 * 60); // Default expiration is 1 hour
    }

    public String generateToken(String email, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles); // Add the roles to the claims
        return generateToken(claims, email, 1000 * 60 * 60 * 24); // Token expiration is set to 24 hours
    }


    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                                   .map(GrantedAuthority::getAuthority)
                                   .collect(Collectors.toList()));
        return generateToken(claims, userDetails.getUsername(), 1000 * 60 * 60 * 24); // Default expiration is 24 hours
    }

    // Generate a JWT and set some default claims
    private String generateToken(Map<String, Object> claims, String subject, long expirationMillis) {
        return Jwts.builder()
                .setClaims(claims) // Set the custom claims
                .setSubject(subject) // Set the subject to the username
                .setIssuedAt(new Date(System.currentTimeMillis())) // Set the issued-at time
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis)) // Set the expiration time
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Sign the JWT using a fixed secret key and the HMAC SHA-256 signing algorithm
                .compact();
    }

    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    
    public boolean isTokenValid(String token, UserDetails userDetails, List<String> requiredRoles) {
        final String username = extractUsername(token);
        boolean isValidUser = username.equals(userDetails.getUsername()) && !isTokenExpired(token);

        if (isValidUser) {
            List<String> roles = extractRoles(token);
            return requiredRoles.stream().allMatch(roles::contains);
        }

        return false;
    }

    // Check whether the JWT has expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extract the expiration time from the JWT
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract the roles from the JWT
    private List<String> extractRoles(String token) {
        return extractClaim(token, claims -> {
            Object rolesClaim = claims.get("roles");
            if (rolesClaim instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) rolesClaim;
                return roles;
            }
            return List.of(); // Return an empty list in case of an unexpected type
        });
    }

    
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid JWT token");
        }
    }
}

