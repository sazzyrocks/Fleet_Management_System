package com.example.car_rental_sys.dao.impl;

import com.example.car_rental_sys.dao.DAOException;
import com.example.car_rental_sys.dao.OrderDAO;
import com.example.car_rental_sys.db.DatabaseConnection;
import com.example.car_rental_sys.model.Order;
import com.example.car_rental_sys.model.OrderStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Raw JDBC implementation of {@link OrderDAO} with double-booking prevention.
 */
public class OrderDAOImpl implements OrderDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private static final String BASE_SELECT =
            "SELECT o.*, u.full_name AS customer_name, CONCAT(v.brand, ' ', v.model) AS vehicle_model, " +
            "du.full_name AS driver_name " +
            "FROM orders o " +
            "JOIN users u ON o.customer_id = u.user_id " +
            "JOIN vehicles v ON o.vehicle_id = v.vehicle_id " +
            "LEFT JOIN drivers d ON o.driver_id = d.driver_id " +
            "LEFT JOIN users du ON d.user_id = du.user_id ";

    private Order mapResultSet(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setOrderNumber(rs.getString("order_number"));
        o.setCustomerId(rs.getInt("customer_id"));
        o.setVehicleId(rs.getInt("vehicle_id"));

        int driverId = rs.getInt("driver_id");
        if (!rs.wasNull()) {
            o.setDriverId(driverId);
        }

        Timestamp start = rs.getTimestamp("start_date");
        if (start != null) o.setStartDate(start.toLocalDateTime());
        Timestamp end = rs.getTimestamp("end_date");
        if (end != null) o.setEndDate(end.toLocalDateTime());

        o.setPickupLocation(rs.getString("pickup_location"));
        o.setReturnLocation(rs.getString("return_location"));
        o.setTotalPrice(rs.getBigDecimal("total_price"));
        o.setStatus(OrderStatus.fromString(rs.getString("status")));

        try {
            String rmStr = rs.getString("rental_mode");
            if (rmStr != null) {
                o.setRentalMode(com.example.car_rental_sys.model.RentalMode.fromString(rmStr));
            } else if (o.getDriverId() != null) {
                o.setRentalMode(com.example.car_rental_sys.model.RentalMode.WITH_DRIVER);
            }
        } catch (SQLException ignored) {
            if (o.getDriverId() != null) {
                o.setRentalMode(com.example.car_rental_sys.model.RentalMode.WITH_DRIVER);
            }
        }

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) o.setCreatedAt(created.toLocalDateTime());

        // Projection metadata
        try { o.setCustomerName(rs.getString("customer_name")); } catch (SQLException ignored) {}
        try { o.setVehicleModel(rs.getString("vehicle_model")); } catch (SQLException ignored) {}
        try { o.setDriverName(rs.getString("driver_name")); } catch (SQLException ignored) {}

        return o;
    }

    @Override
    public Optional<Order> findById(Integer id) throws DAOException {
        String sql = BASE_SELECT + "WHERE o.order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding order by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findAll() throws DAOException {
        String sql = BASE_SELECT + "ORDER BY o.order_id DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error retrieving orders", e);
        }
        return list;
    }

    @Override
    public Order save(Order o) throws DAOException {
        String sql = "INSERT INTO orders (order_number, customer_id, vehicle_id, driver_id, " +
                     "start_date, end_date, pickup_location, return_location, total_price, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, o.getOrderNumber());
            stmt.setInt(2, o.getCustomerId());
            stmt.setInt(3, o.getVehicleId());
            if (o.getDriverId() != null) {
                stmt.setInt(4, o.getDriverId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setTimestamp(5, Timestamp.valueOf(o.getStartDate()));
            stmt.setTimestamp(6, Timestamp.valueOf(o.getEndDate()));
            stmt.setString(7, o.getPickupLocation());
            stmt.setString(8, o.getReturnLocation());
            stmt.setBigDecimal(9, o.getTotalPrice() != null ? o.getTotalPrice() : BigDecimal.ZERO);
            stmt.setString(10, o.getStatus() != null ? o.getStatus().name() : OrderStatus.PENDING.name());

            stmt.executeUpdate();
            try (ResultSet gk = stmt.getGeneratedKeys()) {
                if (gk.next()) {
                    o.setOrderId(gk.getInt(1));
                }
            }
            return o;
        } catch (SQLException e) {
            throw new DAOException("Error creating order: " + o.getOrderNumber(), e);
        }
    }

    @Override
    public boolean update(Order o) throws DAOException {
        String sql = "UPDATE orders SET customer_id = ?, vehicle_id = ?, driver_id = ?, " +
                     "start_date = ?, end_date = ?, pickup_location = ?, return_location = ?, " +
                     "total_price = ?, status = ? WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, o.getCustomerId());
            stmt.setInt(2, o.getVehicleId());
            if (o.getDriverId() != null) {
                stmt.setInt(3, o.getDriverId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setTimestamp(4, Timestamp.valueOf(o.getStartDate()));
            stmt.setTimestamp(5, Timestamp.valueOf(o.getEndDate()));
            stmt.setString(6, o.getPickupLocation());
            stmt.setString(7, o.getReturnLocation());
            stmt.setBigDecimal(8, o.getTotalPrice());
            stmt.setString(9, o.getStatus() != null ? o.getStatus().name() : OrderStatus.PENDING.name());
            stmt.setInt(10, o.getOrderId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error updating order ID: " + o.getOrderId(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DAOException {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error deleting order ID: " + id, e);
        }
    }

    @Override
    public boolean existsById(Integer id) throws DAOException {
        String sql = "SELECT 1 FROM orders WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DAOException("Error checking order existence: " + id, e);
        }
    }

    @Override
    public long count() throws DAOException {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DAOException("Error counting orders", e);
        }
        return 0;
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) throws DAOException {
        String sql = BASE_SELECT + "WHERE o.order_number = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding order by number: " + orderNumber, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findByCustomerId(int customerId) throws DAOException {
        String sql = BASE_SELECT + "WHERE o.customer_id = ? ORDER BY o.order_id DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding orders for customer: " + customerId, e);
        }
        return list;
    }

    @Override
    public List<Order> findByVehicleId(int vehicleId) throws DAOException {
        String sql = BASE_SELECT + "WHERE o.vehicle_id = ? ORDER BY o.order_id DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vehicleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding orders for vehicle: " + vehicleId, e);
        }
        return list;
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) throws DAOException {
        String sql = BASE_SELECT + "WHERE o.status = ? ORDER BY o.order_id DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status != null ? status.name() : OrderStatus.PENDING.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding orders by status: " + status, e);
        }
        return list;
    }

    @Override
    public boolean updateStatus(int orderId, OrderStatus status) throws DAOException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status != null ? status.name() : OrderStatus.PENDING.name());
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error updating order status for ID: " + orderId, e);
        }
    }

    @Override
    public boolean hasOverlappingRentals(int vehicleId, LocalDateTime start, LocalDateTime end, Integer excludeOrderId) throws DAOException {
        StringBuilder sql = new StringBuilder(
                "SELECT 1 FROM orders WHERE vehicle_id = ? " +
                "AND status NOT IN ('CANCELLED', 'COMPLETED') " +
                "AND (? < end_date AND ? > start_date) "
        );
        if (excludeOrderId != null) {
            sql.append("AND order_id != ? ");
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setInt(1, vehicleId);
            stmt.setTimestamp(2, Timestamp.valueOf(start));
            stmt.setTimestamp(3, Timestamp.valueOf(end));
            if (excludeOrderId != null) {
                stmt.setInt(4, excludeOrderId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Overlap detected if record returned
            }
        } catch (SQLException e) {
            throw new DAOException("Error validating rental schedule overlap for vehicle: " + vehicleId, e);
        }
    }

    @Override
    public BigDecimal calculateTotalRevenue() throws DAOException {
        String sql = "SELECT COALESCE(SUM(total_price), 0.00) FROM orders WHERE status IN ('CONFIRMED', 'ACTIVE', 'COMPLETED')";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            throw new DAOException("Error calculating total revenue", e);
        }
        return BigDecimal.ZERO;
    }
}
