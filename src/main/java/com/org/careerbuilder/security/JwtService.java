package com.org.careerbuilder.security;

import com.org.careerbuilder.models.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final Key key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret:replace-with-32+char-super-secret-key-1234567890}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(AppUser user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        Long schoolId = resolveSchoolId(user);

        return Jwts.builder()
                .setSubject(user.getEmail() != null ? user.getEmail() : user.getMobile())
                .claim("userId", user.getId())
                .claim("studentId", user.getStudent() != null ? user.getStudent().getId() : null)
                .claim("schoolId", schoolId)
                .claim("role", user.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }

    private Long resolveSchoolId(AppUser user) {
        if (user.getSchool() != null) {
            return user.getSchool().getId();
        }
        if (user.getStudent() != null && user.getStudent().getSchool() != null) {
            return user.getStudent().getSchool().getId();
        }
        return null;
    }

    public Claims parseAndValidate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isExpired(Claims claims) {
        Date exp = claims.getExpiration();
        return exp != null && exp.before(new Date());
    }
}