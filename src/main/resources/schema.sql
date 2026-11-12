-- ==============================================================================
-- Fleet Management System - Database Schema (MySQL 8+)
-- Replaces legacy text-file storage with ACID-compliant relational tables.
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS `fleet_management_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `fleet_management_db`;

-- Disable foreign key checks during schema creation
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------------------------
-- 1. Table: users
-- ------------------------------------------------------------------------------
-- DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
    `user_id`       INT AUTO_INCREMENT PRIMARY KEY,
    `username`      VARCHAR(50)  NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL COMMENT 'BCrypt salted hash (60 chars)',
    `email`         VARCHAR(100) NOT NULL UNIQUE,
    `phone`         VARCHAR(20)  NULL,
    `full_name`     VARCHAR(100) NOT NULL,
    `avatar_path`   VARCHAR(255) NULL,
    `driving_license` VARCHAR(50) NULL,
    `date_of_birth` VARCHAR(20)  NULL,
    `address`       VARCHAR(255) NULL,
    `bio`           TEXT         NULL,
    `role`          ENUM('ADMIN', 'CUSTOMER', 'DRIVER') NOT NULL DEFAULT 'CUSTOMER',
    `status`        ENUM('ACTIVE', 'SUSPENDED', 'PENDING') NOT NULL DEFAULT 'ACTIVE',
    `created_at`    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_users_role` (`role`),
    INDEX `idx_users_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 2. Table: bank_cards
-- ------------------------------------------------------------------------------
-- DROP TABLE IF EXISTS `bank_cards`;
CREATE TABLE IF NOT EXISTS `bank_cards` (
    `card_id`      INT AUTO_INCREMENT PRIMARY KEY,
    `user_id`      INT NOT NULL,
    `card_number`  VARCHAR(30)  NOT NULL,
    `card_holder`  VARCHAR(100) NOT NULL,
    `expiry_date`  VARCHAR(10)  NOT NULL,
    `cvv`          VARCHAR(10)  NOT NULL,
    `balance`      DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    `card_type`    VARCHAR(30)  DEFAULT 'Visa',
    `is_default`   BOOLEAN DEFAULT FALSE,
    `created_at`   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE,
    INDEX `idx_bank_cards_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 3. Table: drivers
