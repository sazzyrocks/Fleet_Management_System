package com.example.car_rental_sys.service;

import com.example.car_rental_sys.StatusContainer;
import com.example.car_rental_sys.dao.DAOFactory;
import com.example.car_rental_sys.model.DeliveryOrder;
import com.example.car_rental_sys.model.Order;
import com.example.car_rental_sys.model.OrderStatus;
import com.example.car_rental_sys.model.RentalMode;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.model.Vehicle;
import com.example.car_rental_sys.model.VehicleStatus;
import com.example.car_rental_sys.util.EnvConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Enterprise In-Memory & Database Delivery Order Coordinator.
 * Manages pending vehicle delivery jobs, active dispatches, and route telemetry.
 * Connects directly to MySQL 8 DAO tier with fallback to realistic pre-seeded Andhra Pradesh fleet.
 */
public class DeliveryOrderManager {

    private static volatile DeliveryOrderManager instance;
    private final List<DeliveryOrder> availableOrders = new ArrayList<>();
    private final Set<Integer> completedOrderIds = Collections.synchronizedSet(new HashSet<>());
    private DeliveryOrder activeDispatchOrder = null;

    private DeliveryOrderManager() {
        availableOrders.clear();
    }

    public static DeliveryOrderManager getInstance() {
        if (instance == null) {
            synchronized (DeliveryOrderManager.class) {
                if (instance == null) {
                    instance = new DeliveryOrderManager();
                }
            }
        }
        return instance;
    }

    /**
     * Registers a new customer booking dynamically into the driver's dispatch queue.
     */
    public synchronized void registerNewBooking(Order o) {
        if (o == null) return;
        DeliveryOrder dOrder = convertOrderToDeliveryOrder(o);
        if (dOrder != null) {
            availableOrders.removeIf(existing -> existing.getOrderId() == dOrder.getOrderId());
            availableOrders.add(0, dOrder);
        }
    }

