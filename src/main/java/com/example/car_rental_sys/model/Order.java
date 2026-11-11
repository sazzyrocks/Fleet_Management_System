package com.example.car_rental_sys.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Rental Order / Booking Entity.
 */
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer orderId;
    private String orderNumber;
    private Integer customerId;
    private String customerName; // View property
    private Integer vehicleId;
    private String vehicleModel; // View property
    private Integer driverId;
    private String driverName;   // View property
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String pickupLocation;
    private String returnLocation;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private RentalMode rentalMode = RentalMode.SELF_DRIVE;
    private LocalDateTime createdAt;

    public Order() {
        this.status = OrderStatus.PENDING;
        this.totalPrice = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
    }

    public Order(String orderNumber, Integer customerId, Integer vehicleId,
                 LocalDateTime startDate, LocalDateTime endDate, BigDecimal totalPrice) {
        this();
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public void setDriverId(Integer driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getReturnLocation() {
        return returnLocation;
    }

    public void setReturnLocation(String returnLocation) {
        this.returnLocation = returnLocation;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public RentalMode getRentalMode() {
        return rentalMode != null ? rentalMode : RentalMode.SELF_DRIVE;
    }

    public void setRentalMode(RentalMode rentalMode) {
        this.rentalMode = rentalMode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Calculates duration in days between rental start and end dates.
     */
    public long getRentalDays() {
        if (startDate == null || endDate == null) return 1;
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return Math.max(1, days);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return Objects.equals(orderId, order.orderId) && Objects.equals(orderNumber, order.orderNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, orderNumber);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", orderNumber='" + orderNumber + '\'' +
                ", customerId=" + customerId +
                ", vehicleId=" + vehicleId +
                ", totalPrice=" + totalPrice +
                ", status=" + status +
                '}';
    }
}
