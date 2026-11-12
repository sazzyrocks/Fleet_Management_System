package com.example.car_rental_sys.service;

import com.example.car_rental_sys.dao.DAOFactory;
import com.example.car_rental_sys.dao.UserDAO;
import com.example.car_rental_sys.model.Role;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.model.UserStatus;
import com.example.car_rental_sys.util.PasswordUtil;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Enterprise Authentication and Identity Management Service.
 */
public class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());

    private final UserDAO userDAO;
    private User currentUser; // Thread-safe in single-user desktop client session

    public AuthService() {
        this(DAOFactory.getInstance().getUserDAO());
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user by username and plaintext password using jBCrypt.
     *
     * @param username      username or email
     * @param plainPassword plaintext password
     * @return the authenticated {@link User} entity
     * @throws AuthenticationException if credentials are invalid or account is suspended
     */
    public synchronized User login(String username, String plainPassword) throws AuthenticationException {
        if (username == null || username.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty()) {
            throw new AuthenticationException("Username and password are required.");
        }

        String query = username.trim();
        Optional<User> userOpt = userDAO.findByUsername(query);
        if (userOpt.isEmpty()) {
            // Also attempt email login
            userOpt = userDAO.findByEmail(query);
        }

        if (userOpt.isEmpty()) {
            throw new AuthenticationException("Invalid username or password.");
        }

        User user = userOpt.get();

        // Check account status
        if (UserStatus.SUSPENDED.equals(user.getStatus())) {
            throw new AuthenticationException("This account has been suspended. Please contact customer service.");
        }

        // Verify password using jBCrypt salted hash
        boolean matches = PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash());
        if (!matches) {
            LOGGER.warning("Failed login attempt for user: " + username);
            throw new AuthenticationException("Invalid username or password.");
        }

        this.currentUser = user;
        LOGGER.info("User logged in successfully: " + user.getUsername() + " (Role: " + user.getRole() + ")");
        return user;
    }

    /**
     * Registers a new customer account.
     *
     * @param username      desired username
     * @param plainPassword plaintext password
     * @param email         email address
     * @param fullName      full name
     * @param phone         phone number
     * @return newly created User
     * @throws AuthenticationException if validation fails or user exists
     */
    public User register(String username, String plainPassword, String email, String fullName, String phone, String drivingLicense) throws AuthenticationException {
        if (username == null || username.trim().length() < 3) {
            throw new AuthenticationException("Username must be at least 3 characters.");
        }
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new AuthenticationException("Password must be at least 6 characters.");
        }
        if (email == null || !email.contains("@")) {
            throw new AuthenticationException("A valid email address is required.");
        }

        if (userDAO.existsByUsername(username.trim())) {
            throw new AuthenticationException("Username is already taken.");
        }
        if (userDAO.existsByEmail(email.trim())) {
            throw new AuthenticationException("Email is already registered.");
        }

        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        User newUser = new User();
        newUser.setUsername(username.trim());
        newUser.setPasswordHash(hashedPassword);
        newUser.setEmail(email.trim());
        newUser.setFullName(fullName != null ? fullName.trim() : username.trim());
        newUser.setPhone(phone);
        newUser.setDrivingLicense(drivingLicense);
        newUser.setRole(Role.CUSTOMER);
        newUser.setStatus(UserStatus.ACTIVE);

        return userDAO.save(newUser);
    }

    public User register(String username, String plainPassword, String email, String fullName, String phone) throws AuthenticationException {
        return register(username, plainPassword, email, fullName, phone, null);
    }

    /**
     * Logs out the current user and terminates session.
     */
    public synchronized void logout() {
        if (currentUser != null) {
            LOGGER.info("User logged out: " + currentUser.getUsername());
            currentUser = null;
        }
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public synchronized boolean isAuthenticated() {
        return currentUser != null;
    }

    public synchronized boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}
