package com.nwltecnologia.studiobelle.security;

import com.nwltecnologia.studiobelle.user.entity.UserRole;
import org.springframework.stereotype.Component;

@Component
public class SecuritySupport {

    public AuthenticatedUser currentUser() {
        AuthenticatedUser user = SecurityContext.get();
        if (user == null) {
            throw new ApiSecurityException("Usuário não autenticado");
        }
        return user;
    }

    public void requireAdmin() {
        if (currentUser().role() != UserRole.ADMIN) {
            throw new ApiSecurityException("Acesso permitido apenas para administradores");
        }
    }
}
