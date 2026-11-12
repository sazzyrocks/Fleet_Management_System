package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.StatusContainer;
import com.example.car_rental_sys.concurrency.AsyncExecutor;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.service.ServiceFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Controller for the Billing Summary and Spending Analytics sidebar.
 */
public class BillingSideBarController extends Controller {

    @FXML
    private Label billSideText;

    @FXML
    private Pane lineChart;

    @FXML
    private Pane radioChart;

    @FXML
    public void initialize() {
        loadTotalSpending();
    }

    private void loadTotalSpending() {
        AsyncExecutor.getInstance().execute(() -> {
            try {
                User user = ServiceFactory.getInstance().getAuthService().getCurrentUser();
                if (user == null) {
                    user = StatusContainer.currentUser;
                }

                BigDecimal total = BigDecimal.ZERO;
                if (user != null) {
                    total = ServiceFactory.getInstance().getPaymentService().getTotalSpendingByCustomer(user.getUserId());
                }

                final BigDecimal finalTotal = total != null ? total : BigDecimal.ZERO;
                DecimalFormat df = new DecimalFormat("#,##0.00");
                Platform.runLater(() -> {
                    if (billSideText != null) {
                        billSideText.setText("₹" + df.format(finalTotal));
                    }
                });
            } catch (Exception ignored) {
            }
        });
    }
}

