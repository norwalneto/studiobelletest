package com.nwltecnologia.studiobelle.integration.openai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey;
    private final String model;

    public OpenAiService(@Value("${app.openai.api-key:}") String apiKey,
                         @Value("${app.openai.model:gpt-4o-mini}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    public String generateWhatsAppMessage(String customerName, String serviceName, String startTimeText) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Olá " + customerName + ", seu agendamento para " + serviceName + " foi confirmado para " + startTimeText + ".";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", "Você cria mensagens curtas e gentis para confirmação de agendamento no WhatsApp."),
                        Map.of("role", "user", "content", "Crie mensagem para cliente " + customerName +
                                ", serviço " + serviceName + ", horário " + startTimeText + ".")
                ),
                "temperature", 0.6
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.openai.com/v1/chat/completions",
                HttpMethod.POST,
                request,
                Map.class
        );

        return extractContent(response.getBody());
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> body) {
        if (body == null) {
            return "Agendamento confirmado.";
        }
        Object choicesObj = body.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            return "Agendamento confirmado.";
        }
        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> firstMap)) {
            return "Agendamento confirmado.";
        }
        Object message = firstMap.get("message");
        if (!(message instanceof Map<?, ?> messageMap)) {
            return "Agendamento confirmado.";
        }
        Object content = messageMap.get("content");
        return content == null ? "Agendamento confirmado." : content.toString();
    }
}
