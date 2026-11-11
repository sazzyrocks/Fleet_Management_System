package com.example.car_rental_sys.ui_components;

import com.example.car_rental_sys.StatusContainer;
import com.example.car_rental_sys.ToolsLib.DataTools;
import com.example.car_rental_sys.ToolsLib.DateTools;
import com.example.car_rental_sys.ToolsLib.ImageTools;
import com.example.car_rental_sys.controllers.AdminServiceController;
import com.example.car_rental_sys.controllers.CustomerServiceController;
import com.example.car_rental_sys.controllers.OrderDetailsComponentController;
import com.example.car_rental_sys.model.Role;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * Authentic visual table row component for showOrderComponent.fxml.
 */
public class UIOrderRow extends Pane {

    private Label lblOrderID, lblName, lblOrderDate, lblOrderAmount, lblOrderStatus;
    private String name, orderDate, orderAmount;
    private int carID = 1, orderID = 1;
    private int orderStatus = 5;
    public Image imgCar;

    public UIOrderRow() {
    }

    public UIOrderRow(int orderID, String name, String orderDate, int orderAmount, int orderStatus) {
        this.orderID = orderID;
        this.name = name;
        this.orderDate = DateTools.getSpecFormatDate(orderDate);
        this.orderAmount = String.format("₹%d.00", orderAmount);
        this.orderStatus = orderStatus;
        init();
    }

    private void init() {
        this.setLayoutX(0);
        this.setLayoutY(0);
        this.setPrefSize(460, 32);
        initData();
        initComponent();
        initTheme();
        initEvent();
    }

    private void initData() {
        try {
            String[] info = DataTools.getOrderInfoFromOrderID(String.valueOf(orderID));
            this.carID = Integer.parseInt(info[1]);
        } catch (Exception e) {
            this.carID = 1;
        }
    }

    private void initComponent() {
        lblOrderID = new Label();
        lblName = new Label();
        lblOrderDate = new Label();
        lblOrderAmount = new Label();
        lblOrderStatus = new Label();

        lblOrderID.setText("#" + orderID);
        String displayName = (name != null) ? name : "Vehicle #" + carID;
        if (displayName.length() > 16) {
            displayName = displayName.substring(0, 16) + "...";
        }
        lblName.setText(displayName);
        lblOrderDate.setText(orderDate);
        lblOrderAmount.setText(orderAmount);

        boolean isCustomer = (StatusContainer.currentUser == null || StatusContainer.currentUser.getRole() == Role.CUSTOMER);
        String statusStr;
        String pillStyle;

        if (orderStatus == -1) {
            statusStr = "CANCELLED";
            pillStyle = isCustomer
                    ? "-fx-background-color: rgba(245, 63, 63, 0.25); -fx-text-fill: #f76560;"
                    : "-fx-background-color: #ffece8; -fx-text-fill: #f53f3f;";
        } else if (orderStatus == 0 || orderStatus == 1 || orderStatus == 2) {
            statusStr = "CONFIRMED";
            pillStyle = isCustomer
                    ? "-fx-background-color: rgba(22, 93, 255, 0.25); -fx-text-fill: #6aa1ff;"
                    : "-fx-background-color: #e8f3ff; -fx-text-fill: #165dff;";
        } else if (orderStatus == 3 || orderStatus == 4) {
            statusStr = (orderStatus == 4) ? "ON RENT" : "CONTINUING";
            pillStyle = isCustomer
                    ? "-fx-background-color: rgba(255, 125, 0, 0.25); -fx-text-fill: #ff9a2e;"
                    : "-fx-background-color: #fff7e8; -fx-text-fill: #ff7d00;";
        } else {
            statusStr = "COMPLETED";
            pillStyle = isCustomer
                    ? "-fx-background-color: rgba(0, 180, 42, 0.25); -fx-text-fill: #4cd263;"
                    : "-fx-background-color: #e8ffea; -fx-text-fill: #00b42a;";
        }

        lblOrderStatus.setText(statusStr);
        lblOrderStatus.setStyle(pillStyle + " -fx-background-radius: 4px; -fx-padding: 3 8 3 8; -fx-font-weight: bold; -fx-font-size: 10px;");

        lblOrderID.setLayoutX(9);
        lblName.setLayoutX(90);
        lblOrderDate.setLayoutX(215);
        lblOrderAmount.setLayoutX(305);
        lblOrderStatus.setLayoutX(395);

        lblOrderID.setLayoutY(6);
        lblName.setLayoutY(6);
        lblOrderDate.setLayoutY(6);
        lblOrderAmount.setLayoutY(6);
        lblOrderStatus.setLayoutY(5);

        this.getChildren().addAll(lblOrderID, lblName, lblOrderDate, lblOrderAmount, lblOrderStatus);
    }

    private void initTheme() {
        boolean isCustomer = (StatusContainer.currentUser == null || StatusContainer.currentUser.getRole() == Role.CUSTOMER);

        lblOrderID.setStyle("-fx-text-fill: #4080ff; -fx-font-weight: bold;");
        lblName.setStyle("-fx-text-fill: " + (isCustomer ? "#ffffff" : "#1d2129") + "; -fx-font-weight: bold;");
        lblOrderDate.setStyle("-fx-text-fill: #86909c; -fx-font-weight: normal;");
        lblOrderAmount.setStyle("-fx-text-fill: " + (isCustomer ? "#ffffff" : "#1d2129") + "; -fx-font-weight: bold;");
    }

    private void initEvent() {
        this.setCursor(Cursor.HAND);
        boolean isCustomer = (StatusContainer.currentUser == null || StatusContainer.currentUser.getRole() == Role.CUSTOMER);

        this.setOnMouseEntered(e -> {
            this.setStyle(isCustomer
                    ? "-fx-background-color: rgba(255, 255, 255, 0.06); -fx-background-radius: 6px;"
                    : "-fx-background-color: #f2f3f5; -fx-background-radius: 6px;");
        });

        this.setOnMouseExited(e -> {
            this.setStyle("-fx-background-color: transparent;");
        });
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            StatusContainer.currentOrderID = this.orderID;

            if (CustomerServiceController.instance != null) {
                CustomerServiceController.instance.closeTrackOrder();
            }
            if (AdminServiceController.instance != null) {
                AdminServiceController.instance.closeTrackOrder();
            }

            loadImage();
            if (OrderDetailsComponentController.instance != null) {
                OrderDetailsComponentController.instance.updateOrderDetails(this);
            }
        });
    }

    private void loadImage() {
        imgCar = ImageTools.getCarImageObjFromCarID(this.carID);
    }

    public int getOrderID() {
        return orderID;
    }

    public String getName() {
        return name;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getOrderAmount() {
        return orderAmount;
    }

    public int getOrderStatus() {
        return orderStatus;
    }
}

