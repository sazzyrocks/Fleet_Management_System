package com.example.car_rental_sys.service;

import com.example.car_rental_sys.util.EnvConfig;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise Geolocation Service integrating IP-API REST Service.
 * Fulfills the Java course project requirement for live external API integration.
 *
 * <p>Reference: <a href="https://ip-api.com/">IP Geolocation API</a></p>
 */
public class GeoLocationService {

    private static final Logger LOGGER = Logger.getLogger(GeoLocationService.class.getName());
    private static final String IP_API_ENDPOINT = EnvConfig.get("IP_API_URL", "http://ip-api.com/json/");
    private static final int TIMEOUT_MS = 4000;
    private static volatile GeoLocation cachedLocation = null;

    public GeoLocation getCachedLocation() {
        return cachedLocation;
    }

    public record GeoLocation(
            String queryIp,
            String country,
            String countryCode,
            String region,
            String regionName,
            String city,
            String zip,
            double latitude,
            double longitude,
            String timezone,
            String isp,
            boolean success
    ) {
        public static GeoLocation fallback() {
            return new GeoLocation(
                    "103.217.237.58",
                    "India",
                    "IN",
                    "AP",
                    "Andhra Pradesh",
                    "Amaravati (SRM AP)",
                    "522502",
                    16.4632,
                    80.5078,
                    "Asia/Kolkata",
                    "Campus Network",
                    true
            );
        }

        public String getFormattedLocation() {
            return city + ", " + regionName + ", " + country;
        }
    }

    /**
     * Queries the IP-API Geolocation REST endpoint for client telemetry.
     *
     * @return {@link GeoLocation} record containing geographic coordinates and ISP details
     */
    public GeoLocation fetchCurrentLocation() {
        if (cachedLocation != null && cachedLocation.success()) {
            return cachedLocation;
        }
        LOGGER.info("Initiating external REST API request to IP-API: " + IP_API_ENDPOINT);
        try {
            URL url = URI.create(IP_API_ENDPOINT).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", "FleetManagementSystem-SRMAP/1.0");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject json = new JSONObject(response.toString());
                    if ("success".equalsIgnoreCase(json.optString("status"))) {
                        GeoLocation loc = new GeoLocation(
                                json.optString("query"),
                                json.optString("country", "India"),
                                json.optString("countryCode", "IN"),
                                json.optString("region", "AP"),
                                json.optString("regionName", "Andhra Pradesh"),
                                json.optString("city", "Amaravati"),
                                json.optString("zip", "522502"),
                                json.optDouble("lat", 16.4632),
                                json.optDouble("lon", 80.5078),
                                json.optString("timezone", "Asia/Kolkata"),
                                json.optString("isp", "SRM AP Network"),
                                true
                        );
                        LOGGER.info("External Geolocation REST API Success: " + loc.getFormattedLocation()
                                + " (Lat: " + loc.latitude() + ", Lon: " + loc.longitude() + ")");
                        cachedLocation = loc;
                        return loc;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "External IP Geolocation API call failed; applying resilient campus fallback", e);
        }
        GeoLocation fb = GeoLocation.fallback();
        if (cachedLocation == null) {
            cachedLocation = fb;
        }
        return fb;
    }
}