    /**
     * Converts a domain Order (from MySQL or StatusContainer) into a rich DeliveryOrder for drivers.
     */
    public DeliveryOrder convertOrderToDeliveryOrder(Order o) {
        if (o == null) return null;
        try {
            var userDao = DAOFactory.getInstance().getUserDAO();
            var vehicleDao = DAOFactory.getInstance().getVehicleDAO();

            Optional<User> userOpt = userDao.findById(o.getCustomerId());
            Optional<Vehicle> vehicleOpt = vehicleDao.findById(o.getVehicleId());

            String rName = userOpt.map(User::getFullName).filter(n -> !n.isBlank()).orElse("Valued Customer");
            String rPhone = userOpt.map(User::getPhone).filter(p -> !p.isBlank()).orElse("+91 93463 89758");
            String rAvatar = userOpt.map(User::getAvatarPath).filter(a -> a != null && !a.isBlank()).orElse("/com/example/car_rental_sys/image/avatar/1.png");

            String cModel = vehicleOpt.map(v -> v.getModel().replace(" ", "_")).orElse("Porsche_718");
            String cPlate = vehicleOpt.map(Vehicle::getLicensePlate).orElse("AP 39 DL 1001");
            String carImgPath = vehicleOpt.map(Vehicle::getImagePath).orElse("/com/example/car_rental_sys/image/cars/" + cModel + ".png");
            String seats = vehicleOpt.map(v -> (v.getSeats() != null ? v.getSeats() : 4) + " Seats").orElse("4 Seats");
            String power = vehicleOpt.map(v -> (v.getFuelType() != null ? v.getFuelType().name() : "Petrol") + " • " + (v.getTransmission() != null ? v.getTransmission().name() : "Auto")).orElse("3.0 L Turbo");
            String speed = "280 km/h";

            boolean isChauffeur = (o.getRentalMode() == com.example.car_rental_sys.model.RentalMode.WITH_DRIVER);
            String startLoc;
            double sLat, sLng;
            String destLoc;
            double dLat, dLng;

            if (isChauffeur) {
                // Chauffeur Mode (Driver ke sath): Pick up client at client pickup point and drive to destination/airport
                startLoc = (o.getPickupLocation() != null && !o.getPickupLocation().isBlank())
                        ? o.getPickupLocation() : "Guntur Central";
                double[] sCoords = resolveCoordinates(startLoc, 16.3007, 80.4639);
                sLat = sCoords[0];
                sLng = sCoords[1];

                if (o.getReturnLocation() != null && !o.getReturnLocation().isBlank() && !o.getReturnLocation().equalsIgnoreCase(startLoc)) {
                    destLoc = o.getReturnLocation();
                } else {
                    destLoc = "Vijayawada Junction (BZA Hub)";
                }
                double[] dCoords = resolveCoordinates(destLoc, 16.5175, 80.6200);
                dLat = dCoords[0];
                dLng = dCoords[1];
            } else {
                // Self-Drive Mode (Bas drop / Vehicle Delivery): Driver dispatches car from Fleet Depot to Customer's chosen handover point
                startLoc = "SRM AP Fleet Hub, Amaravati";
                sLat = 16.4632;
                sLng = 80.5078;

                destLoc = (o.getPickupLocation() != null && !o.getPickupLocation().isBlank())
                        ? o.getPickupLocation() : "Guntur Central";
                if (destLoc.toLowerCase().contains("srm") || destLoc.toLowerCase().contains("amaravati")) {
                    destLoc = (o.getReturnLocation() != null && !o.getReturnLocation().isBlank() && !o.getReturnLocation().equalsIgnoreCase(startLoc))
                            ? o.getReturnLocation() : "Vijayawada Junction (BZA Hub)";
                }
                double[] dCoords = resolveCoordinates(destLoc, 16.3007, 80.4639);
                dLat = dCoords[0];
                dLng = dCoords[1];
            }

            String sDate = o.getStartDate() != null ? o.getStartDate().toLocalDate().toString() : "2026-11-21";
            String eDate = o.getEndDate() != null ? o.getEndDate().toLocalDate().toString() : "2026-11-25";

            DeliveryOrder dOrder = new DeliveryOrder(
                    o.getOrderId(),
                    o.getOrderNumber() != null ? o.getOrderNumber() : ("ORD-2026-" + o.getOrderId()),
                    cModel,
                    cPlate,
                    rName,
                    isChauffeur ? "Chauffeur Ride Client" : "Self-Drive Rental Client",
                    rAvatar,
                    seats,
                    power,
                    speed,
                    startLoc,
                    sLat, sLng,
                    destLoc,
                    dLat, dLng,
                    sDate,
                    eDate,
                    isChauffeur ? "#722ED1" : "#0052d4",
                    isChauffeur ? "#9254de" : "#4364f7",
                    rPhone
            );
            dOrder.setRentalMode(o.getRentalMode());
            dOrder.setDemo(false);
            dOrder.setCarImagePath(carImgPath);
            return dOrder;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns list of real customer bookings from MySQL database or active customer checkout.
     * Purely dynamic: only genuine customer orders are shown.
     */
    public synchronized List<DeliveryOrder> getAvailableOrders() {
        List<DeliveryOrder> result = new ArrayList<>();

        // 1. Fetch real customer bookings from MySQL database if available
        try {
            var orderDao = DAOFactory.getInstance().getOrderDAO();
            List<Order> dbOrders = new ArrayList<>();
            List<Order> confirmed = orderDao.findByStatus(OrderStatus.CONFIRMED);
            if (confirmed != null) dbOrders.addAll(confirmed);
            List<Order> pending = orderDao.findByStatus(OrderStatus.PENDING);
            if (pending != null) dbOrders.addAll(pending);

            // Sort newest bookings first so driver sees the latest customer booking at card 0
            dbOrders.sort((a, b) -> Integer.compare(b.getOrderId(), a.getOrderId()));

            for (Order o : dbOrders) {
                if (completedOrderIds.contains(o.getOrderId())) {
                    continue;
                }
                if (activeDispatchOrder != null && activeDispatchOrder.getOrderId() == o.getOrderId()) {
                    continue;
                }
                DeliveryOrder dOrder = convertOrderToDeliveryOrder(o);
                if (dOrder != null && result.stream().noneMatch(r -> r.getOrderId() == dOrder.getOrderId())) {
                    result.add(dOrder);
                }
            }
        } catch (Exception ignored) {
            // Offline or table not populated yet; fallback smoothly
        }

        // 2. Also ensure newly booked customer order in StatusContainer is immediately included
        if (StatusContainer.currentOrder != null) {
            Order co = StatusContainer.currentOrder;
            if ((co.getStatus() == OrderStatus.CONFIRMED || co.getStatus() == OrderStatus.PENDING)
                    && !completedOrderIds.contains(co.getOrderId())
                    && result.stream().noneMatch(r -> r.getOrderId() == co.getOrderId())
                    && (activeDispatchOrder == null || activeDispatchOrder.getOrderId() != co.getOrderId())) {
                DeliveryOrder dOrder = convertOrderToDeliveryOrder(co);
                if (dOrder != null) {
                    result.add(0, dOrder); // Highest priority: newly booked customer vehicle
                }
            }
        }

        // 3. Include any dynamically registered customer bookings
        for (DeliveryOrder o : availableOrders) {
            if (completedOrderIds.contains(o.getOrderId())) {
                continue;
            }
            if (activeDispatchOrder != null && activeDispatchOrder.getOrderId() == o.getOrderId()) {
                continue;
            }
            if (result.stream().noneMatch(r -> r.getOrderId() == o.getOrderId())) {
                result.add(o);
            }
        }

        return result;
    }

    public synchronized boolean isOrderCompleted(int orderId) {
        return completedOrderIds.contains(orderId);
    }

    public synchronized Optional<DeliveryOrder> findOrderById(int orderId) {
        if (activeDispatchOrder != null && activeDispatchOrder.getOrderId() == orderId) {
            return Optional.of(activeDispatchOrder);
        }
        return availableOrders.stream().filter(o -> o.getOrderId() == orderId).findFirst();
    }

    public synchronized void setActiveDispatchOrder(DeliveryOrder order) {
        this.activeDispatchOrder = order;
        StatusContainer.activeDeliveryOrder = order;
        if (order != null) {
            StatusContainer.currentContinueOrderID = order.getOrderId();
        }
    }

    public synchronized void acceptOrder(DeliveryOrder order) {
        order.setStatus(OrderStatus.ACTIVE);
        availableOrders.removeIf(o -> o.getOrderId() == order.getOrderId());
        this.activeDispatchOrder = order;
        StatusContainer.activeDeliveryOrder = order;
        StatusContainer.currentContinueOrderID = order.getOrderId();

        // Sync status to database only for real orders (not demo mock orders)
        if (!order.isDemo()) {
            try {
                DAOFactory.getInstance().getOrderDAO().updateStatus(order.getOrderId(), OrderStatus.ACTIVE);
            } catch (Exception ignored) {}
        }
    }

    public synchronized void completeActiveDelivery() {
        DeliveryOrder order = activeDispatchOrder;
        if (order == null && StatusContainer.activeDeliveryOrder != null) {
            order = StatusContainer.activeDeliveryOrder;
        }

        if (order != null) {
            final DeliveryOrder targetOrder = order;
            completedOrderIds.add(targetOrder.getOrderId());
            availableOrders.removeIf(o -> o.getOrderId() == targetOrder.getOrderId());

            OrderStatus nextStatus = (targetOrder.getRentalMode() == RentalMode.SELF_DRIVE)
                    ? OrderStatus.ON_RENT
                    : OrderStatus.COMPLETED;

            targetOrder.setStatus(nextStatus);
            if (StatusContainer.currentOrder != null && StatusContainer.currentOrder.getOrderId() == targetOrder.getOrderId()) {
                StatusContainer.currentOrder.setStatus(nextStatus);
            }
            if (!targetOrder.isDemo()) {
                try {
                    DAOFactory.getInstance().getOrderDAO().updateStatus(targetOrder.getOrderId(), nextStatus);
                    // If it was with driver and now completed, release vehicle to AVAILABLE
                    if (nextStatus == OrderStatus.COMPLETED) {
                        DAOFactory.getInstance().getOrderDAO().findById(targetOrder.getOrderId()).ifPresent(dbOrder -> {
                            try {
                                DAOFactory.getInstance().getVehicleDAO().updateStatus(dbOrder.getVehicleId(), VehicleStatus.AVAILABLE);
                            } catch (Exception ignored) {}
                        });
                    }
                } catch (Exception ignored) {}
            }
        }
        this.activeDispatchOrder = null;
        StatusContainer.activeDeliveryOrder = null;
        StatusContainer.currentContinueOrderID = -1;
    }

    public synchronized void cancelDispatchForOrder(int orderId) {
        completedOrderIds.add(orderId);
        availableOrders.removeIf(o -> o.getOrderId() == orderId);
        if (activeDispatchOrder != null && activeDispatchOrder.getOrderId() == orderId) {
            activeDispatchOrder.setStatus(OrderStatus.CANCELLED);
            this.activeDispatchOrder = null;
        }
        if (StatusContainer.activeDeliveryOrder != null && StatusContainer.activeDeliveryOrder.getOrderId() == orderId) {
            StatusContainer.activeDeliveryOrder = null;
            StatusContainer.currentContinueOrderID = -1;
        }
    }

    public synchronized void dismissOrder(int orderId) {
        completedOrderIds.add(orderId);
        availableOrders.removeIf(o -> o.getOrderId() == orderId);
    }

    public synchronized DeliveryOrder getActiveDispatchOrder() {
        return activeDispatchOrder;
    }

    public synchronized boolean hasActiveDispatch() {
        return activeDispatchOrder != null && activeDispatchOrder.getStatus() == OrderStatus.ACTIVE;
    }

    public synchronized void resetAll() {
        activeDispatchOrder = null;
        availableOrders.clear();
        completedOrderIds.clear();
    }

    public static double[] resolveCoordinates(String locationName, double defaultLat, double defaultLng) {
        if (locationName == null || locationName.isBlank()) {
            return new double[]{defaultLat, defaultLng};
        }
        String loc = locationName.toLowerCase();
        if (loc.contains("guntur")) {
            return new double[]{16.3007, 80.4639};
        } else if (loc.contains("airport") || loc.contains("vga") || loc.contains("gannavaram")) {
            return new double[]{16.5304, 80.7968};
        } else if (loc.contains("vijayawada") || loc.contains("bza") || loc.contains("junction")) {
            return new double[]{16.5175, 80.6200};
        } else if (loc.contains("mangalagiri") || loc.contains("aiims")) {
            return new double[]{16.4422, 80.5772};
        } else if (loc.contains("secretariat") || loc.contains("velagapudi")) {
            return new double[]{16.5417, 80.5150};
        } else if (loc.contains("srm") || loc.contains("amaravati") || loc.contains("depot") || loc.contains("hub")) {
            return new double[]{16.4632, 80.5078};
        }
        return new double[]{defaultLat, defaultLng};
    }
}
