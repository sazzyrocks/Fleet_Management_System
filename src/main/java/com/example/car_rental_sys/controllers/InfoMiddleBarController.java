package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.model.Role;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.service.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.net.URL;

/**
 * Controller for the Profile navigation Middle Bar component.
 */
public class InfoMiddleBarController {

    @FXML
    private Pane userMiddleBar;

    @FXML
    private Button btnBackToMain;

    @FXML
    private Button btnPersonInfo;

    public static InfoMiddleBarController instance;

    public InfoMiddleBarController() {
        instance = this;
    }

    @FXML
    private void initialize() {
        instance = this;
        initButtonStyle();
        initTheme();
        if (btnBackToMain != null) {
            btnBackToMain.setOnAction(e -> handleBackNavigation());
            btnBackToMain.setOnMouseClicked(e -> handleBackNavigation());
        }
    }

    private void initButtonStyle() {
        if (btnPersonInfo != null) {
            btnPersonInfo.getStyleClass().add("btnMiddleFocusStyle");
        }
    }

    private void initTheme() {
        User user = ServiceFactory.getInstance().getAuthService().getCurrentUser();
        if (user != null && user.getRole() == Role.CUSTOMER && userMiddleBar != null) {
            URL darkCss = getClass().getResource("/com/example/car_rental_sys/style/middleBarDark.css");
            if (darkCss != null) {
                userMiddleBar.getStylesheets().add(darkCss.toExternalForm());
            }
        }
    }

    @FXML
    void btnPersonInfoClicked(MouseEvent event) {
        barButtonClickEvent(event);
        User user = ServiceFactory.getInstance().getAuthService().getCurrentUser();
        if (user != null && user.getRole() == Role.CUSTOMER) {
            if (CustomerServiceController.instance != null) {
                CustomerServiceController.instance.showProfilePage();
            }
        } else if (user != null && user.getRole() == Role.ADMIN) {
            if (AdminServiceController.instance != null) {
                AdminServiceController.instance.showProfilePage();
            }
        } else {
            if (DriverMainPageController.driverMainPageInstance != null) {
                DriverMainPageController.driverMainPageInstance.showProfilePage();
            }
        }
    }

    @FXML
    private void barButtonClickEvent(MouseEvent e) {
        if (!(e.getSource() instanceof Button)) {
            return;
        }
        Button btn = (Button) e.getSource();
        clearFocusStyle();
        btn.getStyleClass().add("btnMiddleFocusStyle");
        btn.getStyleClass().remove("defaultMiddleBtnStyle");
    }

    private void clearFocusStyle() {
        if (btnPersonInfo != null) {
            btnPersonInfo.getStyleClass().remove("btnMiddleFocusStyle");
            btnPersonInfo.getStyleClass().add("defaultMiddleBtnStyle");
        }
    }

    @FXML
    private void btnBackToMainClicked(javafx.event.Event event) {
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
                com.example.car_rental_sys.util.SceneManager.switchScene("driverServicePage.fxml");
            }
            return;
        }

        if (role == Role.ADMIN) {
            if (AdminServiceController.instance != null) {
                AdminServiceController.instance.showDashboardPage();
            } else {
                com.example.car_rental_sys.util.SceneManager.switchScene("adminServicePage.fxml");
            }
            return;
        }

        if (CustomerServiceController.instance != null) {
            CustomerServiceController.instance.showOrderPage();
        } else {
            com.example.car_rental_sys.util.SceneManager.switchScene("customerServicePage.fxml");
        }
    }
}

