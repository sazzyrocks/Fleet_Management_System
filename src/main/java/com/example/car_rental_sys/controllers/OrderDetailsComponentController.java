package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.concurrency.AsyncExecutor;
import com.example.car_rental_sys.dao.DAOFactory;
import com.example.car_rental_sys.dao.OrderDAO;
import com.example.car_rental_sys.dao.VehicleDAO;
import com.example.car_rental_sys.model.Order;
import com.example.car_rental_sys.model.OrderStatus;
import com.example.car_rental_sys.model.Vehicle;
import com.example.car_rental_sys.model.VehicleStatus;
import com.example.car_rental_sys.service.ServiceFactory;
import com.example.car_rental_sys.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controller for viewing specific Order Specification and Invoice Summary.
 */
public class OrderDetailsComponentController extends Controller {

    public static OrderDetailsComponentController instance;
    private static Order selectedOrder;

    @FXML
    private Pane panelOrderDetails;

    @FXML
    private Label orderNum;

    @FXML
    private Label orderDate;

    @FXML
    private ImageView carImg;

    @FXML
    private Label carName;

    @FXML
    private Label carPrice;

    @FXML
    private Label orderQty;

    @FXML
    private Label paymentType;

    @FXML
    private Label lblLocation;

    @FXML
    private Label orderTime;

    @FXML
    private Label approveTime;

    @FXML
    private Label paymentTime;

    @FXML
    private Label pickUpTime;

    @FXML
    private Label dueTime;

    @FXML
    private Label lblFeedback;

    @FXML
    private Label totalAmount;

    @FXML
    private Button btnTrackOrder;

    @FXML
    private Button invoiceBtn;

    @FXML
    private ImageView invoiceIcon;

    @FXML
    private ImageView trackIcon;

    @FXML
    private Label lblOrderStatusBadge;

    @FXML
    private Button btnCancelRide;

    public static void setSelectedOrder(Order order) {
        selectedOrder = order;
    }

    public static Order getSelectedOrder() {
        return selectedOrder;
    }

    @FXML
    public void initialize() {
        instance = this;
        initTheme();
        if (selectedOrder != null) {
            populateOrderData();
        } else {
            clearOrderData();
        }
        attachButtonEvents();
    }

    private void initTheme() {
        boolean isCustomer = (com.example.car_rental_sys.StatusContainer.currentUser == null
                || com.example.car_rental_sys.StatusContainer.currentUser.getRole() == com.example.car_rental_sys.model.Role.CUSTOMER);
        if (isCustomer && panelOrderDetails != null) {
            try {
                java.net.URL css = getClass().getResource("/com/example/car_rental_sys/style/orderDetailsComponentDark.css");
                if (css != null) {
                    panelOrderDetails.getStylesheets().clear();
                    panelOrderDetails.getStylesheets().add(css.toExternalForm());
                }
            } catch (Exception ignored) {}
        }
    }

    public void updateOrderDetails(com.example.car_rental_sys.ui_components.UIOrderRow row) {
        if (row == null) return;
        if (orderNum != null) orderNum.setText(String.format("%09d", row.getOrderID()));
        if (orderDate != null) orderDate.setText(row.getOrderDate());
        if (carName != null) carName.setText(row.getName());
        if (carPrice != null) carPrice.setText(row.getOrderAmount());
        if (totalAmount != null) totalAmount.setText(row.getOrderAmount());
        if (carImg != null && row.imgCar != null) carImg.setImage(row.imgCar);

        AsyncExecutor.getInstance().execute(() -> {
            try {
                java.util.Optional<Order> orderOpt = ServiceFactory.getInstance().getRentalService().getOrderById(row.getOrderID());
                if (orderOpt.isPresent()) {
                    selectedOrder = orderOpt.get();
                    Platform.runLater(this::populateOrderData);
                }
            } catch (Exception ignored) {}
        });
    }

