package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.service.DashboardSyncService;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;

/**
 * Controller for Admin Analytics Dashboard (dashboard.fxml).
 * Restored with authentic animated web charts and financial metrics loaded via JavaFX WebView,
 * fully synchronized with the live MySQL fleet management database.
 */
public class dashboardController extends Controller {

    public static dashboardController instance;

    @FXML
    private Pane browserPane;

    private WebView webView;
    private WebEngine engine;

    @FXML
    public void initialize() {
        instance = this;
        loadAuthenticDashboard();
    }

    private void loadAuthenticDashboard() {
        if (browserPane == null) return;

        // Ensure data.js on disk is synchronized with live MySQL before loading
        try {
            DashboardSyncService.syncDataFiles();
        } catch (Exception ignored) {}

        webView = new WebView();
        webView.setPrefSize(1000, 750);
        engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);

        changePage("dashboard");

        browserPane.getChildren().setAll(webView);
    }

    public void refreshDashboard() {
        if (engine != null) {
            DashboardSyncService.sync(engine);
        }
    }

    public void changePage(String type) {
        if (engine == null && webView != null) {
            engine = webView.getEngine();
        }
        if (engine == null) return;

        String pagePath = "income".equalsIgnoreCase(type)
                ? "/com/example/car_rental_sys/html/incomeAnalysis/index.html"
                : "/com/example/car_rental_sys/html/dashboard/index.html";

        URL url = getClass().getResource(pagePath);
        if (url != null) {
            engine.load(url.toExternalForm());
        }

        if (AdminServiceController.instance != null) {
            AdminServiceController.instance.changeIcon("income".equalsIgnoreCase(type) ? "left" : "right");
        }
    }
}
