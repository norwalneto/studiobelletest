package com.nwltecnologia.studiobelle.integration.whatsapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class WhatsAppClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl;

    public WhatsAppClient(@Value("${app.whatsapp.api-url:https://graph.facebook.com/v19.0}") String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public void sendTextMessage(String accessToken, String phoneNumberId, String toPhone, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", toPhone,
                "type", "text",
                "text", Map.of("preview_url", false, "body", message)
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        restTemplate.exchange(apiUrl + "/" + phoneNumberId + "/messages", HttpMethod.POST, entity, String.class);
    }
}
