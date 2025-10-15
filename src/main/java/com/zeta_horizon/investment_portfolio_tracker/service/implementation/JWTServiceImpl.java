package com.zeta_horizon.investment_portfolio_tracker.service.implementation;

import com.zeta_horizon.investment_portfolio_tracker.entity.User;
import com.zeta_horizon.investment_portfolio_tracker.service.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




@Service
public class JWTServiceImpl implements JWTService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of("ROLE_" + user.getRole().name()));
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .and()
                .signWith(generateKey())
                .compact();
    }

    /**
     * Extracts the username (email) from the given JWT token.
     * @param token JWT token
     * @return Username (subject field of the token)
     */
    @Override
    public String extractUsername(String token) {
        return getAllClaims(token).getSubject(); // 'subject' field contains the username/email
    }

    /**
     * Parses the JWT and returns all claims (payload data).
     * @param token JWT token
     * @return Claims (like subject, expiration, etc.)
     */
    private Claims getAllClaims(String token) {
        return Jwts
                .parser()                          // Create a parser to parse the token
                .verifyWith(generateKey())         // Set the signing key to validate signature
                .build()                           // Build the parser
                .parseSignedClaims(token)          // Parse the token and verify the signature
                .getPayload();                     // Get the claims (subject, expiration, etc.)
    }



    private SecretKey generateKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    // Validating the token
    /**
     * Validates that the token belongs to the given user and is not expired.
     * @param token JWT token
     * @param userDetails UserDetails object to compare with token data
     * @return true if token is valid and belongs to the user
     */
    @Override
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token); // Extract username from token
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token); // Check match and expiration
    }

    @Override
    public List<String> extractRoles(String token) {
        Object rolesObj = getAllClaims(token).get("roles");

        System.out.println(rolesObj);
        if (rolesObj instanceof List<?>) {
            return ((List<?>) rolesObj).stream()
                    .map(Object::toString)
                    .toList();
        }
        return List.of();
    }

    /**
     * Checks if the token is expired.
     * @param token JWT token
     * @return true if expired
     */
    private boolean isTokenExpired(String token) {
        return getAllClaims(token).getExpiration().before(new Date()); // Compare token expiry with current time
    }



}
