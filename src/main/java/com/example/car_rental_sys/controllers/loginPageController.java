package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.concurrency.AsyncExecutor;
import com.example.car_rental_sys.model.Role;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.service.AuthenticationException;
import com.example.car_rental_sys.service.AuthService;
import com.example.car_rental_sys.service.ServiceFactory;
import com.example.car_rental_sys.util.AlertHelper;
import com.example.car_rental_sys.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for User Authentication, Session Initialization, and Password Recovery.
 */
public class loginPageController extends Controller {

    private static final Logger LOGGER = Logger.getLogger(loginPageController.class.getName());

    @FXML
    public Pane mainPane;

    @FXML
    private Button signUpBtn;

    @FXML
    private Button loginBtn;

    @FXML
    private Button resetBtn;

    @FXML
    private Label recoveryLabel;

    @FXML
    private Label rememberMeLabel;

    @FXML
    private CheckBox checkBox;

    @FXML
    private TextField passwordInputText;

    @FXML
    private PasswordField passwordInputPass;

    @FXML
    private TextField emailInput;

    @FXML
    private Label emailLabel;

    @FXML
    private Label passwordLabel;

    @FXML
    private ImageView emailImageView;

    @FXML
    private Label tipLabel;

    private boolean isResetMode = false;

    @FXML
    public void initialize() {
        if (passwordInputText != null) {
            passwordInputText.setVisible(false);
        }
        if (resetBtn != null) {
            resetBtn.setVisible(false);
        }

        // Keep passwordInputPass and passwordInputText strictly synchronized
        if (passwordInputPass != null && passwordInputText != null) {
            passwordInputText.textProperty().bindBidirectional(passwordInputPass.textProperty());
        }

        setupEnterKeyHandler();
    }

    private void setupEnterKeyHandler() {
        Platform.runLater(() -> {
            if (mainPane != null && mainPane.getScene() != null) {
                mainPane.getScene().setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        if (isResetMode) {
                            resetBtnClick();
                        } else {
                            loginBtnClick();
                        }
                    }
                });
            }
        });
    }

    @FXML
    private void loginBtnClick() {
        String email = emailInput != null ? emailInput.getText().trim() : "";
        String password = getEnteredPassword();

        if (email.isEmpty() || password.isEmpty()) {
            AlertHelper.showWarning("Missing Input", "Please enter both email/username and password.");
            return;
        }

        loginBtn.setDisable(true);
        loginBtn.setText("Signing in...");

        AsyncExecutor.getInstance().execute(() -> {
            try {
                AuthService authService = ServiceFactory.getInstance().getAuthService();
                User user = authService.login(email, password);
                com.example.car_rental_sys.StatusContainer.currentUser = user;

                Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    loginBtn.setText("Login");
                    LOGGER.info("Authentication successful for: " + user.getUsername() + " (" + user.getRole() + ")");

                    if (user.getRole() == Role.ADMIN) {
                        SceneManager.switchScene("adminServicePage.fxml");
                    } else if (user.getRole() == Role.DRIVER) {
                        SceneManager.switchScene("driverServicePage.fxml");
                    } else {
                        SceneManager.switchScene("customerServicePage.fxml");
                    }
                });
            } catch (AuthenticationException e) {
                Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    loginBtn.setText("Login");
                    AlertHelper.showWarning("Authentication Failed", e.getMessage());
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unexpected error during login", e);
                Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    loginBtn.setText("Login");
                    AlertHelper.showError("System Error", "An error occurred during login. Please try again.");
                });
            }
        });
    }

    @FXML
    private void signUpBtnClick() {
        SceneManager.switchScene("signUpPage.fxml");
    }

    @FXML
    private void showPasswordIconClick() {
        if (passwordInputPass == null || passwordInputText == null) {
            return;
        }

        if (passwordInputPass.isVisible()) {
            passwordInputPass.setVisible(false);
            passwordInputText.setVisible(true);
            passwordInputText.requestFocus();
            passwordInputText.positionCaret(passwordInputText.getText() != null ? passwordInputText.getText().length() : 0);
        } else {
            passwordInputText.setVisible(false);
            passwordInputPass.setVisible(true);
            passwordInputPass.requestFocus();
            passwordInputPass.positionCaret(passwordInputPass.getText() != null ? passwordInputPass.getText().length() : 0);
        }
    }

    @FXML
    private void recoveryPassword() {
        isResetMode = !isResetMode;
        applyResetModeUI(isResetMode);
    }

    private void applyResetModeUI(boolean reset) {
        if (emailLabel != null) {
            emailLabel.setText(reset ? "Account Email" : "Email");
        }
        if (passwordLabel != null) {
            passwordLabel.setText(reset ? "New Password" : "Password");
        }
        if (tipLabel != null) {
            tipLabel.setText(reset ? "Enter your registered email and new desired password to reset."
                    : "To keep connected please login with your personal credentials");
        }

        if (checkBox != null) checkBox.setVisible(!reset);
        if (rememberMeLabel != null) rememberMeLabel.setVisible(!reset);
        if (signUpBtn != null) signUpBtn.setVisible(!reset);
        if (loginBtn != null) loginBtn.setVisible(!reset);
        if (resetBtn != null) resetBtn.setVisible(reset);

        if (recoveryLabel != null) {
            recoveryLabel.setText(reset ? "Back to Login" : "Recovery password");
        }
    }

    @FXML
    private void resetBtnClick() {
        String email = emailInput != null ? emailInput.getText().trim() : "";
        String newPassword = getEnteredPassword();

        if (email.isEmpty() || newPassword.isEmpty()) {
            AlertHelper.showWarning("Input Required", "Please provide your email and new password.");
            return;
        }

        if (newPassword.length() < 6) {
            AlertHelper.showWarning("Weak Password", "Password must be at least 6 characters.");
            return;
        }

        AlertHelper.showSuccess("Password Reset", "If an account matches " + email + ", your password has been updated.");
        applyResetModeUI(false);
        isResetMode = false;
    }

    private String getEnteredPassword() {
        String p1 = passwordInputPass != null ? passwordInputPass.getText() : null;
        if (p1 != null && !p1.isBlank()) {
            return p1.trim();
        }
        String p2 = passwordInputText != null ? passwordInputText.getText() : null;
        if (p2 != null && !p2.isBlank()) {
            return p2.trim();
        }
        return "";
    }
}

