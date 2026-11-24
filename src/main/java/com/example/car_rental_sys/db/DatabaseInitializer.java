package com.example.car_rental_sys.db;



import java.io.BufferedReader;

import java.io.InputStream;

import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;

import java.sql.Connection;

import java.sql.Statement;

import java.util.logging.Level;

import java.util.logging.Logger;



/**

 * Automated Database Schema Initializer.

 *

 * <p>Reads {@code schema.sql} from classpath resources and initializes database tables,

 * indexes, and seeds if they do not already exist. Enables zero-configuration setup.</p>

 */

public final class DatabaseInitializer {



    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());

    private static final String SCHEMA_FILE = "schema.sql";



    private DatabaseInitializer() {

        // Prevent instantiation of utility class

    }



    private static boolean tablesAlreadyExist() {

        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'fleet_management_db' AND table_name = 'vehicles'";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();

             Statement stmt = conn.createStatement();

             java.sql.ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {

                return rs.getInt(1) > 0;

            }

        } catch (Exception ignored) {

        }

        return false;

    }



    private static void ensureDatabaseExists() {

        try {

            DatabaseConnection dbConn = DatabaseConnection.getInstance();

            String fullUrl = dbConn.getUrl();

            String username = dbConn.getUsername();

            String password = dbConn.getPassword();



            if (fullUrl == null || username == null) {

                return;

            }



            // Extract server URL without DB name

            String serverUrl = fullUrl;

            String dbName = "fleet_management_db";

            int slashIndex = fullUrl.indexOf('/', "jdbc:mysql://".length());

            if (slashIndex != -1) {

                int questionIndex = fullUrl.indexOf('?', slashIndex);

                if (questionIndex != -1) {

                    dbName = fullUrl.substring(slashIndex + 1, questionIndex);

                    serverUrl = fullUrl.substring(0, slashIndex + 1) + fullUrl.substring(questionIndex);

                } else {

                    dbName = fullUrl.substring(slashIndex + 1);

                    serverUrl = fullUrl.substring(0, slashIndex + 1);

                }

            }



            try (Connection conn = java.sql.DriverManager.getConnection(serverUrl, username, password);

                 Statement stmt = conn.createStatement()) {

                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");

                LOGGER.info("Ensured database '" + dbName + "' exists on MySQL server.");

            }

        } catch (Exception e) {

            LOGGER.log(Level.WARNING, "Note during database pre-creation: {0}", e.getMessage());

        }

    }



    /**

     * Executes schema.sql against the configured database to ensure all required tables exist.

     *

     * @return true if initialization completed without errors, false otherwise

     */

    public static boolean initializeDatabase() {

        LOGGER.info("Starting automated database schema verification...");

        ensureDatabaseExists();



        if (tablesAlreadyExist()) {
            LOGGER.info("Database schema already initialized. Preserving existing fleet data.");
            try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
                ensureSchemaMigrations(conn);
                ensureDefaultUsers(conn);
                ensureDefaultVehicles(conn);
                ensureDefaultTodos(conn);
                ensureDashboardSeedData(conn);
                ensureDefaultVehicles(conn);
                ensureDefaultTodos(conn);
                ensureDashboardSeedData(conn);
            } catch (Exception ignored) {
            }
            return true;
        }



        try (InputStream in = DatabaseInitializer.class.getClassLoader().getResourceAsStream(SCHEMA_FILE)) {

            if (in == null) {

                LOGGER.log(Level.WARNING, "Schema file {0} not found on classpath. Skipping automatic initialization.", SCHEMA_FILE);

                return false;

            }



            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

                 Connection conn = DatabaseConnection.getInstance().getConnection();

                 Statement stmt = conn.createStatement()) {



                StringBuilder sqlBuilder = new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {

                    line = line.trim();

                    // Skip comments and empty lines

                    if (line.isEmpty() || line.startsWith("--") || line.startsWith("/*")) {

                        continue;

                    }

                    sqlBuilder.append(line).append(" ");

                    if (line.endsWith(";")) {

                        String statement = sqlBuilder.toString().trim();

                        if (!statement.isEmpty()) {

                            try {

                                stmt.execute(statement);

                            } catch (Exception ex) {

                                LOGGER.log(Level.FINE, "Statement execution note: {0}", ex.getMessage());

                            }

                        }

                        sqlBuilder.setLength(0);

                    }

                }

                ensureDefaultUsers(conn);
                ensureDefaultVehicles(conn);
                ensureDefaultTodos(conn);
                ensureDashboardSeedData(conn);

                LOGGER.info("Database schema initialized successfully.");

                return true;

            }

        } catch (Exception e) {

            LOGGER.log(Level.SEVERE, "Automated database initialization encountered an error: " + e.getMessage(), e);

            return false;

        }

    }



    private static void ensureSchemaMigrations(Connection conn) {
        ensureColumnExists(conn, "users", "driving_license", "VARCHAR(50) NULL");
        ensureColumnExists(conn, "users", "date_of_birth", "VARCHAR(20) NULL");
        ensureColumnExists(conn, "users", "address", "VARCHAR(255) NULL");
        ensureColumnExists(conn, "users", "bio", "TEXT NULL");
    }

    private static void ensureColumnExists(Connection conn, String table, String column, String colDef) {
        try {
            java.sql.DatabaseMetaData meta = conn.getMetaData();
            try (java.sql.ResultSet rs = meta.getColumns(null, null, table, column)) {
                if (!rs.next()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` " + colDef);
                        LOGGER.info("Added missing column '" + column + "' to table '" + table + "'");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Note during column check for {0}.{1}: {2}", new Object[]{table, column, e.getMessage()});
        }
    }

    private static void ensureDefaultUsers(Connection conn) {

        ensureAdminUser(conn);

        ensureDriverUser(conn);

        ensureCustomerUser(conn);

    }



    private static void ensureAdminUser(Connection conn) {

        String adminPass = "admin123";

        String adminHash = com.example.car_rental_sys.util.PasswordUtil.hashPassword(adminPass);

        String sql = "INSERT INTO `users` (`user_id`, `username`, `password_hash`, `email`, `phone`, `full_name`, `role`, `status`) " +

                     "VALUES (1, 'admin', ?, 'admin@fleetms.com', '+91 98765 00001', 'System Administrator', 'ADMIN', 'ACTIVE') " +

                     "ON DUPLICATE KEY UPDATE `password_hash` = ?, `status` = 'ACTIVE'";

        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, adminHash);

            pstmt.setString(2, adminHash);

            pstmt.executeUpdate();

            LOGGER.info("Verified default admin credentials (admin / admin123).");

        } catch (Exception e) {

            LOGGER.log(Level.WARNING, "Note verifying admin account: {0}", e.getMessage());

        }

    }



    private static void ensureDriverUser(Connection conn) {

        String driverPass = "driver123";

        String driverHash = com.example.car_rental_sys.util.PasswordUtil.hashPassword(driverPass);

        String sqlUser = "INSERT INTO `users` (`username`, `password_hash`, `email`, `phone`, `full_name`, `role`, `status`) " +

                         "VALUES ('driver', ?, 'driver@fleetms.com', '+91 98765 00002', 'Vikram Singh', 'DRIVER', 'ACTIVE') " +

                         "ON DUPLICATE KEY UPDATE `password_hash` = ?, `full_name` = 'Vikram Singh', `status` = 'ACTIVE', `role` = 'DRIVER'";

        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sqlUser)) {

            pstmt.setString(1, driverHash);

            pstmt.setString(2, driverHash);

            pstmt.executeUpdate();

            LOGGER.info("Verified default driver credentials (driver / driver123).");

        } catch (Exception e) {

            LOGGER.log(Level.WARNING, "Note verifying driver account: {0}", e.getMessage());

        }



        try (Statement stmt = conn.createStatement();

             java.sql.ResultSet rs = stmt.executeQuery("SELECT user_id FROM users WHERE username = 'driver'")) {

            if (rs.next()) {

                int driverUid = rs.getInt(1);

                String sqlDriver = "INSERT INTO `drivers` (`user_id`, `license_number`, `experience_years`, `rating`, `status`) " +

                                   "VALUES (" + driverUid + ", 'DL-AP-2024-8899', 5, 4.95, 'AVAILABLE') " +

                                   "ON DUPLICATE KEY UPDATE `rating` = 4.95, `status` = 'AVAILABLE'";

                stmt.executeUpdate(sqlDriver);

            }

        } catch (Exception ignored) {}

    }



    private static void ensureCustomerUser(Connection conn) {

        String customerPass = "customer123";

        String customerHash = com.example.car_rental_sys.util.PasswordUtil.hashPassword(customerPass);

        String sqlUser = "INSERT INTO `users` (`username`, `password_hash`, `email`, `phone`, `full_name`, `role`, `status`) " +

                         "VALUES ('customer', ?, 'customer@fleetms.com', '+91 98765 00003', 'Aarav Sharma', 'CUSTOMER', 'ACTIVE') " +

                         "ON DUPLICATE KEY UPDATE `password_hash` = ?, `full_name` = 'Aarav Sharma', `status` = 'ACTIVE', `role` = 'CUSTOMER'";

        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sqlUser)) {

            pstmt.setString(1, customerHash);

            pstmt.setString(2, customerHash);

            pstmt.executeUpdate();

            LOGGER.info("Verified default customer credentials (customer / customer123).");

        } catch (Exception e) {

            LOGGER.log(Level.WARNING, "Note verifying customer account: {0}", e.getMessage());

        }

    }



    public static void ensureDefaultVehicles(Connection conn) {
        String sql = "INSERT INTO `vehicles` (`vehicle_id`, `category_id`, `brand`, `model`, `year`, `license_plate`, `color`, `seats`, `transmission`, `fuel_type`, `daily_rate`, `mileage`, `status`, `image_path`) VALUES " +
                "(1, 3, 'Lamborghini', 'Huracan EVO', 2024, 'LMB-001', 'Arancio Xanto', 2, 'AUTOMATIC', 'PETROL', 350.00, 1200, 'AVAILABLE', '/com/example/car_rental_sys/image/cars/Huracan_EVO.png'), " +
                "(2, 4, 'Tesla', 'Model 3', 2024, 'TSL-002', 'Pearl White', 5, 'AUTOMATIC', 'ELECTRIC', 120.00, 4500, 'AVAILABLE', '/com/example/car_rental_sys/image/cars/Model_3.png'), " +
                "(3, 3, 'Porsche', '718 Cayman', 2024, 'POR-003', 'Guards Red', 2, 'AUTOMATIC', 'PETROL', 250.00, 2800, 'AVAILABLE', '/com/example/car_rental_sys/image/cars/Porsche_718.png'), " +
                "(4, 3, 'Ferrari', '488 Spider', 2023, 'FER-004', 'Rosso Corsa', 2, 'AUTOMATIC', 'PETROL', 450.00, 3100, 'AVAILABLE', '/com/example/car_rental_sys/image/cars/488_Spider.png'), " +
                "(5, 3, 'McLaren', '720S', 2024, 'MCL-005', 'Papaya Orange', 2, 'AUTOMATIC', 'PETROL', 420.00, 1500, 'AVAILABLE', '/com/example/car_rental_sys/image/cars/720S.png'), " +
                "(6, 1, 'BMW', 'M4 Competition', 2024, 'BMW-006', 'Isle of Man Green', 4, 'AUTOMATIC', 'PETROL', 180.00, 5200, 'AVAILABLE', '/com/example/car_rental_sys/image/cars/M4_Competition.png'), " +
                "(7, 3, 'Mercedes-Benz', 'AMG GT', 2024, 'MB-007', 'Selenite Grey', 2, 'AUTOMATIC', 'PETROL', 320.00, 2100, 'AVAILABLE', '/com/example/car_rental_sys/image/cars/Mercedes_AMG_GT.png'), " +
                "(8, 3, 'Audi', 'R8 V10', 2024, 'AUD-008', 'Mythos Black', 2, 'AUTOMATIC', 'PETROL', 380.00, 1800, 'AVAILABLE', '/com/example/car_rental_sys/image/cars/Audi_R8.png'), " +
                "(9, 3, 'Aston Martin', 'Vantage', 2024, 'AST-009', 'British Racing Green', 2, 'AUTOMATIC', 'PETROL', 390.00, 1400, 'AVAILABLE', '/com/example/car_rental_sys/image/cars/Aston_Martin_Vantage.png'), " +
                "(10, 1, 'Ford', 'Mustang GT', 2024, 'FRD-010', 'Velocity Blue', 4, 'AUTOMATIC', 'PETROL', 220.00, 4800, 'AVAILABLE', '/com/example/car_rental_sys/image/cars/Ford_Mustang_GT.png'), " +
                "(11, 3, 'Nissan', 'GT-R Nismo', 2024, 'NIS-011', 'Solid White', 4, 'AUTOMATIC', 'PETROL', 260.00, 3300, 'AVAILABLE', '/com/example/car_rental_sys/image/cars/Nissan_GTR.png'), " +
                "(12, 3, 'Ferrari', 'SF90 Spider', 2024, 'FER-012', 'Giallo Modena', 2, 'AUTOMATIC', 'PETROL', 520.00, 900, 'AVAILABLE', '/com/example/car_rental_sys/image/cars/Ferrari_SF90_Spider.png'), " +
                "(13, 3, 'Koenigsegg', 'Jesko Attack', 2024, 'KOE-013', 'Carbon Ghost', 2, 'AUTOMATIC', 'PETROL', 890.00, 400, 'AVAILABLE', '/com/example/car_rental_sys/image/cars/Koenigsegg.png') " +
                "ON DUPLICATE KEY UPDATE `status` = 'AVAILABLE', `daily_rate` = VALUES(`daily_rate`), `image_path` = VALUES(`image_path`), `model` = VALUES(`model`), `brand` = VALUES(`brand`);";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            LOGGER.info("Verified default fleet vehicle catalog in MySQL.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Note verifying default fleet vehicles: {0}", e.getMessage());
        }
    }

    public static void ensureDefaultTodos(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            int activeCount = 0;
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM `todos` WHERE `user_id` = 1 AND `is_completed` = 0")) {
                if (rs.next()) {
                    activeCount = rs.getInt(1);
                }
            }

            if (activeCount < 2) {
                stmt.executeUpdate("DELETE FROM `todos` WHERE `user_id` = 1");
                String insertSql = "INSERT INTO `todos` (`user_id`, `title`, `description`, `priority`, `is_completed`, `due_date`) VALUES " +
                        "(1, 'Submit OODJ', 'Complete and submit OODJ assignment and audit documentation', 'HIGH', 0, '2026-12-16'), " +
                        "(1, 'New task', 'Quarterly fleet inspection and telemetry audit', 'MEDIUM', 0, '2026-11-28')";
                stmt.executeUpdate(insertSql);
                LOGGER.info("Initialized default admin todos in MySQL.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Note verifying default todos: {0}", e.getMessage());
        }
    }

    public static void ensureDashboardSeedData(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // 1. Ensure diverse users for rich dashboard representation
            String hash = com.example.car_rental_sys.util.PasswordUtil.hashPassword("customer123");
            String insertUsers = "INSERT INTO `users` (`user_id`, `username`, `password_hash`, `email`, `phone`, `full_name`, `role`, `status`) VALUES " +
                    "(51, 'aarav_sharma', '" + hash + "', 'aarav@fleetms.com', '+91 98765 10001', 'Aarav-Sharma', 'CUSTOMER', 'ACTIVE'), " +
                    "(52, 'rohan_verma', '" + hash + "', 'rohan@fleetms.com', '+91 98765 10002', 'Rohan-Verma', 'CUSTOMER', 'ACTIVE'), " +
                    "(53, 'ananya_reddy', '" + hash + "', 'ananya@fleetms.com', '+91 98765 10003', 'Ananya-Reddy', 'CUSTOMER', 'ACTIVE'), " +
                    "(54, 'priya_patel', '" + hash + "', 'priya@fleetms.com', '+91 98765 10004', 'Priya-Patel', 'CUSTOMER', 'ACTIVE'), " +
                    "(55, 'paul_jackson', '" + hash + "', 'paul@fleetms.com', '+91 98765 10005', 'Paul-Jackson', 'DRIVER', 'ACTIVE'), " +
                    "(56, 'ryan_cameron', '" + hash + "', 'ryan@fleetms.com', '+91 98765 10006', 'Ryan-Cameron', 'DRIVER', 'ACTIVE'), " +
                    "(57, 'robert_murphy', '" + hash + "', 'robert@fleetms.com', '+91 98765 10007', 'Robert-Murphy', 'DRIVER', 'ACTIVE') " +
                    "ON DUPLICATE KEY UPDATE `full_name` = VALUES(`full_name`);";
            stmt.executeUpdate(insertUsers);

            // Ensure drivers exist
            String insertDrivers = "INSERT INTO `drivers` (`user_id`, `license_number`, `experience_years`, `rating`, `status`) VALUES " +
                    "(55, 'DL-AP-2024-9101', 5, 4.90, 'AVAILABLE'), " +
                    "(56, 'DL-AP-2024-9102', 6, 4.95, 'AVAILABLE'), " +
                    "(57, 'DL-AP-2024-9103', 4, 4.85, 'AVAILABLE') " +
                    "ON DUPLICATE KEY UPDATE `status` = 'AVAILABLE';";
            stmt.executeUpdate(insertDrivers);

            // Check if 7-day orders exist
            int recentOrdersCount = 0;
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM `orders` WHERE `created_at` >= DATE_SUB(NOW(), INTERVAL 6 DAY)")) {
                if (rs.next()) {
                    recentOrdersCount = rs.getInt(1);
                }
            }

            // If we have fewer than 20 recent orders in the last 7 days, seed a balanced 7-day distribution
            if (recentOrdersCount < 20) {
                int[][] daySpecs = {
                    { 6, 4, 2, 1 },
                    { 5, 6, 3, 2 },
                    { 4, 9, 7, 4 },
                    { 3, 11, 8, 7 },
                    { 2, 11, 9, 9 },
                    { 1, 8, 6, 5 },
                    { 0, 5, 4, 2 }
                };

                int[] customerIds = { 51, 52, 53, 54 };
                int[] vehicleIds = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 };
                double[] prices = { 1801.0, 2356.0, 2111.0, 2776.0, 3782.0, 4500.0, 5200.0, 3100.0, 1850.0 };

                int seedIndex = 1000;
                for (int[] spec : daySpecs) {
                    int daysAgo = spec[0];
                    int totalForDay = spec[1];
                    int activeForDay = spec[2];
                    int completedForDay = spec[3];

                    for (int k = 0; k < totalForDay; k++) {
                        seedIndex++;
                        String orderNum = String.format("ORD-SEED-%04d", seedIndex);
                        String txRef = String.format("95%08d", (seedIndex * 137L) % 100000000L);
                        int custId = customerIds[seedIndex % customerIds.length];
                        int vehId = vehicleIds[seedIndex % vehicleIds.length];
                        double price = prices[seedIndex % prices.length];

                        String status;
                        if (k < completedForDay) {
                            status = "COMPLETED";
                        } else if (k < activeForDay) {
                            status = "ACTIVE";
                        } else {
                            status = "CONFIRMED";
                        }

                        String orderSql = "INSERT INTO `orders` (`order_number`, `customer_id`, `vehicle_id`, `start_date`, `end_date`, `pickup_location`, `return_location`, `total_price`, `status`, `rental_mode`, `created_at`) VALUES " +
                                "('" + orderNum + "', " + custId + ", " + vehId + ", DATE_SUB(NOW(), INTERVAL " + daysAgo + " DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL " + daysAgo + " DAY), INTERVAL 3 DAY), 'SRM AP Fleet Hub', 'Amaravati Fleet Station', " + price + ", '" + status + "', 'SELF_DRIVE', DATE_SUB(NOW(), INTERVAL " + daysAgo + " DAY)) " +
                                "ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);";
                        stmt.executeUpdate(orderSql);

                        try (java.sql.ResultSet ordRs = stmt.executeQuery("SELECT `order_id` FROM `orders` WHERE `order_number` = '" + orderNum + "'")) {
                            if (ordRs.next()) {
                                int oId = ordRs.getInt(1);
                                String paySql = "INSERT INTO `payments` (`order_id`, `amount`, `payment_method`, `transaction_reference`, `payment_status`, `paid_at`) VALUES " +
                                        "(" + oId + ", " + price + ", 'CARD', '" + txRef + "', 'SUCCESS', DATE_SUB(NOW(), INTERVAL " + daysAgo + " DAY)) " +
                                        "ON DUPLICATE KEY UPDATE `payment_status` = 'SUCCESS';";
                                try (Statement pStmt = conn.createStatement()) {
                                    pStmt.executeUpdate(paySql);
                                }
                            }
                        }
                    }
                }
                LOGGER.info("Seeded 7-day historical fleet orders and payments for analytics dashboard.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Note during dashboard seeding: {0}", e.getMessage());
        }
    }

}

