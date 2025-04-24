package com.example.cms.config;

import com.example.cms.service.RedissonService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtTokenUtil;

    public static HttpServletRequest REQUEST;
    @Autowired
    private AuthenticationFailureHandler authenticationFailureHandler;
    @Autowired
    private RedissonService redisService;

    @Autowired
    ClientConfig clientConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        REQUEST = request;
        // Bỏ qua xác thực cho các API cụ thể
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            Authentication authentication = null;
            try {
                authentication = extractAuthentication(request, response);
                if (authentication == null) {
                    throw new BadCredentialsException("Invalid Basic Authentication");
                }
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (AuthenticationException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
                return;
            }catch (Exception e){
                throw new BadCredentialsException("Invalid Authentication");
            }
        }
        filterChain.doFilter(request, response);
    }

    private Authentication extractAuthentication(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null) {
            return null;
        }
        //case xac thuc để call api khác
        if (header.startsWith("Bearer ")) {
            String token = header.split(" ")[1];
            String clientId = null;
            if (StringUtils.isNotBlank(token)) {
                try {
                    clientId = jwtTokenUtil.extractClientIdFromToken(token);
//                    var role = jwtTokenUtil.extractRoleFromToken(token);
//                    if (token.equalsIgnoreCase(redisService.get(clientId).toString())) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(clientId, clientId);
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    return usernamePasswordAuthenticationToken;
//                    }else{
//                        throw new Exception();
//                    }
                } catch (ExpiredJwtException ex) {
                    // JWT hết hạn
                    authenticationFailureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("JWT has expired"));

                } catch (MalformedJwtException ex) {
                    // JWT không hợp lệ
                    authenticationFailureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("Invalid JWT"));

                } catch (Exception e) {
                    // Các trường hợp khác
                    authenticationFailureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("Authentication failed"));
                }
            }
        }
        return null;
    }
}
