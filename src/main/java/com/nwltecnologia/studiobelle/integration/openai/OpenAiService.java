package com.nwltecnologia.studiobelle.integration.openai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
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
        return humanizeMessage("Olá " + customerName + ", seu agendamento para " + serviceName + " foi confirmado para " + startTimeText + ".");
    }

    public String humanizeMessage(String baseMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            return baseMessage;
        }
        return ask("Você melhora mensagens curtas de WhatsApp com tom gentil e profissional.",
                "Melhore mantendo o significado: " + baseMessage);
    }

    public Map<String, String> extractAppointmentData(String rawMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            return extractFallback(rawMessage);
        }
        String content = ask("Extraia dados de agendamento e retorne JSON com campos: nome,data,hora,servico. Data em yyyy-MM-dd e hora em HH:mm.", rawMessage);
        return parseLooseJson(content);
    }

    private String ask(String systemText, String userText) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemText),
                        Map.of("role", "user", "content", userText)
                ),
                "temperature", 0.4
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange("https://api.openai.com/v1/chat/completions", HttpMethod.POST, request, Map.class);
        return extractContent(response.getBody());
    }

    private Map<String, String> extractFallback(String raw) {
        Map<String, String> data = new HashMap<>();
        data.put("nome", "Cliente");
        data.put("servico", "Atendimento");
        data.put("data", java.time.LocalDate.now().plusDays(1).toString());
        data.put("hora", "10:00");
        if (raw != null && !raw.isBlank()) {
            data.put("servico", raw.length() > 120 ? raw.substring(0, 120) : raw);
        }
        return data;
    }

    private Map<String, String> parseLooseJson(String content) {
        Map<String, String> data = extractFallback(content);
        if (content == null) return data;
        String sanitized = content.replace("{", "").replace("}", "").replace("\"", "");
        for (String part : sanitized.split(",")) {
            String[] kv = part.split(":", 2);
            if (kv.length == 2) {
                data.put(kv[0].trim(), kv[1].trim());
            }
        }
        return data;
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
