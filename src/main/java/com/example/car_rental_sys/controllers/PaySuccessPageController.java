package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.util.SceneManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.net.URL;
import java.util.logging.Logger;

/**
 * Controller for Payment Success Confirmation Screen.
 * Plays the celebratory supercar animation video via HTML5 WebView with sound,
 * and auto-navigates to customer orders upon completion or skip.
 */
public class PaySuccessPageController extends Controller {

    private static final Logger LOGGER = Logger.getLogger(PaySuccessPageController.class.getName());

    @FXML public Pane mainPane;
    @FXML private WebView webview;
    @FXML private Button btnSkip;

    private PauseTransition autoNavTimer;
    private boolean isNavigating = false;
    private long screenLoadTime = 0L;

    @FXML
    public void initialize() {
        screenLoadTime = System.currentTimeMillis();
        loadCelebrationAnimation();
        setupKeyHandlers();
    }

    private void loadCelebrationAnimation() {
        if (webview == null) return;

        WebEngine engine = webview.getEngine();
        engine.setJavaScriptEnabled(true);

        // Listen for JavaScript alert commands from index.html (e.g. alert("CLOSE_PAY_SUCCESS"))
        engine.setOnAlert(event -> {
            if ("CLOSE_PAY_SUCCESS".equals(event.getData())) {
                Platform.runLater(this::handleViewOrders);
            }
        });

        // Load the celebration HTML5 video page
        URL resource = getClass().getResource("/com/example/car_rental_sys/html/paySuccess/index.html");
        if (resource != null) {
            engine.load(resource.toExternalForm());
        } else {
            LOGGER.warning("paySuccess index.html not found, fallback to orders in 3s");
            scheduleAutoNav(3);
            return;
        }

        // Global maximum safety timer (22s) so customer is never stuck
        scheduleAutoNav(22);
    }

    private void scheduleAutoNav(int seconds) {
        if (autoNavTimer != null) autoNavTimer.stop();
        autoNavTimer = new PauseTransition(Duration.seconds(seconds));
        autoNavTimer.setOnFinished(e -> handleViewOrders());
        autoNavTimer.play();
    }

    @FXML
    public void handleViewOrders() {
        if (isNavigating) return;
        // Minimum display guard: Prevent accidental instant skips from leftover keys or clicks
        if (System.currentTimeMillis() - screenLoadTime < 1500) {
            LOGGER.info("Skipping premature navigation request (minimum display guard active)");
            return;
        }
        isNavigating = true;

        if (autoNavTimer != null) {
            autoNavTimer.stop();
        }

        // Clean up webview to release video and audio resources
        if (webview != null) {
            try {
                webview.getEngine().load(null);
            } catch (Throwable ignored) {}
        }

        // Session guard: Prevent verification dialog loop when returning to customer orders
        CustomerServiceController.hasPromptedVerificationThisSession = true;

        Platform.runLater(() -> SceneManager.switchScene("customerServicePage.fxml"));
    }

    private void setupKeyHandlers() {
        Platform.runLater(() -> {
            if (mainPane != null) {
                mainPane.setFocusTraversable(true);
                mainPane.requestFocus();
                mainPane.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.SPACE
                            || event.getCode() == KeyCode.ESCAPE) {
                        handleViewOrders();
                    }
                });
            }
        });
    }
}
