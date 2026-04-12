package com.nwltecnologia.studiobelle.integration.whatsapp;

import com.nwltecnologia.studiobelle.appointment.entity.Appointment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppNotificationService {

    private final WhatsAppClient whatsAppClient;
    private final String phoneNumberId;

    public WhatsAppNotificationService(WhatsAppClient whatsAppClient,
                                       @Value("${app.whatsapp.phone-number-id:}") String phoneNumberId) {
        this.whatsAppClient = whatsAppClient;
        this.phoneNumberId = phoneNumberId;
    }

    public void notifyAppointmentCreated(Appointment appointment, String smartMessage) {
        if (phoneNumberId == null || phoneNumberId.isBlank()) {
            return;
        }
        whatsAppClient.sendTextMessage(phoneNumberId, appointment.getClientPhone(), smartMessage);
    }
}
