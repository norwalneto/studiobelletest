package com.nwltecnologia.studiobelle.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    private final TenantResolver tenantResolver;

    public TenantFilter(TenantResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/whatsapp/webhook")) {
            filterChain.doFilter(request, response);
            return;
        }

        String tenant = resolveTenant(request);

        try {
            if (tenant != null && !tenant.isBlank()) {
                TenantContext.setTenant(tenant);
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveTenant(HttpServletRequest request) {
        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isBlank()) {
            host = request.getServerName();
        }

        String subdomain = extractSubdomain(host);
        String tenantBySubdomain = tenantResolver.resolveBySubdomain(subdomain);
        if (tenantBySubdomain != null) {
            return tenantBySubdomain;
        }

        String tenantByHeader = request.getHeader("X-Tenant-ID");
        if (tenantByHeader != null && !tenantByHeader.isBlank()) {
            return tenantByHeader;
        }

        return null;
    }

    private String extractSubdomain(String host) {
        if (host == null || host.isBlank()) {
            return null;
        }

        String normalizedHost = host.contains(":") ? host.substring(0, host.indexOf(':')) : host;
        String[] parts = normalizedHost.split("\\.");

        if (parts.length >= 3) {
            return parts[0].toLowerCase();
        }

        if (parts.length == 2 && "localhost".equalsIgnoreCase(parts[1])) {
            return parts[0].toLowerCase();
        }

        return null;
    }
}
