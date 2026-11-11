package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.StatusContainer;
import com.example.car_rental_sys.model.Role;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * Controller for visual milestone dots in the Order Tracking component.
 */
public class TrackingStatusController extends Controller {

    @FXML
    private Pane panelStatus;

    @FXML
    private Pane pane1, pane2, pane3, pane4;

    @FXML
    private ImageView imgView1, imgView2, imgView3, imgView4;

    @FXML
    private Pane dotStatus1, dotStatus2, dotStatus3, dotStatus4;

    @FXML
    private Label lblStatusText1, lblStatusText2, lblStatusText3, lblStatusText4;

    @FXML
    public void initialize() {
        setMilestone(1);
    }

    public void setMilestone(int stage) {
        Pane[] dots = {dotStatus1, dotStatus2, dotStatus3, dotStatus4};
        Label[] labels = {lblStatusText1, lblStatusText2, lblStatusText3, lblStatusText4};
        boolean isCustomer = (StatusContainer.currentUser == null || StatusContainer.currentUser.getRole() == Role.CUSTOMER);

        for (int i = 0; i < dots.length; i++) {
            if (dots[i] != null) {
                if (stage == -1) {
                    dots[i].setStyle("-fx-background-color: #f53f3f; -fx-background-radius: 10px;");
                } else if (i <= stage) {
                    dots[i].setStyle("-fx-background-color: #165dff; -fx-background-radius: 10px;");
                } else {
                    dots[i].setStyle(isCustomer ? "-fx-background-color: #38383a; -fx-background-radius: 10px;" : "-fx-background-color: #c9cdd4; -fx-background-radius: 10px;");
                }
            }
            if (labels[i] != null) {
                if (stage == -1) {
                    labels[i].setStyle("-fx-text-fill: #f76560; -fx-font-weight: normal;");
                } else if (i <= stage) {
                    labels[i].setStyle(isCustomer ? "-fx-text-fill: #ffffff; -fx-font-weight: bold;" : "-fx-text-fill: #1d2129; -fx-font-weight: bold;");
                } else {
                    labels[i].setStyle(isCustomer ? "-fx-text-fill: #6e737d; -fx-font-weight: normal;" : "-fx-text-fill: #86909c; -fx-font-weight: normal;");
                }
            }
        }
    }
}
