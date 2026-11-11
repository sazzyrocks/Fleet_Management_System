package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.concurrency.AsyncExecutor;
import com.example.car_rental_sys.dao.DAOFactory;
import com.example.car_rental_sys.dao.UserDAO;
import com.example.car_rental_sys.model.Role;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.service.ServiceFactory;
import com.example.car_rental_sys.util.AlertHelper;
import com.example.car_rental_sys.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Controller for User Profile Management and Personal Information.
 */
public class PersonalPageController extends Controller {

    @FXML
    private Pane panelSetting;

    @FXML
    private ImageView ivAvatar;

    @FXML
    private Button ivDeleteBtn;

    @FXML
    private Button ivUploadBtn;

    @FXML
    private TextField ivTxtUsername;

    @FXML
    private TextField ivTxtBirthday;

    @FXML
    private TextArea ivTxtAbout;

    @FXML
    private TextField ivTxtPhoneNumber;

    @FXML
    private TextField ivTxtEmail;

    @FXML
    private TextArea ivTxtAddress;

    @FXML
    private Button ivSaveBtn;

    @FXML
    private Button ivCancelBtn;

    @FXML
    private Button btnBackToMain;

    @FXML
    private Button backBtn;

    @FXML
    private Label aboutWordCount;

    @FXML
    private Label nameWordCount;

    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = ServiceFactory.getInstance().getAuthService().getCurrentUser();
        loadUserData();
        setupCharacterCounters();
        initBackBtn();
    }

    private void initBackBtn() {
        if (btnBackToMain != null) {
            User user = currentUser != null ? currentUser : ServiceFactory.getInstance().getAuthService().getCurrentUser();
            if (user != null && user.getRole() == Role.DRIVER) {
                btnBackToMain.setText("← Back to Workplace");
            } else if (user != null && user.getRole() == Role.CUSTOMER) {
                btnBackToMain.setText("← Back to Orders");
            } else if (user != null && user.getRole() == Role.ADMIN) {
                btnBackToMain.setText("← Back to Overview");
            } else {
                btnBackToMain.setText("← Back");
            }

            btnBackToMain.setOnAction(e -> navigateBack());
            btnBackToMain.setOnMouseClicked(e -> navigateBack());
        }
    }

    private void loadUserData() {
        if (currentUser != null) {
            if (ivTxtUsername != null) ivTxtUsername.setText(currentUser.getUsername());
            if (ivTxtEmail != null) ivTxtEmail.setText(currentUser.getEmail());
            if (ivTxtPhoneNumber != null && currentUser.getPhone() != null) {
                ivTxtPhoneNumber.setText(currentUser.getPhone());
            }
            if (ivTxtBirthday != null) ivTxtBirthday.setText("1996-05-18");
            if (ivTxtAbout != null) ivTxtAbout.setText("Fleet client account.");
            if (ivTxtAddress != null) ivTxtAddress.setText("SRM University-AP, Amaravati, Andhra Pradesh");

            loadDefaultAvatar();
        }
    }

    private void setupCharacterCounters() {
        if (ivTxtAbout != null && aboutWordCount != null) {
            ivTxtAbout.textProperty().addListener((obs, oldV, newV) -> {
                aboutWordCount.setText(String.valueOf(newV != null ? newV.length() : 0));
            });
        }
        if (ivTxtUsername != null && nameWordCount != null) {
            ivTxtUsername.textProperty().addListener((obs, oldV, newV) -> {
                nameWordCount.setText(String.valueOf(newV != null ? newV.length() : 0));
            });
        }
    }

    @FXML
    private void saveChangesClicked(MouseEvent event) {
        if (currentUser == null) return;

        String newUsername = ivTxtUsername != null ? ivTxtUsername.getText().trim() : currentUser.getUsername();
        String newEmail = ivTxtEmail != null ? ivTxtEmail.getText().trim() : currentUser.getEmail();
        String newPhone = ivTxtPhoneNumber != null ? ivTxtPhoneNumber.getText().trim() : "";

        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            AlertHelper.showWarning("Required Fields", "Username and email cannot be empty.");
            return;
        }

        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);
        currentUser.setPhone(newPhone);

        ivSaveBtn.setDisable(true);
        AsyncExecutor.getInstance().execute(() -> {
            try {
                UserDAO userDAO = DAOFactory.getInstance().getUserDAO();
                userDAO.update(currentUser);
                Platform.runLater(() -> {
                    ivSaveBtn.setDisable(false);
                    AlertHelper.showSuccess("Profile Updated", "Your profile details have been saved.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    ivSaveBtn.setDisable(false);
                    AlertHelper.showError("Update Failed", e.getMessage());
                });
            }
        });
    }

    @FXML
    private void backToDashboardClicked(MouseEvent event) {
        navigateBack();
    }

    @FXML
    private void cancelClicked(MouseEvent event) {
        navigateBack();
    }

    private void navigateBack() {
        User user = currentUser != null ? currentUser : ServiceFactory.getInstance().getAuthService().getCurrentUser();
        Role role = user != null ? user.getRole() : null;

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

        if (CustomerServiceController.instance != null) {
            CustomerServiceController.instance.showOrderPage();
        } else {
            SceneManager.switchScene("customerServicePage.fxml");
        }
    }

    @FXML
    private void uploadClicked(MouseEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Avatar Picture");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(null);
        if (file != null && ivAvatar != null) {
            try {
                ivAvatar.setImage(new Image(new FileInputStream(file)));
            } catch (Exception ignored) {
            }
        }
    }

    @FXML
    private void deleteClicked(MouseEvent event) {
        loadDefaultAvatar();
    }

    private void loadDefaultAvatar() {
        if (ivAvatar == null) return;
        try (InputStream stream = getClass().getResourceAsStream("/com/example/car_rental_sys/image/avatar/1.png")) {
            if (stream != null) ivAvatar.setImage(new Image(stream));
        } catch (Exception ignored) {
        }
    }
}

