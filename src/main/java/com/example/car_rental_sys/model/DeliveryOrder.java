package com.example.car_rental_sys.model;

import java.io.Serializable;

/**
 * Rich model representing an available or active vehicle delivery assignment for drivers.
 */
public class DeliveryOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    private int orderId;
    private String orderNumber;
    private String carModel;
    private String carNumber;
    private String renterName;
    private String renterPost;
    private String renterAvatar;
    private String seats;
    private String power;
    private String speed;
    private String startLocation;
    private double startLat;
    private double startLng;
    private String destLocation;
    private double destLat;
    private double destLng;
    private String startDate;
    private String endDate;
    private String darkColor;
    private String lightColor;
    private String renterPhone;
    private OrderStatus status;
    private RentalMode rentalMode = RentalMode.SELF_DRIVE;
    private boolean isDemo = false;

    public DeliveryOrder() {
        this.status = OrderStatus.PENDING;
    }

    public DeliveryOrder(int orderId, String orderNumber, String carModel, String carNumber,
                         String renterName, String renterPost, String renterAvatar,
                         String seats, String power, String speed,
                         String startLocation, double startLat, double startLng,
                         String destLocation, double destLat, double destLng,
                         String startDate, String endDate,
                         String darkColor, String lightColor) {
        this(orderId, orderNumber, carModel, carNumber, renterName, renterPost, renterAvatar,
                seats, power, speed, startLocation, startLat, startLng, destLocation, destLat, destLng,
                startDate, endDate, darkColor, lightColor, "+91 98765 00003");
    }

    public DeliveryOrder(int orderId, String orderNumber, String carModel, String carNumber,
                         String renterName, String renterPost, String renterAvatar,
                         String seats, String power, String speed,
                         String startLocation, double startLat, double startLng,
                         String destLocation, double destLat, double destLng,
                         String startDate, String endDate,
                         String darkColor, String lightColor, String renterPhone) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.carModel = carModel;
        this.carNumber = carNumber;
        this.renterName = renterName;
        this.renterPost = renterPost;
        this.renterAvatar = renterAvatar;
        this.seats = seats;
        this.power = power;
        this.speed = speed;
        this.startLocation = startLocation;
        this.startLat = startLat;
        this.startLng = startLng;
        this.destLocation = destLocation;
        this.destLat = destLat;
        this.destLng = destLng;
        this.startDate = startDate;
        this.endDate = endDate;
        this.darkColor = darkColor;
        this.lightColor = lightColor;
        this.renterPhone = renterPhone;
        this.status = OrderStatus.PENDING;
    }

    public String getRenterPhone() {
        return renterPhone != null ? renterPhone : "+91 98765 00003";
    }

    public void setRenterPhone(String renterPhone) {
        this.renterPhone = renterPhone;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getRenterName() {
        return renterName;
    }

    public void setRenterName(String renterName) {
        this.renterName = renterName;
    }

    public String getRenterPost() {
        return renterPost;
    }

    public String getRenterRole() {
        return renterPost;
    }

    public void setRenterPost(String renterPost) {
        this.renterPost = renterPost;
    }

    public String getRenterAvatar() {
        return renterAvatar;
    }

    public void setRenterAvatar(String renterAvatar) {
        this.renterAvatar = renterAvatar;
    }

    public String getSeats() {
        return seats;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public String getPower() {
        return power;
    }

    public String getEngine() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getSpeed() {
        return speed;
    }

    public String getTopSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public double getStartLat() {
        return startLat;
    }

    public void setStartLat(double startLat) {
        this.startLat = startLat;
    }

    public double getStartLng() {
        return startLng;
    }

    public void setStartLng(double startLng) {
        this.startLng = startLng;
    }

    public String getDestLocation() {
        return destLocation;
    }

    public void setDestLocation(String destLocation) {
        this.destLocation = destLocation;
    }

    public double getDestLat() {
        return destLat;
    }

    public void setDestLat(double destLat) {
        this.destLat = destLat;
    }

    public double getDestLng() {
        return destLng;
    }

    public void setDestLng(double destLng) {
        this.destLng = destLng;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDarkColor() {
        return darkColor;
    }

    public void setDarkColor(String darkColor) {
        this.darkColor = darkColor;
    }

    public String getLightColor() {
        return lightColor;
    }

    public void setLightColor(String lightColor) {
        this.lightColor = lightColor;
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

    public boolean isDemo() {
        return isDemo;
    }

    public void setDemo(boolean demo) {
        isDemo = demo;
    }

    public boolean isChauffeur() {
        return getRentalMode() == RentalMode.WITH_DRIVER;
    }
}
