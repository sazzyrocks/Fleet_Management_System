package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.concurrency.AsyncExecutor;
import com.example.car_rental_sys.dao.DAOFactory;
import com.example.car_rental_sys.dao.DriverDAO;
import com.example.car_rental_sys.model.Driver;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.service.AuthService;
import com.example.car_rental_sys.service.ServiceFactory;
import com.example.car_rental_sys.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.Optional;

/**
 * Controller for the Driver Portal Sidebar.
 */
public class DriverSideController extends Controller {

    @FXML
    private ImageView avatarImageView;

    @FXML
    private Label nameTextLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label postLabel;

    @FXML
    private Label rating;

    @FXML
    private Label exp;

    @FXML
    private Label speed;

    @FXML
    private Pane rowProfile;
    @FXML
    private Pane rowMessage;
    @FXML
    private Pane rowWorkplace;
    @FXML
    private Pane rowSetting;
    @FXML
    private Pane rowLogout;

    @FXML
    private Pane item1; // Profile

    @FXML
    private Pane item2; // Message

    @FXML
    private Pane item3; // Workplace

    @FXML
    private Pane item4; // Setting

    @FXML
    private Pane item5; // Logout

    @FXML
    private ImageView imgIcon1;
    @FXML
    private ImageView imgIcon2;
    @FXML
    private ImageView imgIcon3;
    @FXML
    private ImageView imgIcon4;
    @FXML
    private ImageView imgIcon5;

    @FXML
    public void initialize() {
        populateDriverData();
        attachNavigationHandlers();
    }

    private void populateDriverData() {
        AuthService authService = ServiceFactory.getInstance().getAuthService();
        User currentUser = authService.getCurrentUser();

        if (currentUser != null) {
            String displayName = currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getUsername();
            if (nameTextLabel != null) nameTextLabel.setText(displayName);
            if (nameLabel != null) nameLabel.setText(displayName);
            if (postLabel != null) postLabel.setText("Fleet Driver");

            // Query driver telemetry asynchronously
            AsyncExecutor.getInstance().execute(() -> {
                try {
                    DriverDAO driverDAO = DAOFactory.getInstance().getDriverDAO();
                    Optional<Driver> driverOpt = driverDAO.findByUserId(currentUser.getUserId());

                    Platform.runLater(() -> {
                        if (driverOpt.isPresent()) {
                            Driver d = driverOpt.get();
                            if (rating != null) {
                                rating.setText(String.format("%.1f", d.getRating() != null ? d.getRating().doubleValue() : 5.0));
                            }
                            if (exp != null) {
                                exp.setText(String.valueOf(d.getExperienceYears() != null ? d.getExperienceYears() : 3));
                            }
                            if (speed != null) {
                                speed.setText("Active");
                            }
                        } else {
                            if (rating != null) rating.setText("5.0");
                            if (exp != null) exp.setText("1");
                            if (speed != null) speed.setText("Ready");
                        }
                    });
                } catch (Exception ignored) {
                }
            });
        }
    }

    private void attachNavigationHandlers() {
        Runnable openProfile = () -> {
            if (DriverMainPageController.driverMainPageInstance != null) {
                DriverMainPageController.driverMainPageInstance.showProfilePage();
            } else {
                SceneManager.switchScene("driverServicePage.fxml");
                Platform.runLater(() -> {
                    if (DriverMainPageController.driverMainPageInstance != null) {
                        DriverMainPageController.driverMainPageInstance.showProfilePage();
                    }
                });
            }
        };
        if (rowProfile != null) rowProfile.setOnMouseClicked(e -> openProfile.run());
        if (item1 != null) item1.setOnMouseClicked(e -> openProfile.run());
        if (imgIcon1 != null) imgIcon1.setOnMouseClicked(e -> openProfile.run());

        Runnable openMessage = () -> SceneManager.switchScene("messagePage.fxml");
        if (rowMessage != null) rowMessage.setOnMouseClicked(e -> openMessage.run());
        if (item2 != null) item2.setOnMouseClicked(e -> openMessage.run());
        if (imgIcon2 != null) imgIcon2.setOnMouseClicked(e -> openMessage.run());

        Runnable openWorkplace = () -> {
            if (DriverMainPageController.driverMainPageInstance != null) {
                DriverMainPageController.driverMainPageInstance.showWorkplace();
            } else {
                SceneManager.switchScene("driverServicePage.fxml");
            }
        };
        if (rowWorkplace != null) rowWorkplace.setOnMouseClicked(e -> openWorkplace.run());
        if (item3 != null) item3.setOnMouseClicked(e -> openWorkplace.run());
        if (imgIcon3 != null) imgIcon3.setOnMouseClicked(e -> openWorkplace.run());

        Runnable openSetting = () -> {
            if (DriverMainPageController.driverMainPageInstance != null) {
                DriverMainPageController.driverMainPageInstance.showSettingPage();
            } else {
                SceneManager.switchScene("driverServicePage.fxml");
                Platform.runLater(() -> {
                    if (DriverMainPageController.driverMainPageInstance != null) {
                        DriverMainPageController.driverMainPageInstance.showSettingPage();
                    }
                });
            }
        };
        if (rowSetting != null) rowSetting.setOnMouseClicked(e -> openSetting.run());
        if (item4 != null) item4.setOnMouseClicked(e -> openSetting.run());
        if (imgIcon4 != null) imgIcon4.setOnMouseClicked(e -> openSetting.run());

        if (rowLogout != null) rowLogout.setOnMouseClicked(e -> handleLogout());
        if (item5 != null) item5.setOnMouseClicked(e -> handleLogout());
        if (imgIcon5 != null) imgIcon5.setOnMouseClicked(e -> handleLogout());
    }

    private void handleLogout() {
        ServiceFactory.getInstance().getAuthService().logout();
        SceneManager.switchScene("loginPage.fxml");
    }
}
