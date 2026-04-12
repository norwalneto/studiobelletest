package com.nwltecnologia.studiobelle.security;

import com.nwltecnologia.studiobelle.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/tenants",
            "/whatsapp/webhook"
    );

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (isPublicPath(path, request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            unauthorized(response, "Token de autenticação não informado");
            return;
        }

        String token = authorization.substring(7);
        try {
            JwtService.TokenPayload payload = jwtService.parseAndValidate(token);
            String currentTenant = TenantContext.getTenant();
            if (currentTenant == null || currentTenant.isBlank()) {
                unauthorized(response, "Tenant ausente na requisição");
                return;
            }
            if (!payload.tenantId().equals(currentTenant)) {
                unauthorized(response, "Token não pertence ao tenant atual");
                return;
            }

            SecurityContext.set(new AuthenticatedUser(payload.userId(), payload.email(), payload.tenantId(), payload.role()));
            filterChain.doFilter(request, response);
        } catch (ApiSecurityException ex) {
            unauthorized(response, ex.getMessage());
        } finally {
            SecurityContext.clear();
        }
    }

    private boolean isPublicPath(String path, String method) {
        return PUBLIC_PATHS.contains(path)
                || (HttpMethod.POST.matches(method) && "/tenants".equals(path));
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