    public void populateOrderData() {
        if (selectedOrder == null) {
            clearOrderData();
            return;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (orderNum != null) orderNum.setText(String.format("%09d", selectedOrder.getOrderId()));
        if (orderDate != null && selectedOrder.getCreatedAt() != null) {
            orderDate.setText(selectedOrder.getCreatedAt().format(df));
        }

        if (paymentType != null) paymentType.setText("Credit Card / Digital Payment");
        if (lblLocation != null) {
            lblLocation.setText(selectedOrder.getPickupLocation() != null ? selectedOrder.getPickupLocation() : "SRM University-AP, Amaravati");
        }

        if (totalAmount != null && selectedOrder.getTotalPrice() != null) {
            totalAmount.setText("₹" + selectedOrder.getTotalPrice().toPlainString());
            if (carPrice != null) carPrice.setText("₹" + selectedOrder.getTotalPrice().toPlainString());
        }

        if (orderQty != null) {
            orderQty.setText("1");
        }

        if (orderTime != null && selectedOrder.getCreatedAt() != null) {
            orderTime.setText(selectedOrder.getCreatedAt().format(dtf));
        }
        if (approveTime != null && selectedOrder.getCreatedAt() != null) {
            approveTime.setText(selectedOrder.getCreatedAt().plusMinutes(5).format(dtf));
        }
        if (paymentTime != null && selectedOrder.getCreatedAt() != null) {
            paymentTime.setText(selectedOrder.getCreatedAt().plusMinutes(6).format(dtf));
        }
        if (pickUpTime != null && selectedOrder.getStartDate() != null) {
            pickUpTime.setText(selectedOrder.getStartDate().format(dtf));
        }
        if (dueTime != null && selectedOrder.getEndDate() != null) {
            dueTime.setText(selectedOrder.getEndDate().format(dtf));
        }

        OrderStatus status = selectedOrder.getStatus();
        if (status == null) status = OrderStatus.CONFIRMED;

        boolean isCustomer = (com.example.car_rental_sys.StatusContainer.currentUser == null
                || com.example.car_rental_sys.StatusContainer.currentUser.getRole() == com.example.car_rental_sys.model.Role.CUSTOMER);

        if (lblOrderStatusBadge != null) {
            lblOrderStatusBadge.setText(status.name());
            switch (status) {
                case CANCELLED -> {
                    lblOrderStatusBadge.setStyle(isCustomer
                            ? "-fx-background-color: rgba(245, 63, 63, 0.25); -fx-text-fill: #f76560; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;"
                            : "-fx-background-color: #ffece8; -fx-text-fill: #f53f3f; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                }
                case COMPLETED -> {
                    lblOrderStatusBadge.setStyle(isCustomer
                            ? "-fx-background-color: rgba(0, 180, 42, 0.25); -fx-text-fill: #27c346; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;"
                            : "-fx-background-color: #e8ffea; -fx-text-fill: #00b42a; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                }
                case ON_RENT -> {
                    lblOrderStatusBadge.setStyle(isCustomer
                            ? "-fx-background-color: rgba(20, 201, 201, 0.25); -fx-text-fill: #14C9C9; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;"
                            : "-fx-background-color: #e8fffb; -fx-text-fill: #14C9C9; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                }
                case PENDING -> {
                    lblOrderStatusBadge.setStyle(isCustomer
                            ? "-fx-background-color: rgba(255, 125, 0, 0.25); -fx-text-fill: #ff9a2e; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;"
                            : "-fx-background-color: #fff7e8; -fx-text-fill: #ff7d00; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                }
                default -> {
                    lblOrderStatusBadge.setStyle(isCustomer
                            ? "-fx-background-color: rgba(22, 93, 255, 0.25); -fx-text-fill: #6aa1ff; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;"
                            : "-fx-background-color: #e8f3ff; -fx-text-fill: #165dff; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                }
            }
        }

        if (btnCancelRide != null) {
            if (status == OrderStatus.CANCELLED) {
                btnCancelRide.setDisable(true);
                btnCancelRide.setText("Ride Cancelled");
                btnCancelRide.setStyle("");
            } else if (status == OrderStatus.COMPLETED) {
                btnCancelRide.setDisable(true);
                btnCancelRide.setText("Ride Completed");
                btnCancelRide.setStyle("");
            } else if (status == OrderStatus.ON_RENT) {
                btnCancelRide.setDisable(false);
                btnCancelRide.setText("Return Vehicle");
                btnCancelRide.setStyle("-fx-background-color: #00b42a; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
            } else {
                btnCancelRide.setDisable(false);
                btnCancelRide.setText("Cancel Ride");
                btnCancelRide.setStyle("");
            }
        }

        if (btnTrackOrder != null) {
            if (status == OrderStatus.CANCELLED || status == OrderStatus.COMPLETED) {
                btnTrackOrder.setDisable(true);
                btnTrackOrder.setOpacity(0.5);
            } else {
                btnTrackOrder.setDisable(false);
                btnTrackOrder.setOpacity(1.0);
            }
        }

        // Fetch vehicle details asynchronously
        AsyncExecutor.getInstance().execute(() -> {
            try {
                Optional<Vehicle> vOpt = ServiceFactory.getInstance().getVehicleService().getVehicleById(selectedOrder.getVehicleId());
                Platform.runLater(() -> {
                    if (vOpt.isPresent()) {
                        Vehicle v = vOpt.get();
                        if (carName != null) carName.setText(v.getBrand() + " " + v.getModel());
                        loadCarImage(carImg, v.getImagePath(), v.getModel());
                    } else {
                        if (carName != null) carName.setText("Fleet Vehicle #" + selectedOrder.getVehicleId());
                    }
                });
            } catch (Exception ignored) {
            }
        });
    }

    public void clearOrderData() {
        selectedOrder = null;
        if (orderNum != null) orderNum.setText("--");
        if (orderDate != null) orderDate.setText("--");
        if (carImg != null) carImg.setImage(null);
        if (carName != null) carName.setText("No Active Booking");
        if (carPrice != null) carPrice.setText("--");
        if (orderQty != null) orderQty.setText("--");
        if (paymentType != null) paymentType.setText("--");
        if (lblLocation != null) lblLocation.setText("--");
        if (orderTime != null) orderTime.setText("--");
        if (approveTime != null) approveTime.setText("--");
        if (paymentTime != null) paymentTime.setText("--");
        if (pickUpTime != null) pickUpTime.setText("--");
        if (dueTime != null) dueTime.setText("--");
        if (totalAmount != null) totalAmount.setText("--");
        if (lblOrderStatusBadge != null) {
            lblOrderStatusBadge.setText("NONE");
            lblOrderStatusBadge.setStyle("-fx-background-color: transparent; -fx-text-fill: transparent;");
        }
        if (btnTrackOrder != null) {
            btnTrackOrder.setDisable(true);
            btnTrackOrder.setOpacity(0.4);
        }
        if (btnCancelRide != null) {
            btnCancelRide.setDisable(true);
            btnCancelRide.setOpacity(0.4);
            btnCancelRide.setText("No Active Ride");
            btnCancelRide.setStyle("");
        }
        if (invoiceBtn != null) {
            invoiceBtn.setDisable(true);
            invoiceBtn.setOpacity(0.4);
        }
    }

    private void attachButtonEvents() {
        if (invoiceBtn != null) {
            invoiceBtn.setOnMouseClicked(event -> {
                InvoiceController.setSelectedOrder(selectedOrder);
                SceneManager.switchScene("Invoice.fxml");
            });
        }
        if (lblFeedback != null) {
            lblFeedback.setOnMouseClicked(event -> SceneManager.switchScene("ViewCommentsPage.fxml"));
        }
    }

    @FXML
    private void btnTrackOrderClicked(MouseEvent event) {
        TrackOrderController.setSelectedOrder(selectedOrder);
        if (CustomerServiceController.instance != null) {
            CustomerServiceController.instance.showTrackOrder();
        } else if (AdminServiceController.instance != null) {
            AdminServiceController.instance.showTrackOrder();
        } else {
            SceneManager.switchScene("customerServicePage.fxml");
        }
    }

    @FXML
    private void btnCancelRideClicked(MouseEvent event) {
        if (selectedOrder == null) return;
        OrderStatus status = selectedOrder.getStatus();
        if (status == OrderStatus.CANCELLED || status == OrderStatus.COMPLETED) {
            return;
        }

        // Return Vehicle Action when ON_RENT
        if (status == OrderStatus.ON_RENT) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Return Vehicle Confirmation");
            alert.setHeaderText("Return Vehicle for Order #" + String.format("%09d", selectedOrder.getOrderId()) + "?");
            alert.setContentText("Confirm returning the vehicle back to the fleet hub?\nThis concludes your rental.");

            ButtonType returnBtn = new ButtonType("Confirm Return", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelBtn = new ButtonType("Keep Driving", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(returnBtn, cancelBtn);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == returnBtn) {
                int orderId = selectedOrder.getOrderId();
                AsyncExecutor.getInstance().execute(() -> {
                    try {
                        ServiceFactory.getInstance().getRentalService().returnVehicle(orderId);
                        selectedOrder.setStatus(OrderStatus.COMPLETED);

                        Platform.runLater(() -> {
                            populateOrderData();
                            if (OrderListComponentController.instance != null) {
                                OrderListComponentController.instance.loadOrders();
                            }
                            if (CustomerSideBarController.instance != null) {
                                CustomerSideBarController.instance.populateCustomerData();
                            }
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Vehicle Returned");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("Thank you! The vehicle has been successfully returned and checked into the fleet.");
                            successAlert.showAndWait();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Return Failed");
                            errorAlert.setHeaderText(null);
                            errorAlert.setContentText("Could not process vehicle return: " + e.getMessage());
                            errorAlert.showAndWait();
                        });
                    }
                });
            }
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Ride Confirmation");
        alert.setHeaderText("Cancel Booking for Order #" + String.format("%09d", selectedOrder.getOrderId()) + "?");
        alert.setContentText("Are you sure you want to cancel this ride?\nThe vehicle will be released back to the fleet.");

        ButtonType cancelRideBtn = new ButtonType("Yes, Cancel Ride", ButtonBar.ButtonData.OK_DONE);
        ButtonType keepRideBtn = new ButtonType("Keep Booking", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(cancelRideBtn, keepRideBtn);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == cancelRideBtn) {
            int orderId = selectedOrder.getOrderId();
            int vehicleId = selectedOrder.getVehicleId();

            AsyncExecutor.getInstance().execute(() -> {
                try {
                    OrderDAO orderDAO = DAOFactory.getInstance().getOrderDAO();
                    VehicleDAO vehicleDAO = DAOFactory.getInstance().getVehicleDAO();

                    orderDAO.updateStatus(orderId, OrderStatus.CANCELLED);
                    vehicleDAO.updateStatus(vehicleId, VehicleStatus.AVAILABLE);

                    // Notify & remove from DeliveryOrderManager in-flight driver queue
                    com.example.car_rental_sys.service.DeliveryOrderManager.getInstance().cancelDispatchForOrder(orderId);

                    selectedOrder.setStatus(OrderStatus.CANCELLED);

                    Platform.runLater(() -> {
                        populateOrderData();

                        if (OrderListComponentController.instance != null) {
                            OrderListComponentController.instance.loadOrders();
                        }
                        if (CustomerSideBarController.instance != null) {
                            CustomerSideBarController.instance.populateCustomerData();
                        }

                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Ride Cancelled");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("Your ride has been successfully cancelled.");
                        successAlert.showAndWait();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Cancellation Failed");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("Could not cancel ride: " + e.getMessage());
                        errorAlert.showAndWait();
                    });
                }
            });
        }
    }

    private void loadCarImage(ImageView imageView, String path, String model) {
        if (imageView == null) return;
        try {
            Image img = com.example.car_rental_sys.ToolsLib.ImageTools.getCarImage(model, path);
            if (img != null && !img.isError()) {
                imageView.setImage(img);
            }
        } catch (Exception ignored) {
        }
    }
}

