package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.util.SceneManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.net.URL;

/**
 * Controller for Payment Success Confirmation Screen.
 * Plays car animation video via WebView, then auto-navigates to orders.
 * Bidirectional JS-to-Java communication is supported via WebEngine alert callback.
 */
public class PaySuccessPageController extends Controller {

    @FXML public Pane mainPane;
    @FXML private WebView webview;
    @FXML private Pane navPane;

    private PauseTransition autoNavTimer;

    @FXML
    public void initialize() {
        loadPaySuccessAnimation();
    }

    private void loadPaySuccessAnimation() {
        if (webview == null) return;

        WebEngine engine = webview.getEngine();

        // Listen for JavaScript alert commands from index.html (e.g. alert("CLOSE_PAY_SUCCESS"))
        engine.setOnAlert(event -> {
            if ("CLOSE_PAY_SUCCESS".equals(event.getData())) {
                Platform.runLater(this::skipToOrders);
            }
        });

        // Load the pay-success HTML (plays video2.mp4 smoothly)
        URL resource = getClass().getResource(
                "/com/example/car_rental_sys/html/paySuccess/index.html");

        if (resource != null) {
            engine.load(resource.toExternalForm());
        } else {
            // fallback: navigate after 3s if resource is missing
            scheduleAutoNav(3);
            return;
        }

        // Auto-navigate to orders after video finishes (~16s) and user sees payment done
        autoNavTimer = new PauseTransition(Duration.seconds(30));
        autoNavTimer.setOnFinished(e -> goToOrders());
        autoNavTimer.play();

        // Let user press SPACE / ENTER / ESCAPE to skip immediately
        Platform.runLater(() -> {
            if (mainPane != null) {
                mainPane.setFocusTraversable(true);
                mainPane.requestFocus();
                mainPane.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.SPACE
                            || event.getCode() == KeyCode.ENTER
                            || event.getCode() == KeyCode.ESCAPE) {
                        skipToOrders();
                    }
                });
            }
        });
    }

    private void scheduleAutoNav(int seconds) {
        PauseTransition t = new PauseTransition(Duration.seconds(seconds));
        t.setOnFinished(e -> goToOrders());
        t.play();
    }

    private void skipToOrders() {
        if (autoNavTimer != null) autoNavTimer.stop();
        goToOrders();
    }

    private void goToOrders() {
        Platform.runLater(() -> SceneManager.switchScene("customerServicePage.fxml"));
    }
}
