package com.example.car_rental_sys.service;

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
import java.util.List;
import java.util.Optional;

/**
 * Enterprise In-Memory & Database Delivery Order Coordinator.
 * Manages pending vehicle delivery jobs, active dispatches, and route telemetry.
 * Connects directly to MySQL 8 DAO tier with fallback to realistic pre-seeded Andhra Pradesh fleet.
 */
public class DeliveryOrderManager {

    private static volatile DeliveryOrderManager instance;
    private final List<DeliveryOrder> availableOrders = new ArrayList<>();
    private DeliveryOrder activeDispatchOrder = null;

    private DeliveryOrderManager() {
        seedInitialDeliveryFleet();
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
     * Seeds initial delivery fleet matching realistic local Andhra Pradesh hubs.
     * Each order is paired with a specific client and direct contact number.
     */
    private void seedInitialDeliveryFleet() {
        availableOrders.clear();

        String name1 = EnvConfig.get("TEST_CLIENT_NAME_1", "SAZAL GOREY");
        String phone1 = EnvConfig.get("TEST_CLIENT_PHONE_1", "+91 93463 89758");

        String name2 = EnvConfig.get("TEST_CLIENT_NAME_2", "Arjun Rao");
        String phone2 = EnvConfig.get("TEST_CLIENT_PHONE_2", "+91 98765 00002");

        String name3 = EnvConfig.get("TEST_CLIENT_NAME_3", "Priya Patel");
        String phone3 = EnvConfig.get("TEST_CLIENT_PHONE_3", "+91 98765 00003");

        String name4 = EnvConfig.get("TEST_CLIENT_NAME_4", "Rajesh Kumar");
        String phone4 = EnvConfig.get("TEST_CLIENT_PHONE_4", "+91 98765 00004");

        String name5 = EnvConfig.get("TEST_CLIENT_NAME_5", "Ananya Reddy");
        String phone5 = EnvConfig.get("TEST_CLIENT_PHONE_5", "+91 98765 00005");

        // 1. Porsche 718 (Blue) - Demo Self-Drive
        DeliveryOrder o1 = new DeliveryOrder(
                101, "ORD-2026-101",
                "Porsche_718", "5KFC102",
                name1, "[DEMO] CEO VIP Client", "/com/example/car_rental_sys/image/avatar/1.png",
                "2 Seats", "4.0 L Twin-Turbo", "328 km/h",
                "SRM University-AP, Amaravati", 16.4632, 80.5078,
                "Vijayawada Junction (BZA Hub)", 16.5175, 80.6200,
                "2026-11-08", "2026-11-15",
                "#0052d4", "#4364f7",
                phone1
        );
        o1.setDemo(true);
        o1.setRentalMode(RentalMode.SELF_DRIVE);
        availableOrders.add(o1);

        // 2. Ferrari SF90 Spider (Golden Yellow) - Demo With Driver (Chauffeur)
        DeliveryOrder o2 = new DeliveryOrder(
                102, "ORD-2026-102",
                "Ferrari_SF90_Spider", "5VD4060",
                name2, "[DEMO] VIP3 Member", "/com/example/car_rental_sys/image/avatar/2.png",
                "2 Seats", "4.0 L V8 Hybrid", "340 km/h",
                "Guntur Central Railway Terminal", 16.2970, 80.4430,
                "Mangalagiri AIIMS Hospital", 16.4420, 80.5680,
                "2026-11-10", "2026-11-20",
                "#f7971e", "#ffd200",
                phone2
        );
        o2.setDemo(true);
        o2.setRentalMode(RentalMode.WITH_DRIVER);
        availableOrders.add(o2);

        // 3. Chevrolet Corvette ZR1 (White/Red) - Demo Self-Drive
        DeliveryOrder o3 = new DeliveryOrder(
                103, "ORD-2026-103",
                "Chevrolet_Corvette_ZR1", "MJA 230",
                name3, "[DEMO] Gold VIP Client", "/com/example/car_rental_sys/image/avatar/3.png",
                "4 Seats", "6.2 L Supercharged V8", "330 km/h",
                "Vijayawada Airport (VGA)", 16.5304, 80.7968,
                "Amaravati Government Secretariat", 16.5130, 80.5180,
                "2026-11-12", "2026-11-25",
                "#cb2d3e", "#ef473a",
                phone3
        );
        o3.setDemo(true);
        o3.setRentalMode(RentalMode.SELF_DRIVE);
        availableOrders.add(o3);

        // 4. Audi R8 (Green) - Demo With Driver (Chauffeur)
        DeliveryOrder o4 = new DeliveryOrder(
                104, "ORD-2026-104",
                "Audi_R8", "AP 39 R8 1001",
                name4, "[DEMO] Fleet Partner", "/com/example/car_rental_sys/image/avatar/4.png",
                "2 Seats", "5.2 L V10 Performance", "330 km/h",
                "SRM AP Campus", 16.4632, 80.5078,
                "Tadepalle Bypass Junction", 16.4820, 80.6010,
                "2026-11-14", "2026-11-28",
                "#11998e", "#38ef7d",
                phone4
        );
        o4.setDemo(true);
        o4.setRentalMode(RentalMode.WITH_DRIVER);
        availableOrders.add(o4);

        // 5. Huracan EVI (Black/Gold) - Demo Self-Drive
        DeliveryOrder o5 = new DeliveryOrder(
                105, "ORD-2026-105",
                "Huracan_EVI", "WC 8888 D",
                name5, "[DEMO] VIP Elite Partner", "/com/example/car_rental_sys/image/avatar/5.png",
                "2 Seats", "5.2 L V10 Naturally Aspirated", "325 km/h",
                "Mangalagiri AIIMS Complex", 16.4420, 80.5680,
                "Vijayawada Junction (BZA Hub)", 16.5175, 80.6200,
                "2026-11-15", "2026-11-30",
                "#232526", "#414345",
                phone5
        );
        o5.setDemo(true);
        o5.setRentalMode(RentalMode.SELF_DRIVE);
        availableOrders.add(o5);
    }

    /**
     * Returns combined list of real MySQL customer bookings plus active seeded delivery assignments.
     */
    public synchronized List<DeliveryOrder> getAvailableOrders() {
        List<DeliveryOrder> result = new ArrayList<>();

        // 1. Fetch real customer bookings from MySQL database if available
        try {
            var orderDao = DAOFactory.getInstance().getOrderDAO();
            var userDao = DAOFactory.getInstance().getUserDAO();
            var vehicleDao = DAOFactory.getInstance().getVehicleDAO();

            List<Order> dbOrders = new ArrayList<>();
            List<Order> confirmed = orderDao.findByStatus(OrderStatus.CONFIRMED);
            if (confirmed != null) dbOrders.addAll(confirmed);
            List<Order> pending = orderDao.findByStatus(OrderStatus.PENDING);
            if (pending != null) dbOrders.addAll(pending);

            for (Order o : dbOrders) {
                if (activeDispatchOrder != null && activeDispatchOrder.getOrderId() == o.getOrderId()) {
                    continue;
                }
                Optional<User> userOpt = userDao.findById(o.getCustomerId());
                Optional<Vehicle> vehicleOpt = vehicleDao.findById(o.getVehicleId());

                String rName = userOpt.map(User::getFullName).orElse("Valued Customer");
                String rPhone = userOpt.map(User::getPhone).orElse("+91 98765 00003");
                String cModel = vehicleOpt.map(v -> v.getModel().replace(" ", "_")).orElse("Porsche_718");
                String cPlate = vehicleOpt.map(Vehicle::getLicensePlate).orElse("AP 39 DL 1001");
                String seats = vehicleOpt.map(v -> v.getSeats() + " Seats").orElse("2 Seats");
                String power = "4.0 L Twin-Turbo";
                String speed = "320 km/h";

                DeliveryOrder dOrder = new DeliveryOrder(
                        o.getOrderId(),
                        o.getOrderNumber(),
                        cModel,
                        cPlate,
                        rName,
                        "VIP Client",
                        "/com/example/car_rental_sys/image/avatar/1.png",
                        seats,
                        power,
                        speed,
                        o.getPickupLocation() != null ? o.getPickupLocation() : "SRM University-AP, Amaravati",
                        16.4632, 80.5078,
                        o.getReturnLocation() != null ? o.getReturnLocation() : "Vijayawada Junction (BZA Hub)",
                        16.5175, 80.6200,
                        o.getStartDate() != null ? o.getStartDate().toLocalDate().toString() : "2026-11-08",
                        o.getEndDate() != null ? o.getEndDate().toLocalDate().toString() : "2026-11-15",
                        "#0052d4", "#4364f7",
                        rPhone
                );
                dOrder.setRentalMode(o.getRentalMode());
                dOrder.setDemo(false);
                result.add(dOrder);
            }
        } catch (Exception ignored) {
            // Offline or table not populated yet; fallback to seeded list
        }

        // 2. Add seeded demo orders ONLY if no real customer orders exist, preventing useless mock data clutter
        if (result.isEmpty()) {
            for (DeliveryOrder o : availableOrders) {
                if (activeDispatchOrder == null || activeDispatchOrder.getOrderId() != o.getOrderId()) {
                    result.add(o);
                }
            }
        }

        return result;
    }

    public synchronized Optional<DeliveryOrder> findOrderById(int orderId) {
        if (activeDispatchOrder != null && activeDispatchOrder.getOrderId() == orderId) {
            return Optional.of(activeDispatchOrder);
        }
        return availableOrders.stream().filter(o -> o.getOrderId() == orderId).findFirst();
    }

    public synchronized void acceptOrder(DeliveryOrder order) {
        order.setStatus(OrderStatus.ACTIVE);
        availableOrders.removeIf(o -> o.getOrderId() == order.getOrderId());
        this.activeDispatchOrder = order;

        // Sync status to database only for real orders (not demo mock orders)
        if (!order.isDemo()) {
            try {
                DAOFactory.getInstance().getOrderDAO().updateStatus(order.getOrderId(), OrderStatus.ACTIVE);
            } catch (Exception ignored) {}
        }
    }

    public synchronized void completeActiveDelivery() {
        if (activeDispatchOrder != null) {
            DeliveryOrder order = activeDispatchOrder;
            OrderStatus nextStatus = (order.getRentalMode() == RentalMode.SELF_DRIVE)
                    ? OrderStatus.ON_RENT
                    : OrderStatus.COMPLETED;

            order.setStatus(nextStatus);
            if (!order.isDemo()) {
                try {
                    DAOFactory.getInstance().getOrderDAO().updateStatus(order.getOrderId(), nextStatus);
                    // If it was with driver and now completed, release vehicle to AVAILABLE
                    if (nextStatus == OrderStatus.COMPLETED) {
                        DAOFactory.getInstance().getOrderDAO().findById(order.getOrderId()).ifPresent(dbOrder -> {
                            try {
                                DAOFactory.getInstance().getVehicleDAO().updateStatus(dbOrder.getVehicleId(), VehicleStatus.AVAILABLE);
                            } catch (Exception ignored) {}
                        });
                    }
                } catch (Exception ignored) {}
            }
            this.activeDispatchOrder = null;
        }
    }

    public synchronized void cancelDispatchForOrder(int orderId) {
        availableOrders.removeIf(o -> o.getOrderId() == orderId);
        if (activeDispatchOrder != null && activeDispatchOrder.getOrderId() == orderId) {
            activeDispatchOrder.setStatus(OrderStatus.CANCELLED);
            this.activeDispatchOrder = null;
        }
    }

    public synchronized void dismissOrder(int orderId) {
        availableOrders.removeIf(o -> o.getOrderId() == orderId);
    }

    public synchronized DeliveryOrder getActiveDispatchOrder() {
        return activeDispatchOrder;
    }

    public synchronized boolean hasActiveDispatch() {
        return activeDispatchOrder != null;
    }

    public synchronized void resetAll() {
        activeDispatchOrder = null;
        seedInitialDeliveryFleet();
    }
}
