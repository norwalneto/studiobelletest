package com.nwltecnologia.studiobelle.dashboard.dto;

import java.math.BigDecimal;

public record DashboardResponse(
        long totalCustomers,
        long activeCustomers,
        long inactiveCustomers,
        long appointmentsToday,
        long freeSlotsToday,
        BigDecimal estimatedRevenueToday
) {
}
