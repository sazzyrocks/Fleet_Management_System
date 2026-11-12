package com.example.car_rental_sys.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Main Customer Service Shell & Order Activity Portal.
 */
public class CustomerServiceController extends Controller {

    private static final Logger LOGGER = Logger.getLogger(CustomerServiceController.class.getName());

    public static CustomerServiceController instance;

    @FXML
    public AnchorPane mainPane;

    @FXML
    private Pane centerContainer;

    @FXML
    private Pane rightContainer;

    @FXML
    private Pane hugeContainer;

    @FXML
    private Pane middlePanel;

    @FXML
    private Pane settingMainPanel;

    @FXML
    public void initialize() {
        instance = this;
        showOrderPage();
        checkProfileCompletion();
    }

    private void checkProfileCompletion() {
        com.example.car_rental_sys.model.User user = com.example.car_rental_sys.StatusContainer.currentUser;
        if (user != null && user.getRole() == com.example.car_rental_sys.model.Role.CUSTOMER) {
            boolean missingDL = (user.getDrivingLicense() == null || user.getDrivingLicense().trim().isEmpty());
            boolean missingPhone = (user.getPhone() == null || user.getPhone().trim().isEmpty());
            if (missingDL || missingPhone) {
                javafx.application.Platform.runLater(() -> {
                    showAccSecurity();
                    com.example.car_rental_sys.util.AlertHelper.showWarning(
                            "Complete Verification",
                            "Please provide your Driving License and Contact Phone to complete account verification."
                    );
                });
            }
        }
    }

    public void showOrderPage() {
        if (hugeContainer != null) hugeContainer.setVisible(false);
        if (centerContainer != null) {
            centerContainer.setVisible(true);
            loadSubComponent(centerContainer, "showOrderComponent.fxml");
        }
        if (rightContainer != null) {
            rightContainer.setVisible(true);
            loadSubComponent(rightContainer, "OrderDetailsComponent.fxml");
        }
    }

    public void showOrderDetails() {
        if (rightContainer != null) {
            rightContainer.setVisible(true);
            loadSubComponent(rightContainer, "OrderDetailsComponent.fxml");
        }
    }

    public void showTrackOrder() {
        if (rightContainer != null) {
            rightContainer.setVisible(true);
            loadSubComponent(rightContainer, "TrackOrderComponent.fxml");
        }
    }

    public void closeTrackOrder() {
        showOrderDetails();
    }

    public void showWalletPage() {
        if (hugeContainer != null) hugeContainer.setVisible(false);
        if (centerContainer != null) {
            centerContainer.setVisible(true);
            loadSubComponent(centerContainer, "BillingComponent.fxml");
        }
        if (rightContainer != null) {
            rightContainer.setVisible(true);
            loadSubComponent(rightContainer, "BillingSideBar.fxml");
        }
    }

    private void initHugeSettingsContainer() {
        if (centerContainer != null) centerContainer.setVisible(false);
        if (rightContainer != null) rightContainer.setVisible(false);
        if (hugeContainer != null) {
            hugeContainer.setVisible(true);
            hugeContainer.getChildren().clear();
            if (middlePanel != null && settingMainPanel != null) {
                hugeContainer.getChildren().addAll(middlePanel, settingMainPanel);
            }
        }
    }

    public void showSettingPage() {
        initHugeSettingsContainer();
        if (middlePanel != null) loadSubComponent(middlePanel, "MiddleBar.fxml");
        if (settingMainPanel != null) loadSubComponent(settingMainPanel, "SettingPage.fxml");
    }

    public void showAccSecurity() {
        initHugeSettingsContainer();
        if (middlePanel != null) loadSubComponent(middlePanel, "MiddleBar.fxml");
        if (settingMainPanel != null) loadSubComponent(settingMainPanel, "AccountSecurityPage.fxml");
    }

    public void showPasswordPage() {
        initHugeSettingsContainer();
        if (middlePanel != null) loadSubComponent(middlePanel, "MiddleBar.fxml");
        if (settingMainPanel != null) loadSubComponent(settingMainPanel, "PasswordPage.fxml");
    }

    public void showProfilePage() {
        initHugeSettingsContainer();
        if (middlePanel != null) loadSubComponent(middlePanel, "InfoMiddleBar.fxml");
        if (settingMainPanel != null) loadSubComponent(settingMainPanel, "PersonalInfoPage.fxml");
    }

    private void loadSubComponent(Pane container, String fxmlName) {
        if (container == null) return;
        try {
            URL resource = getClass().getResource("/com/example/car_rental_sys/fxml/" + fxmlName);
            if (resource != null) {
                Parent root = FXMLLoader.load(resource);
                container.getChildren().setAll(root);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load sub component: " + fxmlName, e);
        }
    }
}
