package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.concurrency.AsyncExecutor;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.service.AuthenticationException;
import com.example.car_rental_sys.service.AuthService;
import com.example.car_rental_sys.service.ServiceFactory;
import com.example.car_rental_sys.util.AlertHelper;
import com.example.car_rental_sys.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Customer Self-Registration.
 */
public class SignUpPageController extends Controller {

    private static final Logger LOGGER = Logger.getLogger(SignUpPageController.class.getName());

    @FXML
    public Pane mainPane;

    @FXML
    private Pane textFieldContainer;

    @FXML
    private Button createAccountBtn;

    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;

    @FXML
    private TextField emailInput;

    @FXML
    private TextField phoneInput;

    @FXML
    private TextField licenseInput;

    @FXML
    private PasswordField password;

    @FXML
    public void initialize() {
        // Initialization ready
    }

    @FXML
    private void loginClicked(MouseEvent event) {
        SceneManager.switchScene("loginPage.fxml");
    }

    @FXML
    private void btnCreateClicked(MouseEvent event) {
        String fName = firstName != null ? firstName.getText().trim() : "";
        String lName = lastName != null ? lastName.getText().trim() : "";
        String email = emailInput != null ? emailInput.getText().trim() : "";
        String phone = phoneInput != null ? phoneInput.getText().trim() : "";
        String license = licenseInput != null ? licenseInput.getText().trim() : "";
        String pass = password != null ? password.getText() : "";

        if (fName.isEmpty() || lName.isEmpty()) {
            AlertHelper.showWarning("Missing Name", "Please enter your first and last name.");
            return;
        }

        if (email.isEmpty() || !email.contains("@")) {
            AlertHelper.showWarning("Invalid Email", "Please enter a valid email address.");
            return;
        }

        if (phone.isEmpty()) {
            AlertHelper.showWarning("Missing Phone", "Please enter your contact phone number.");
            return;
        }

        if (license.isEmpty()) {
            AlertHelper.showWarning("Missing Driving License", "Please enter your driving license number.");
            return;
        }

        if (pass.length() < 6) {
            AlertHelper.showWarning("Password Too Short", "Password must be at least 6 characters.");
            return;
        }

        String fullName = fName + " " + lName;
        // Generate a username based on email local-part or clean name
        String suggestedUsername = email.substring(0, email.indexOf('@')).replaceAll("[^a-zA-Z0-9_]", "");
        if (suggestedUsername.length() < 3) {
            suggestedUsername = fName.toLowerCase() + "_" + System.currentTimeMillis() % 1000;
        }

        final String finalUsername = suggestedUsername;
        createAccountBtn.setDisable(true);
        createAccountBtn.setText("Registering...");

        AsyncExecutor.getInstance().execute(() -> {
            try {
                AuthService authService = ServiceFactory.getInstance().getAuthService();
                User registeredUser = authService.register(
                        finalUsername,
                        pass,
                        email,
                        fullName,
                        phone,
                        license
                );

                Platform.runLater(() -> {
                    createAccountBtn.setDisable(false);
                    createAccountBtn.setText("Next");
                    LOGGER.info("Registered user: " + registeredUser.getUsername() + " (ID: " + registeredUser.getUserId() + ")");
                    AlertHelper.showSuccess("Registration Successful", "Account created successfully! You can now log in.");
                    SceneManager.switchScene("loginPage.fxml");
                });
            } catch (AuthenticationException e) {
                Platform.runLater(() -> {
                    createAccountBtn.setDisable(false);
                    createAccountBtn.setText("Next");
                    AlertHelper.showWarning("Registration Failed", e.getMessage());
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unexpected error during registration", e);
                Platform.runLater(() -> {
                    createAccountBtn.setDisable(false);
                    createAccountBtn.setText("Next");
                    AlertHelper.showError("Registration Error", "An unexpected error occurred. Please try again.");
                });
            }
        });
    }
}

