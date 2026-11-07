package com.example.car_rental_sys.util;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Platform integration tools for triggering external communication:
 * 1. WhatsApp Desktop dispatch messaging (Green button)
 * 2. Windows Phone Link / Cellular direct dialing via tel: protocol (Blue button)
 */
public class PlatformTools {
    private static final Logger LOGGER = Logger.getLogger(PlatformTools.class.getName());

    /**
     * Opens WhatsApp Desktop or WhatsApp Web directly to chat/message with the customer.
     * Prefills the professional driver dispatch greeting.
     *
     * @param phoneNumber recipient phone number (e.g. +91 93463 89758)
     * @param recipientName name of customer or contact
     */
    public static void openWhatsAppMessage(String phoneNumber, String recipientName) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            phoneNumber = "+91 93463 89758";
        }

        String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
        if (cleanPhone.length() == 10) {
            cleanPhone = "91" + cleanPhone; // Add India country code if 10 digits
        }

        String message = "Hello " + (recipientName != null ? recipientName : "Customer") + ", this is your Fleet Delivery Driver regarding your vehicle reservation.";
        String encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8);

        String nativeUri = "whatsapp://send?phone=" + cleanPhone + "&text=" + encodedMsg;
        String webFallbackUrl = "https://api.whatsapp.com/send/?phone=" + cleanPhone + "&text=" + encodedMsg;

        try {
            // Launch WhatsApp Desktop directly via explorer.exe (bypasses browser)
            new ProcessBuilder("explorer.exe", nativeUri).start();
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "explorer.exe whatsapp:// launch failed, falling back to web URL", ex);
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(webFallbackUrl));
                } else {
                    new ProcessBuilder("cmd", "/c", "start", webFallbackUrl).start();
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to launch WhatsApp URL", e);
            }
        }
    }

    /**
     * Opens Windows Phone Link / cellular telephony dialer via tel: protocol (Way 1).
     * Dials directly to customer through linked phone SIM card.
     *
     * @param phoneNumber recipient phone number to dial
     */
    public static void openDialer(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            phoneNumber = "+91 93463 89758";
        }
        String cleanPhone = phoneNumber.replaceAll("[^0-9+]", "");
        String telUri = "tel:" + cleanPhone;

        try {
            // explorer.exe hands off tel: protocol directly to Windows Phone Link or default dialer
            new ProcessBuilder("explorer.exe", telUri).start();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to launch Windows Phone Link via explorer.exe", ex);
            try {
                new ProcessBuilder("cmd", "/c", "start", "", telUri).start();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to start tel protocol", e);
            }
        }
    }

    /**
     * Compatibility alias for WhatsApp call/message.
     */
    public static void callWhatsApp(String phoneNumber, String recipientName) {
        openWhatsAppMessage(phoneNumber, recipientName);
    }

    public static void callWhatsApp(String phoneNumber, String recipientName, boolean autoDial) {
        openWhatsAppMessage(phoneNumber, recipientName);
    }
}