-- ------------------------------------------------------------------------------
-- DROP TABLE IF EXISTS `drivers`;
CREATE TABLE IF NOT EXISTS `drivers` (
    `driver_id`        INT AUTO_INCREMENT PRIMARY KEY,
    `user_id`          INT NOT NULL UNIQUE,
    `license_number`   VARCHAR(50) NOT NULL UNIQUE,
    `experience_years` INT DEFAULT 1,
    `rating`           DECIMAL(3, 2) DEFAULT 5.00,
    `status`           ENUM('AVAILABLE', 'BUSY', 'OFF_DUTY') DEFAULT 'AVAILABLE',
    `created_at`       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE,
    INDEX `idx_drivers_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 4. Table: vehicle_categories
-- ------------------------------------------------------------------------------
-- DROP TABLE IF EXISTS `vehicle_categories`;
CREATE TABLE IF NOT EXISTS `vehicle_categories` (
    `category_id` INT AUTO_INCREMENT PRIMARY KEY,
    `name`        VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 5. Table: vehicles
-- ------------------------------------------------------------------------------
-- DROP TABLE IF EXISTS `vehicles`;
CREATE TABLE IF NOT EXISTS `vehicles` (
    `vehicle_id`    INT AUTO_INCREMENT PRIMARY KEY,
    `category_id`   INT NULL,
    `brand`         VARCHAR(50)  NOT NULL,
    `model`         VARCHAR(50)  NOT NULL,
    `year`          INT NOT NULL,
    `license_plate` VARCHAR(20)  NOT NULL UNIQUE,
    `color`         VARCHAR(30)  NULL,
    `seats`         INT DEFAULT 5,
    `transmission`  ENUM('AUTOMATIC', 'MANUAL') DEFAULT 'AUTOMATIC',
    `fuel_type`     ENUM('PETROL', 'DIESEL', 'ELECTRIC', 'HYBRID') DEFAULT 'PETROL',
    `daily_rate`    DECIMAL(10, 2) NOT NULL,
    `mileage`       INT DEFAULT 0,
    `status`        ENUM('AVAILABLE', 'RENTED', 'MAINTENANCE', 'RESERVED') DEFAULT 'AVAILABLE',
    `image_path`    VARCHAR(255) NULL,
    `created_at`    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`category_id`) REFERENCES `vehicle_categories`(`category_id`) ON DELETE SET NULL,
    INDEX `idx_vehicles_status` (`status`),
    INDEX `idx_vehicles_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 6. Table: vehicle_diagnostics (Real-time telemetry / diagnostic data)
-- ------------------------------------------------------------------------------
-- DROP TABLE IF EXISTS `vehicle_diagnostics`;
CREATE TABLE IF NOT EXISTS `vehicle_diagnostics` (
    `diag_id`                   INT AUTO_INCREMENT PRIMARY KEY,
    `vehicle_id`                INT NOT NULL,
    `engine_temperature`        DECIMAL(5, 2) DEFAULT 90.00,
    `tire_pressure_front_left`  DECIMAL(4, 2) DEFAULT 32.00,
    `tire_pressure_front_right` DECIMAL(4, 2) DEFAULT 32.00,
    `tire_pressure_rear_left`   DECIMAL(4, 2) DEFAULT 32.00,
    `tire_pressure_rear_right`  DECIMAL(4, 2) DEFAULT 32.00,
    `battery_health_percent`    INT DEFAULT 100,
    `fuel_level_percent`        INT DEFAULT 100,
    `recorded_at`               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`vehicle_id`) REFERENCES `vehicles`(`vehicle_id`) ON DELETE CASCADE,
    INDEX `idx_diag_vehicle` (`vehicle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 7. Table: orders (Rentals / Bookings)
-- ------------------------------------------------------------------------------
-- DROP TABLE IF EXISTS `orders`;
CREATE TABLE IF NOT EXISTS `orders` (
    `order_id`        INT AUTO_INCREMENT PRIMARY KEY,
    `order_number`    VARCHAR(40)  NOT NULL UNIQUE,
    `customer_id`     INT NOT NULL,
    `vehicle_id`      INT NOT NULL,
    `driver_id`       INT NULL,
    `start_date`      DATETIME NOT NULL,
    `end_date`        DATETIME NOT NULL,
    `pickup_location` VARCHAR(100) NULL,
    `return_location` VARCHAR(100) NULL,
    `total_price`     DECIMAL(10, 2) NOT NULL,
    `status`          ENUM('PENDING', 'CONFIRMED', 'ACTIVE', 'ON_RENT', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    `rental_mode`     VARCHAR(20) DEFAULT 'SELF_DRIVE',
    `created_at`      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`customer_id`) REFERENCES `users`(`user_id`),
    FOREIGN KEY (`vehicle_id`)  REFERENCES `vehicles`(`vehicle_id`),
    FOREIGN KEY (`driver_id`)   REFERENCES `drivers`(`driver_id`) ON DELETE SET NULL,
    INDEX `idx_orders_customer` (`customer_id`),
    INDEX `idx_orders_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 8. Table: payments
-- ------------------------------------------------------------------------------
-- DROP TABLE IF EXISTS `payments`;
CREATE TABLE IF NOT EXISTS `payments` (
    `payment_id`            INT AUTO_INCREMENT PRIMARY KEY,
    `order_id`              INT NOT NULL,
    `amount`                DECIMAL(10, 2) NOT NULL,
    `payment_method`        VARCHAR(30) DEFAULT 'CARD',
    `transaction_reference` VARCHAR(100) UNIQUE,
    `payment_status`        ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    `paid_at`               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`order_id`) ON DELETE CASCADE,
    INDEX `idx_payments_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 9. Table: reviews (Comments & Ratings)
-- ------------------------------------------------------------------------------
-- DROP TABLE IF EXISTS `reviews`;
CREATE TABLE IF NOT EXISTS `reviews` (
    `review_id`    INT AUTO_INCREMENT PRIMARY KEY,
    `order_id`     INT NOT NULL,
    `user_id`      INT NOT NULL,
    `vehicle_id`   INT NOT NULL,
    `rating`       INT NOT NULL CHECK (`rating` BETWEEN 1 AND 5),
    `comment_text` TEXT NULL,
    `created_at`   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`order_id`)   REFERENCES `orders`(`order_id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`)    REFERENCES `users`(`user_id`) ON DELETE CASCADE,
    FOREIGN KEY (`vehicle_id`) REFERENCES `vehicles`(`vehicle_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 10. Table: messages
-- ------------------------------------------------------------------------------
-- DROP TABLE IF EXISTS `messages`;
CREATE TABLE IF NOT EXISTS `messages` (
    `message_id`  INT AUTO_INCREMENT PRIMARY KEY,
    `sender_id`   INT NOT NULL,
    `receiver_id` INT NOT NULL,
    `subject`     VARCHAR(150) NULL,
    `content`     TEXT NOT NULL,
    `is_read`     BOOLEAN DEFAULT FALSE,
    `sent_at`     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`sender_id`)   REFERENCES `users`(`user_id`) ON DELETE CASCADE,
    FOREIGN KEY (`receiver_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE,
    INDEX `idx_msg_receiver` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 11. Table: system_logs
-- ------------------------------------------------------------------------------
-- DROP TABLE IF EXISTS `system_logs`;
CREATE TABLE IF NOT EXISTS `system_logs` (
    `log_id`      INT AUTO_INCREMENT PRIMARY KEY,
    `user_id`     INT NULL,
    `action_type` VARCHAR(50) NOT NULL,
    `details`     TEXT NULL,
    `ip_address`  VARCHAR(45) NULL,
    `timestamp`   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE SET NULL,
    INDEX `idx_logs_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 12. Table: todos
-- ------------------------------------------------------------------------------
-- DROP TABLE IF EXISTS `todos`;
CREATE TABLE IF NOT EXISTS `todos` (
    `todo_id`      INT AUTO_INCREMENT PRIMARY KEY,
    `user_id`      INT NOT NULL,
    `title`        VARCHAR(150) NOT NULL,
    `description`  TEXT NULL,
    `priority`     ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    `is_completed` BOOLEAN DEFAULT FALSE,
    `due_date`     DATE NULL,
    `created_at`   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- Seed Initial Data
-- ------------------------------------------------------------------------------
-- Insert default categories
INSERT INTO `vehicle_categories` (`category_id`, `name`, `description`) VALUES
(1, 'Sedan', 'Comfortable 4-5 passenger sedans for everyday city driving'),
(2, 'SUV', 'Spacious sport utility vehicles suited for families and off-road'),
(3, 'Luxury', 'High-end executive vehicles featuring premium performance'),
(4, 'Electric', 'Eco-friendly modern electric vehicles with high efficiency');

-- Insert default Admin user (Password is 'admin123' hashed with jBCrypt)
-- Hash generated via BCrypt for 'admin123'
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `email`, `phone`, `full_name`, `role`, `status`) VALUES
(1, 'admin', '$2a$10$4ruqE8FlnERNCuIW/6pI6.1rlZmJiG/plwFwif5KPGxjwbM9Sm6je', 'admin@fleetms.com', '+1-555-0100', 'System Administrator', 'ADMIN', 'ACTIVE');

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
