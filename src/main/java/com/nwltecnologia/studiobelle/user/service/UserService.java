package com.nwltecnologia.studiobelle.user.service;

import com.nwltecnologia.studiobelle.security.ApiSecurityException;
import com.nwltecnologia.studiobelle.security.SecuritySupport;
import com.nwltecnologia.studiobelle.security.PasswordService;
import com.nwltecnologia.studiobelle.tenant.TenantContext;
import com.nwltecnologia.studiobelle.user.dto.UserRequest;
import com.nwltecnologia.studiobelle.user.dto.UserResponse;
import com.nwltecnologia.studiobelle.user.entity.User;
import com.nwltecnologia.studiobelle.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final SecuritySupport securitySupport;

    public UserService(UserRepository userRepository, PasswordService passwordService, SecuritySupport securitySupport) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.securitySupport = securitySupport;
    }

    public List<UserResponse> findAll() {
        securitySupport.requireAdmin();
        return userRepository.findAllByTenantId(currentTenant()).stream().map(this::toResponse).toList();
    }

    public UserResponse findById(Long id) {
        securitySupport.requireAdmin();
        User user = userRepository.findByIdAndTenantId(id, currentTenant())
                .orElseThrow(() -> new ApiSecurityException("Usuário não encontrado"));
        return toResponse(user);
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        securitySupport.requireAdmin();
        userRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new ApiSecurityException("E-mail já cadastrado");
        });

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordService.hashPassword(request.password()))
                .role(request.role())
                .tenantId(currentTenant())
                .active(request.active())
                .build();

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        securitySupport.requireAdmin();
        User user = userRepository.findByIdAndTenantId(id, currentTenant())
                .orElseThrow(() -> new ApiSecurityException("Usuário não encontrado"));

        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());
        user.setActive(request.active());
        user.setPasswordHash(passwordService.hashPassword(request.password()));

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        securitySupport.requireAdmin();
        User user = userRepository.findByIdAndTenantId(id, currentTenant())
                .orElseThrow(() -> new ApiSecurityException("Usuário não encontrado"));
        userRepository.delete(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getTenantId(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private String currentTenant() {
        String tenant = TenantContext.getTenant();
        if (tenant == null || tenant.isBlank()) {
            throw new ApiSecurityException("Tenant não informado");
        }
        return tenant;
    }
}
