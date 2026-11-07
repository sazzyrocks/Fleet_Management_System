package com.example.car_rental_sys.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise Environment Configuration Manager.
 *
 * <p>Loads application secrets, external API keys, and database connection strings
 * from a local {@code .env} file with automatic fallback to System environment variables.</p>
 */
public final class EnvConfig {

    private static final Logger LOGGER = Logger.getLogger(EnvConfig.class.getName());
    private static final Map<String, String> ENV_STORE = new HashMap<>();
    private static boolean initialized = false;

    static {
        init();
    }

    private EnvConfig() {
        // Prevent instantiation
    }

    /**
     * Initializes environment variables by scanning candidate paths for .env.
     */
    public static synchronized void init() {
        if (initialized) return;

        File envFile = locateEnvFile();
        if (envFile != null && envFile.exists() && envFile.isFile()) {
            LOGGER.info("Loading environment configuration from: " + envFile.getAbsolutePath());
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int equalsIndex = line.indexOf('=');
                    if (equalsIndex > 0) {
                        String key = line.substring(0, equalsIndex).trim();
                        String value = line.substring(equalsIndex + 1).trim();
                        // Strip surrounding single or double quotes
                        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                            (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }
                        ENV_STORE.put(key, value);
                    }
                }
                LOGGER.info("Successfully loaded " + ENV_STORE.size() + " environment variables from .env");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error reading .env file", e);
            }
        } else {
            LOGGER.info("No .env file found. Using system environment variables and fallback defaults.");
        }

        syncGoogleMapsKey();
        initialized = true;
    }

    private static File locateEnvFile() {
        String[] candidatePaths = {
                ".env",
                "Fleet_Managemnet_System/.env",
                "../Fleet_Managemnet_System/.env",
                System.getProperty("user.dir") + File.separator + ".env",
                System.getProperty("user.dir") + File.separator + "Fleet_Managemnet_System" + File.separator + ".env"
        };

        for (String path : candidatePaths) {
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                return f;
            }
        }
        return null;
    }

    /**
     * Retrieves an environment property by key with a fallback default.
     *
     * @param key property name
     * @param defaultValue default value if key is not found
     * @return the resolved property value
     */
    public static String get(String key, String defaultValue) {
        if (ENV_STORE.containsKey(key)) {
            String val = ENV_STORE.get(key);
            if (val != null && !val.isBlank()) {
                return val;
            }
        }
        String sysEnv = System.getenv(key);
        if (sysEnv != null && !sysEnv.isBlank()) {
            return sysEnv;
        }
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }
        return defaultValue;
    }

    public static String get(String key) {
        return get(key, "");
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key, null);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key, null);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val.trim());
    }

    /**
     * Synchronizes GOOGLE_MAPS_API_KEY from .env strictly into target/classes runtime output.
     * NEVER writes to src/ to prevent committing secrets to version control.
     */
    private static void syncGoogleMapsKey() {
        String gKey = get("GOOGLE_MAPS_API_KEY", "").trim();
        if (gKey.isEmpty() || gKey.startsWith("your_")) {
            return;
        }

        String jsContent = "let googleMapKey = \"" + gKey + "\";\n";
        String[] runtimeOnlyTargets = {
                "Fleet_Managemnet_System/target/classes/com/example/car_rental_sys/html/googleMapKey.js",
                "target/classes/com/example/car_rental_sys/html/googleMapKey.js"
        };

        for (String targetPath : runtimeOnlyTargets) {
            File f = new File(targetPath);
            if (f.getParentFile() != null && f.getParentFile().exists()) {
                try (FileWriter writer = new FileWriter(f, StandardCharsets.UTF_8)) {
                    writer.write(jsContent);
                    LOGGER.info("Runtime-only sync of GOOGLE_MAPS_API_KEY into build output: " + targetPath);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
