package com.example.car_rental_sys.service;

import com.example.car_rental_sys.dao.DAOFactory;
import com.example.car_rental_sys.dao.OrderDAO;
import com.example.car_rental_sys.dao.VehicleDAO;
import com.example.car_rental_sys.model.Order;
import com.example.car_rental_sys.model.OrderStatus;
import com.example.car_rental_sys.model.RentalMode;
import com.example.car_rental_sys.model.Vehicle;
import com.example.car_rental_sys.model.VehicleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Enterprise Rental Management Service.
 * Orchestrates booking lifecycles, schedule collision checks, and financial totals.
 */
public class RentalService {

    private final OrderDAO orderDAO;
    private final VehicleDAO vehicleDAO;

    public RentalService() {
        this(DAOFactory.getInstance().getOrderDAO(), DAOFactory.getInstance().getVehicleDAO());
    }

    public RentalService(OrderDAO orderDAO, VehicleDAO vehicleDAO) {
        this.orderDAO = orderDAO;
        this.vehicleDAO = vehicleDAO;
    }

    /**
     * Creates a new rental order after verifying date validity and double-booking availability.
     *
     * @param customerId     user ID of customer
     * @param vehicleId      vehicle ID requested
     * @param driverId       optional chauffeur ID (can be null)
     * @param startDate      rental start
     * @param endDate        rental end
     * @param pickupLocation pickup address
     * @param returnLocation return address
     * @return created Order
     * @throws BookingConflictException if the vehicle is already booked for this interval
     * @throws ServiceException         if validation fails
     */
    public Order bookRental(int customerId, int vehicleId, Integer driverId,
                           LocalDateTime startDate, LocalDateTime endDate,
                           String pickupLocation, String returnLocation) throws BookingConflictException {
        return bookRental(customerId, vehicleId, driverId, startDate, endDate, pickupLocation, returnLocation,
                driverId != null ? RentalMode.WITH_DRIVER : RentalMode.SELF_DRIVE);
    }

    public Order bookRental(int customerId, int vehicleId, Integer driverId,
                           LocalDateTime startDate, LocalDateTime endDate,
                           String pickupLocation, String returnLocation,
                           RentalMode rentalMode) throws BookingConflictException {
        if (startDate == null || endDate == null) {
            throw new ServiceException("Start date and end date are required.");
        }
        if (!endDate.isAfter(startDate)) {
            throw new ServiceException("Return date must be strictly after pickup date.");
        }

        // Verify vehicle exists
        Vehicle vehicle = vehicleDAO.findById(vehicleId)
                .orElseThrow(() -> new ServiceException("Vehicle not found: ID " + vehicleId));

        if (VehicleStatus.MAINTENANCE.equals(vehicle.getStatus())) {
            throw new ServiceException("Vehicle is currently under maintenance and cannot be rented.");
        }

        // Double-booking collision check
        boolean hasOverlap = orderDAO.hasOverlappingRentals(vehicleId, startDate, endDate, null);
        if (hasOverlap) {
            throw new BookingConflictException("The selected vehicle is already reserved during this time range. Please choose alternative dates or another car.");
        }

        RentalMode mode = rentalMode != null ? rentalMode : (driverId != null ? RentalMode.WITH_DRIVER : RentalMode.SELF_DRIVE);

        // Calculate pricing (daily rate + optional chauffeur fee)
        long days = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate));
        BigDecimal dailyRate = vehicle.getDailyRate() != null ? vehicle.getDailyRate() : BigDecimal.ZERO;
        BigDecimal dailyEffectiveRate = dailyRate.add(mode.getDailySurcharge());
        BigDecimal totalPrice = dailyEffectiveRate.multiply(BigDecimal.valueOf(days));

        // Generate tracking order number (e.g. ORD-20261031-AB12)
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String shortId = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String orderNumber = "ORD-" + datePrefix + "-" + shortId;

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomerId(customerId);
        order.setVehicleId(vehicleId);
        order.setDriverId(driverId);
        order.setStartDate(startDate);
        order.setEndDate(endDate);
        order.setPickupLocation(pickupLocation != null ? pickupLocation : "Main Fleet Hub");
        order.setReturnLocation(returnLocation != null ? returnLocation : "Main Fleet Hub");
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PENDING);
        order.setRentalMode(mode);

        return orderDAO.save(order);
    }

    public List<Order> getCustomerOrders(int customerId) {
        return orderDAO.findByCustomerId(customerId);
    }

    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    public Optional<Order> getOrderById(int orderId) {
        return orderDAO.findById(orderId);
    }

    public Optional<Order> getOrderByNumber(String orderNumber) {
        return orderDAO.findByOrderNumber(orderNumber);
    }

    public Order createReservation(Integer customerId, Integer vehicleId,
                                   LocalDateTime startDate, LocalDateTime endDate,
                                   String pickupLocation, String returnLocation) {
        return createReservation(customerId, vehicleId, startDate, endDate, pickupLocation, returnLocation, RentalMode.SELF_DRIVE);
    }

    public Order createReservation(Integer customerId, Integer vehicleId,
                                   LocalDateTime startDate, LocalDateTime endDate,
                                   String pickupLocation, String returnLocation,
                                   RentalMode rentalMode) {
        int custId = customerId != null ? customerId : 1;
        int vehId = vehicleId != null ? vehicleId : 1;
        return bookRental(custId, vehId, null, startDate, endDate, pickupLocation, returnLocation, rentalMode);
    }

    /**
     * Handover vehicle to customer in Self-Drive mode upon arrival.
     * Transitions ACTIVE -> ON_RENT.
     */
    public boolean handoverVehicle(Integer orderId) {
        if (orderId == null) return false;
        return updateOrderStatus(orderId, OrderStatus.ON_RENT);
    }

    /**
     * Customer returns the vehicle at the end of booking.
     * Transitions ON_RENT -> COMPLETED and marks vehicle AVAILABLE.
     */
    public boolean returnVehicle(Integer orderId) {
        return completeBooking(orderId);
    }

    public boolean confirmBooking(Integer orderId) {
        if (orderId == null) return false;
        boolean updated = updateOrderStatus(orderId, OrderStatus.CONFIRMED);
        if (updated) {
            orderDAO.findById(orderId).ifPresent(order -> {
                vehicleDAO.updateStatus(order.getVehicleId(), VehicleStatus.RENTED);
            });
        }
        return updated;
    }

    public boolean completeBooking(Integer orderId) {
        if (orderId == null) return false;
        boolean updated = updateOrderStatus(orderId, OrderStatus.COMPLETED);
        if (updated) {
            orderDAO.findById(orderId).ifPresent(order -> {
                vehicleDAO.updateStatus(order.getVehicleId(), VehicleStatus.AVAILABLE);
            });
        }
        return updated;
    }

    public boolean updateOrderStatus(int orderId, OrderStatus status) {
        return orderDAO.updateStatus(orderId, status);
    }

    public BigDecimal getTotalRevenue() {
        return orderDAO.calculateTotalRevenue();
    }
}
