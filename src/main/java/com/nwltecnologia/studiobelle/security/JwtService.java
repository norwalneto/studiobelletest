package com.nwltecnologia.studiobelle.security;

import com.nwltecnologia.studiobelle.user.entity.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class JwtService {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final String secret;
    private final long accessTokenTtlSeconds;

    public JwtService(@Value("${app.security.jwt.secret:change-me-in-production}") String secret,
                      @Value("${app.security.jwt.access-token-ttl-seconds:900}") long accessTokenTtlSeconds) {
        this.secret = secret;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    public TokenPayload parseAndValidate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new ApiSecurityException("Token JWT inválido");
        }

        String signatureCheck = sign(parts[0] + "." + parts[1]);
        if (!signatureCheck.equals(parts[2])) {
            throw new ApiSecurityException("Assinatura JWT inválida");
        }

        String payloadJson = new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);

        long sub = extractLong(payloadJson, "sub");
        String email = extractString(payloadJson, "email");
        String tenantId = extractString(payloadJson, "tenantId");
        String role = extractString(payloadJson, "role");
        long exp = extractLong(payloadJson, "exp");

        if (Instant.now().isAfter(Instant.ofEpochSecond(exp))) {
            throw new ApiSecurityException("Token JWT expirado");
        }

        return new TokenPayload(sub, email, tenantId, UserRole.valueOf(role), Instant.ofEpochSecond(exp));
    }

    public TokenData generateAccessToken(Long userId, String email, String tenantId, UserRole role) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(accessTokenTtlSeconds);

        String header = URL_ENCODER.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String payloadJson = "{\"sub\":" + userId +
                ",\"email\":\"" + escape(email) + "\"" +
                ",\"tenantId\":\"" + escape(tenantId) + "\"" +
                ",\"role\":\"" + role.name() + "\"" +
                ",\"iat\":" + now.getEpochSecond() +
                ",\"exp\":" + expiration.getEpochSecond() + "}";

        String payload = URL_ENCODER.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signature = sign(header + "." + payload);

        return new TokenData(header + "." + payload + "." + signature, expiration);
    }

    private String sign(String content) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return URL_ENCODER.encodeToString(hmac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new ApiSecurityException("Erro interno ao assinar JWT");
        }
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String extractString(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new ApiSecurityException("Campo ausente no JWT: " + field);
        }
        int valueStart = start + marker.length();
        int valueEnd = json.indexOf("\"", valueStart);
        if (valueEnd < 0) {
            throw new ApiSecurityException("Campo malformado no JWT: " + field);
        }
        return json.substring(valueStart, valueEnd);
    }

    private long extractLong(String json, String field) {
        String marker = "\"" + field + "\":";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new ApiSecurityException("Campo ausente no JWT: " + field);
        }

        int valueStart = start + marker.length();
        int valueEnd = valueStart;
        while (valueEnd < json.length() && Character.isDigit(json.charAt(valueEnd))) {
            valueEnd++;
        }
        return Long.parseLong(json.substring(valueStart, valueEnd));
    }

    public record TokenData(String value, Instant expiresAt) {
    }

    public record TokenPayload(Long userId, String email, String tenantId, UserRole role, Instant expiresAt) {
    }
}
