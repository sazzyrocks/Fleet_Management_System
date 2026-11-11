package com.example.car_rental_sys.model;

import java.math.BigDecimal;

/**
 * Rental service mode chosen by customer.
 */
public enum RentalMode {
    SELF_DRIVE("Self Drive", "Drive yourself after vehicle delivery", BigDecimal.ZERO),
    WITH_DRIVER("With Driver", "Professional chauffeur stays with you", new BigDecimal("800.00"));

    private final String displayName;
    private final String description;
    private final BigDecimal dailySurcharge;

    RentalMode(String displayName, String description, BigDecimal dailySurcharge) {
        this.displayName = displayName;
        this.description = description;
        this.dailySurcharge = dailySurcharge;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getDailySurcharge() {
        return dailySurcharge;
    }

    public static RentalMode fromString(String value) {
        if (value == null) return SELF_DRIVE;
        try {
            return RentalMode.valueOf(value.trim().toUpperCase().replace(" ", "_").replace("-", "_"));
        } catch (IllegalArgumentException e) {
            return SELF_DRIVE;
        }
    }
}
