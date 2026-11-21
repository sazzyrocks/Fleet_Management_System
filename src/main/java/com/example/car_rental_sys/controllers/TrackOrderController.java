package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.StatusContainer;
import com.example.car_rental_sys.concurrency.AsyncExecutor;
import com.example.car_rental_sys.model.Order;
import com.example.car_rental_sys.model.OrderStatus;
import com.example.car_rental_sys.model.Role;
import com.example.car_rental_sys.service.ServiceFactory;
import com.example.car_rental_sys.util.AlertHelper;
import com.example.car_rental_sys.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;

import java.net.URL;
import java.time.format.DateTimeFormatter;

/**
 * Controller for Live Order Tracking and Vehicle Pickup/Delivery Status.
 */
public class TrackOrderController extends Controller {

    private static Order selectedOrder;

    @FXML
    private Pane panelTracking;

    @FXML
    private Pane mapTracking;

    @FXML
    private Label lblOrderID;

    @FXML
    private ImageView leftIcon;

    @FXML
    private Button btnReceive;

    @FXML
    private Label lblLocation;

    @FXML
    private Label lblPickupTime;

    @FXML
    private Label lblPickupDate;

    @FXML
    private ImageView locationIcon;

    @FXML
    private TrackingStatusController trackingStatusController;

    public static void setSelectedOrder(Order order) {
        selectedOrder = order;
    }

    public static Order getSelectedOrder() {
        return selectedOrder;
    }

    @FXML
    public void initialize() {
        initTheme();
        populateTrackingInfo();
        initMap();
        attachButtonEvents();
    }

    private void initTheme() {
        boolean isCustomer = (StatusContainer.currentUser == null || StatusContainer.currentUser.getRole() == Role.CUSTOMER);
        if (isCustomer && panelTracking != null) {
            try {
                URL css = getClass().getResource("/com/example/car_rental_sys/style/trackingComponentDark.css");
                if (css != null) {
                    panelTracking.getStylesheets().clear();
                    panelTracking.getStylesheets().add(css.toExternalForm());
                }
            } catch (Exception ignored) {}
        }
    }

    private void initMap() {
        if (mapTracking == null) return;
        try {
            WebView webView = new WebView();
            webView.setPrefSize(290, 115);
            webView.setMaxSize(290, 115);
            URL mapUrl = getClass().getResource("/com/example/car_rental_sys/html/googleMap.html");
            if (mapUrl != null) {
                webView.getEngine().load(mapUrl.toExternalForm());
            }
            mapTracking.getChildren().setAll(webView);
        } catch (Exception ignored) {}
    }

    private void populateTrackingInfo() {
        if (selectedOrder != null) {
            if (lblOrderID != null) {
                lblOrderID.setText("ID #" + String.format("%09d", selectedOrder.getOrderId()));
            }
            if (lblLocation != null) {
                lblLocation.setText(selectedOrder.getPickupLocation() != null ? selectedOrder.getPickupLocation() : "SRM University-AP, Amaravati");
            }

            DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            if (lblPickupDate != null && selectedOrder.getStartDate() != null) {
                lblPickupDate.setText(selectedOrder.getStartDate().format(df));
            }
            if (lblPickupTime != null && selectedOrder.getStartDate() != null) {
                lblPickupTime.setText(selectedOrder.getStartDate().format(tf) + " - " + selectedOrder.getStartDate().plusHours(1).format(tf));
            }

            // Sync milestones with order status
            OrderStatus status = selectedOrder.getStatus();
            if (status == null) status = OrderStatus.CONFIRMED;

            if (trackingStatusController != null) {
                switch (status) {
                    case PENDING -> trackingStatusController.setMilestone(0);
                    case CONFIRMED -> trackingStatusController.setMilestone(1);
                    case ACTIVE -> trackingStatusController.setMilestone(2);
                    case ON_RENT, COMPLETED -> trackingStatusController.setMilestone(3);
                    case CANCELLED -> trackingStatusController.setMilestone(-1);
                }
            }

            if (btnReceive != null) {
                if (status == OrderStatus.ON_RENT) {
                    btnReceive.setDisable(true);
                    btnReceive.setText("Vehicle on Rent");
                } else if (status == OrderStatus.COMPLETED) {
                    btnReceive.setDisable(true);
                    btnReceive.setText("Rental Completed");
                } else if (status == OrderStatus.CANCELLED) {
                    btnReceive.setDisable(true);
                    btnReceive.setText("Ride Cancelled");
                } else if (status == OrderStatus.ACTIVE) {
                    btnReceive.setDisable(false);
                    btnReceive.setText("Confirm Keys Handover");
                } else {
                    btnReceive.setDisable(true);
                    btnReceive.setText("Driver En Route");
                }
            }
        }
    }

    private void attachButtonEvents() {
        if (btnReceive != null) {
            btnReceive.setOnAction(e -> handleOrderReceived());
        }
    }

    private void handleOrderReceived() {
        if (selectedOrder == null) return;

        btnReceive.setDisable(true);
        btnReceive.setText("Confirming...");

        AsyncExecutor.getInstance().execute(() -> {
            try {
                // Handover vehicle to customer (ACTIVE -> ON_RENT)
                ServiceFactory.getInstance().getRentalService().handoverVehicle(selectedOrder.getOrderId());
                selectedOrder.setStatus(OrderStatus.ON_RENT);

                Platform.runLater(() -> {
                    btnReceive.setText("Vehicle on Rent");
                    if (trackingStatusController != null) {
                        trackingStatusController.setMilestone(3);
                    }
                    if (CustomerSideBarController.instance != null) {
                        CustomerSideBarController.instance.populateCustomerData();
                    }
                    if (OrderListComponentController.instance != null) {
                        OrderListComponentController.instance.loadOrders();
                    }
                    AlertHelper.showSuccess("Handover Confirmed", "Keys received! Your vehicle is now actively on rent.\nWhen you are done driving, return the vehicle anytime from your Orders page.");
                    if (CustomerServiceController.instance != null) {
                        CustomerServiceController.instance.closeTrackOrder();
                    } else if (AdminServiceController.instance != null) {
                        AdminServiceController.instance.closeTrackOrder();
                    } else {
                        SceneManager.switchScene("customerServicePage.fxml");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btnReceive.setDisable(false);
                    btnReceive.setText("Confirm Keys Handover");
                    AlertHelper.showError("Confirmation Failed", e.getMessage());
                });
            }
        });
    }

    @FXML
    private void leftIconClicked(MouseEvent event) {
        if (CustomerServiceController.instance != null) {
            CustomerServiceController.instance.closeTrackOrder();
        } else if (AdminServiceController.instance != null) {
            AdminServiceController.instance.closeTrackOrder();
        } else {
            SceneManager.switchScene("customerServicePage.fxml");
        }
    }
}
