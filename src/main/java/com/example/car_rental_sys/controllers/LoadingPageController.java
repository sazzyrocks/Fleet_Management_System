package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.concurrency.AsyncExecutor;
import com.example.car_rental_sys.db.DatabaseInitializer;
import com.example.car_rental_sys.util.SceneManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the startup splash / loading screen.
 */
public class LoadingPageController extends Controller {

    private static final Logger LOGGER = Logger.getLogger(LoadingPageController.class.getName());

    @FXML
    private WebView webView;

    @FXML
    private Label loadingText;

    @FXML
    public void initialize() {
        if (loadingText != null) {
            loadingText.setAlignment(Pos.CENTER);
            loadingText.setText("Initializing Fleet Management System...");
        }

        loadHtmlSplash();
        startBackgroundInitialization();
    }

    private void loadHtmlSplash() {
        if (webView == null) {
            return;
        }

        try {
            WebEngine webEngine = webView.getEngine();
            URL resource = getClass().getResource("/com/example/car_rental_sys/html/loading/loading.html");
            if (resource != null) {
                webEngine.load(resource.toExternalForm());
                webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        webView.setVisible(true);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Splash animation could not be loaded: " + e.getMessage());
        }
    }

    private void startBackgroundInitialization() {
        AsyncExecutor.getInstance().execute(() -> {
            try {
                // Pre-fetch geolocation concurrently in background so Driver and Main maps have it ready instantly
                com.example.car_rental_sys.concurrency.AsyncExecutor.getInstance().execute(() -> {
                    try {
                        com.example.car_rental_sys.service.ServiceFactory.getInstance().getGeoLocationService().fetchCurrentLocation();
                    } catch (Exception ignored) {}
                });

                updateStatus("Verifying database schema...");
                boolean dbReady = DatabaseInitializer.initializeDatabase();
                if (dbReady) {
                    LOGGER.info("Database schema verified successfully.");
                } else {
                    LOGGER.warning("Database schema initialization returned false, proceeding in offline mode.");
                }

                Thread.sleep(800);
                updateStatus("Starting enterprise services...");
                Thread.sleep(600);
                updateStatus("System Ready!");

                Platform.runLater(() -> {
                    PauseTransition pause = new PauseTransition(Duration.millis(500));
                    pause.setOnFinished(event -> SceneManager.switchScene("loginPage.fxml"));
                    pause.play();
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Startup error during initialization: " + e.getMessage(), e);
                Platform.runLater(() -> {
                    updateStatus("Loading interface...");
                    SceneManager.switchScene("loginPage.fxml");
                });
            }
        });
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> {
            if (loadingText != null) {
                loadingText.setText(message);
            }
        });
    }
}

