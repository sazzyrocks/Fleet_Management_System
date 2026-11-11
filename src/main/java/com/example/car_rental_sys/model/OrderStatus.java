package com.example.car_rental_sys.model;

/**
 * Status of a rental booking/order.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    ACTIVE,
    ON_RENT,
    COMPLETED,
    CANCELLED;

    public static OrderStatus fromString(String value) {
        if (value == null) return PENDING;
        try {
            return OrderStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}
