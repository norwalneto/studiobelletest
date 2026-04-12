package com.nwltecnologia.studiobelle.integration.whatsapp;

import com.nwltecnologia.studiobelle.appointment.entity.Appointment;
import com.nwltecnologia.studiobelle.tenant.TenantContext;
import com.nwltecnologia.studiobelle.tenantmaster.master.MasterTenantDirectory;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppNotificationService {

    private final WhatsAppClient whatsAppClient;
    private final MasterTenantDirectory masterTenantDirectory;

    public WhatsAppNotificationService(WhatsAppClient whatsAppClient,
                                       MasterTenantDirectory masterTenantDirectory) {
        this.whatsAppClient = whatsAppClient;
        this.masterTenantDirectory = masterTenantDirectory;
    }

    public void notifyAppointmentCreated(Appointment appointment, String smartMessage) {
        String tenantId = TenantContext.getTenant();
        if (tenantId == null || tenantId.isBlank()) {
            return;
        }

        masterTenantDirectory.findWhatsAppByTenantId(tenantId).ifPresent(account ->
                whatsAppClient.sendTextMessage(account.accessToken(), account.phoneNumberId(), appointment.getClientPhone(), smartMessage)
        );
    }
}
