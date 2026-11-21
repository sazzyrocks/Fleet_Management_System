package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.StatusContainer;
import com.example.car_rental_sys.model.Role;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.service.ServiceFactory;
import com.example.car_rental_sys.ui_components.SwitchButton;
import com.example.car_rental_sys.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.net.URL;

/**
 * Controller for Application Preferences and Settings (SettingPage.fxml).
 * Uses authentic SwitchButton iOS-style toggle components.
 */
public class SettingPageController extends Controller {

    @FXML
    private Pane settingPane;

    @FXML
    private javafx.scene.control.Button btnBackToMain;

    @FXML
    private Pane switchPaneOne;

    @FXML
    private Pane switchPaneTwo;

    @FXML
    private Pane switchPaneThree;

    @FXML
    public void initialize() {
        initTheme();
        initBackBtn();

        if (switchPaneOne != null) {
            switchPaneOne.getChildren().setAll(new SwitchButton(true));
        }
        if (switchPaneTwo != null) {
            switchPaneTwo.getChildren().setAll(new SwitchButton(true));
        }
        if (switchPaneThree != null) {
            switchPaneThree.getChildren().setAll(new SwitchButton(false));
        }
    }

    private void initBackBtn() {
        if (btnBackToMain != null) {
            User currentUser = ServiceFactory.getInstance().getAuthService().getCurrentUser();
            if (currentUser != null && currentUser.getRole() == Role.DRIVER) {
                btnBackToMain.setText("← Back to Workplace");
            } else if (currentUser != null && currentUser.getRole() == Role.CUSTOMER) {
                btnBackToMain.setText("← Back to Orders");
            } else if (currentUser != null && currentUser.getRole() == Role.ADMIN) {
                btnBackToMain.setText("← Back to Overview");
            } else {
                btnBackToMain.setText("← Back");
            }

            btnBackToMain.setOnAction(e -> handleBackNavigation());
            btnBackToMain.setOnMouseClicked(e -> handleBackNavigation());
        }
    }

    private void initTheme() {
        User currentUser = ServiceFactory.getInstance().getAuthService().getCurrentUser();
        if (currentUser != null && currentUser.getRole() == Role.CUSTOMER && settingPane != null) {
            URL darkCss = getClass().getResource("/com/example/car_rental_sys/style/settingComponentDark.css");
            if (darkCss != null) {
                settingPane.getStylesheets().add(darkCss.toExternalForm());
            }
            if (btnBackToMain != null) {
                btnBackToMain.setStyle("-fx-background-color: #2a2b2e; -fx-background-radius: 6px; -fx-cursor: hand; -fx-text-fill: #4080ff; -fx-font-weight: bold; -fx-font-size: 13px;");
            }
        }
    }

    @FXML
    private void backToDashboardClicked(javafx.event.Event event) {
        handleBackNavigation();
    }

    private void handleBackNavigation() {
        User currentUser = ServiceFactory.getInstance().getAuthService().getCurrentUser();
        Role role = currentUser != null ? currentUser.getRole() : null;

        if (role == Role.DRIVER) {
            if (DriverMainPageController.driverMainPageInstance != null 
                    && DriverMainPageController.driverMainPageInstance.mainPane != null
                    && DriverMainPageController.driverMainPageInstance.mainPane.getScene() != null) {
                DriverMainPageController.driverMainPageInstance.showWorkplace();
            } else {
                SceneManager.switchScene("driverServicePage.fxml");
            }
            return;
        }

        if (role == Role.ADMIN) {
            if (AdminServiceController.instance != null) {
                AdminServiceController.instance.showDashboardPage();
            } else {
                SceneManager.switchScene("adminServicePage.fxml");
            }
            return;
        }

        // Default: Customer
        if (CustomerServiceController.instance != null) {
            CustomerServiceController.instance.showOrderPage();
        } else {
            SceneManager.switchScene("customerServicePage.fxml");
        }
    }
}
