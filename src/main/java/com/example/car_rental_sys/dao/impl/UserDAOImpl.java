package com.example.car_rental_sys.dao.impl;

import com.example.car_rental_sys.dao.DAOException;
import com.example.car_rental_sys.dao.UserDAO;
import com.example.car_rental_sys.db.DatabaseConnection;
import com.example.car_rental_sys.model.Role;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.model.UserStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Raw JDBC implementation of {@link UserDAO} using Prepared Statements for security and ACID compliance.
 */
public class UserDAOImpl implements UserDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setFullName(rs.getString("full_name"));
        user.setAvatarPath(rs.getString("avatar_path"));
        user.setRole(Role.fromString(rs.getString("role")));
        user.setStatus(UserStatus.fromString(rs.getString("status")));

        try {
            user.setDrivingLicense(rs.getString("driving_license"));
        } catch (SQLException ignored) {}
        try {
            user.setDateOfBirth(rs.getString("date_of_birth"));
        } catch (SQLException ignored) {}
        try {
            user.setAddress(rs.getString("address"));
        } catch (SQLException ignored) {}
        try {
            user.setBio(rs.getString("bio"));
        } catch (SQLException ignored) {}

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            user.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            user.setUpdatedAt(updated.toLocalDateTime());
        }
        return user;
    }

    @Override
    public Optional<User> findById(Integer id) throws DAOException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding user by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() throws DAOException {
        String sql = "SELECT * FROM users ORDER BY user_id ASC";
        List<User> users = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error retrieving all users", e);
        }
        return users;
    }

    @Override
    public User save(User user) throws DAOException {
        String sql = "INSERT INTO users (username, password_hash, email, phone, full_name, avatar_path, role, status, driving_license, date_of_birth, address, bio) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getFullName());
            stmt.setString(6, user.getAvatarPath());
            stmt.setString(7, user.getRole() != null ? user.getRole().name() : Role.CUSTOMER.name());
            stmt.setString(8, user.getStatus() != null ? user.getStatus().name() : UserStatus.ACTIVE.name());
            stmt.setString(9, user.getDrivingLicense());
            stmt.setString(10, user.getDateOfBirth());
            stmt.setString(11, user.getAddress());
            stmt.setString(12, user.getBio());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating user failed, no ID obtained.");
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DAOException("Error creating user: " + user.getUsername(), e);
        }
    }

    @Override
    public boolean update(User user) throws DAOException {
        String sql = "UPDATE users SET username = ?, email = ?, phone = ?, full_name = ?, " +
                     "avatar_path = ?, role = ?, status = ?, driving_license = ?, " +
                     "date_of_birth = ?, address = ?, bio = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getAvatarPath());
            stmt.setString(6, user.getRole() != null ? user.getRole().name() : Role.CUSTOMER.name());
            stmt.setString(7, user.getStatus() != null ? user.getStatus().name() : UserStatus.ACTIVE.name());
            stmt.setString(8, user.getDrivingLicense());
            stmt.setString(9, user.getDateOfBirth());
            stmt.setString(10, user.getAddress());
            stmt.setString(11, user.getBio());
            stmt.setInt(12, user.getUserId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error updating user ID: " + user.getUserId(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DAOException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error deleting user ID: " + id, e);
        }
    }

    @Override
    public boolean existsById(Integer id) throws DAOException {
        String sql = "SELECT 1 FROM users WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DAOException("Error checking existence of user ID: " + id, e);
        }
    }

    @Override
    public long count() throws DAOException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DAOException("Error counting users", e);
        }
        return 0;
    }

    @Override
    public Optional<User> findByUsername(String username) throws DAOException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding user by username: " + username, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) throws DAOException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding user by email: " + email, e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findByRole(Role role) throws DAOException {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY user_id ASC";
        List<User> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role != null ? role.name() : Role.CUSTOMER.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding users by role: " + role, e);
        }
        return list;
    }

    @Override
    public List<User> findByStatus(UserStatus status) throws DAOException {
        String sql = "SELECT * FROM users WHERE status = ? ORDER BY user_id ASC";
        List<User> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status != null ? status.name() : UserStatus.ACTIVE.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error finding users by status: " + status, e);
        }
        return list;
    }

    @Override
    public boolean updatePassword(int userId, String newPasswordHash) throws DAOException {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error updating password for user ID: " + userId, e);
        }
    }

    @Override
    public boolean updateStatus(int userId, UserStatus status) throws DAOException {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status != null ? status.name() : UserStatus.ACTIVE.name());
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error updating status for user ID: " + userId, e);
        }
    }

    @Override
    public boolean existsByUsername(String username) throws DAOException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DAOException("Error checking username existence: " + username, e);
        }
    }

    @Override
    public boolean existsByEmail(String email) throws DAOException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DAOException("Error checking email existence: " + email, e);
        }
    }
}
