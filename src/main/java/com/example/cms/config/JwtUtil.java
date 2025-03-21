package com.example.cms.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY; // Ít nhất 256-bit
        private static final long EXPIRATION_TIME_ACCESS = 86400000; // 1 ngày
//    private static final long EXPIRATION_TIME_ACCESS = 60000; // 1 phút
        private static final long EXPIRATION_TIME_REFRESH = 86400000 * 7; // 7 ngày
//    private static final long EXPIRATION_TIME_REFRESH = 60000 * 2; // 2 phút

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateAccessToken(String accountId, Integer typeAccount) {
        return Jwts.builder()
                .setSubject(accountId)
                .claim("typeAccount", typeAccount)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_ACCESS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String generateRefreshToken(String accountId) {
        return Jwts.builder()
                .setSubject(accountId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_REFRESH))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // Phương thức kiểm tra token hết hạn
    public boolean isTokenExpired(String token) {
        try {
            // Parse token để lấy claims
            Claims claims = getClaimsFromToken(token);

            // Lấy thời gian hết hạn từ token
            Date expiration = claims.getExpiration();

            // So sánh với thời gian hiện tại
            return expiration.before(new Date());
        } catch (Exception e) {
            // Xử lý trường hợp token không hợp lệ
            return true; // Nếu có lỗi, coi như token đã hết hạn
        }
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isAdminToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Integer typeAccount = claims.get("typeAccount", Integer.class);
            return typeAccount != null && typeAccount == 2; // Kiểm tra typeAccount = 2 (quản trị)
        } catch (Exception e) {
            return false;
        }
    }

    // retrieve username from jwt token
    public String extractClientIdFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public String getToken(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        if (header == null) {
            return null;
        }
        return header.split(" ")[1];
    }

//    public Collection<GrantedAuthority> extractRoleFromToken(String token) {
//        Claims claims = getAllClaimsFromToken(token);
//        String role = (String) claims.get("role");
//        List<GrantedAuthority> authorities = new ArrayList<>();
//        Map<String, Set<String>> roleAuthorizationMap = Map.of(
//                Constant.ROLE.CHECKER.getRoleName(), Constant.ROLE.CHECKER.getAuthorizations(),
//                Constant.ROLE.MAKER.getRoleName(), Constant.ROLE.MAKER.getAuthorizations(),
//                Constant.ROLE.ADMIN.getRoleName(), Constant.ROLE.ADMIN.getAuthorizations(),
//                Constant.ROLE.VIEWER.getRoleName(), Constant.ROLE.VIEWER.getAuthorizations(),
//                Constant.ROLE.SUPER_ADMIN.getRoleName(),Constant.ROLE.SUPER_ADMIN.getAuthorizations()
//        );
//        Set<String> authorizationList = roleAuthorizationMap.getOrDefault(role, Collections.emptySet());
//        for (String author : authorizationList) {
//            authorities.add(new SimpleGrantedAuthority(author));
//        }
//        return authorities;
//    }
}
