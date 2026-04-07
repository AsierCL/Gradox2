package com.example.gradox2.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.example.gradox2.service.exceptions.RateLimitExceededException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final InMemoryRateLimiter rateLimiter;
    private final HandlerExceptionResolver exceptionResolver;

    public RateLimitFilter(InMemoryRateLimiter rateLimiter,
                           @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.rateLimiter = rateLimiter;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            enforceLimit(request);
            filterChain.doFilter(request, response);
        } catch (RateLimitExceededException ex) {
            exceptionResolver.resolveException(request, response, null, ex);
        }
    }

    private void enforceLimit(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("POST".equals(method) && "/api/auth/login".equals(path)) {
            check("auth:login:" + clientIp(request), 5, 15 * 60 * 1000L,
                    "Too many login attempts. Try again later.");
            return;
        }

        if ("POST".equals(method) && "/api/auth/register".equals(path)) {
            check("auth:register:" + clientIp(request), 3, 60 * 60 * 1000L,
                    "Too many registration attempts. Try again later.");
            return;
        }

        if ("GET".equals(method) && "/api/auth/verify".equals(path)) {
            check("auth:verify:" + clientIp(request), 10, 60 * 60 * 1000L,
                    "Too many verification attempts. Try again later.");
            return;
        }

        if (isVoteMutation(method, path)) {
            String principal = authenticatedIdentity();
            check("vote:" + principal, 20, 60 * 1000L,
                    "Too many vote operations. Slow down and retry.");
        }
    }

    private void check(String key, int maxRequests, long windowMs, String message) {
        if (!rateLimiter.isAllowed(key, maxRequests, windowMs)) {
            throw new RateLimitExceededException(message);
        }
    }

    private boolean isVoteMutation(String method, String path) {
        if ("POST".equals(method) && path.matches("/vote/\\d+/(true|false)")) {
            return true;
        }

        if ("DELETE".equals(method) && path.matches("/vote/\\d+")) {
            return true;
        }

        if ("POST".equals(method) && path.matches("/files/\\d+/vote/(true|false)")) {
            return true;
        }

        return "DELETE".equals(method) && path.matches("/files/\\d+/vote");
    }

    private String authenticatedIdentity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
            return auth.getName();
        }
        return "anonymous";
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}