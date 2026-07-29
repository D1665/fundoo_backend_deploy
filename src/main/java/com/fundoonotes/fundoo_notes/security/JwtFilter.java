package com.fundoonotes.fundoo_notes.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // GET AUTHORIZATION HEADER
        String authHeader = request.getHeader("Authorization");

        String token = null;
        String email = null;

        // CHECK IF HEADER STARTS WITH "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

            // STEP 0 — REJECT BLACKLISTED (LOGGED OUT) TOKENS (WITH REDIS FALLBACK)
            boolean isBlacklisted = false;
            try {
                if (Boolean.TRUE.equals(redisTemplate.hasKey("BLACKLIST:" + token))) {
                    isBlacklisted = true;
                }
            } catch (Exception e) {
                System.err.println("[JwtFilter] Redis error checking blacklist: " + e.getMessage());
            }

            if (isBlacklisted) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has been logged out");
                return;
            }

            // STEP 1 — CHECK REDIS CACHE FIRST
            try {
                email = redisTemplate.opsForValue().get("TOKEN:" + token);
            } catch (Exception e) {
                System.err.println("[JwtFilter] Redis error getting cached token: " + e.getMessage());
            }

            // STEP 2 — FALLBACK TO DIRECT JWT PARSING IF CACHE MISSED OR REDIS DOWN
            if (email == null) {
                try {
                    if (jwtUtil.isTokenValid(token)) {
                        email = jwtUtil.extractEmail(token);

                        // STEP 3 — OPTIONALLY CACHE IN REDIS
                        try {
                            redisTemplate.opsForValue().set(
                                    "TOKEN:" + token,
                                    email,
                                    24,
                                    TimeUnit.HOURS
                            );
                        } catch (Exception re) {
                            // Ignore caching errors if Redis is down
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[JwtFilter] JWT parsing/validation failed: " + e.getMessage());
                }
            }
        }

        // SET AUTHENTICATION IN SECURITY CONTEXT
        if (email != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            email, null, new ArrayList<>()
                    );
            authToken.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}