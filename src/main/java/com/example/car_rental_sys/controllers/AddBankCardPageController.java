package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.concurrency.AsyncExecutor;
import com.example.car_rental_sys.dao.BankCardDAO;
import com.example.car_rental_sys.dao.DAOFactory;
import com.example.car_rental_sys.model.BankCard;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.service.ServiceFactory;
import com.example.car_rental_sys.util.AlertHelper;
import com.example.car_rental_sys.util.SceneManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.math.BigDecimal;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for adding new payment cards to the customer's digital wallet.
 */
public class AddBankCardPageController extends Controller {

    private static final Logger LOGGER = Logger.getLogger(AddBankCardPageController.class.getName());

    @FXML public Pane mainPane;

    @FXML private ImageView cardImage1, cardImage2, cardImage3, cardImageMain, backBtn;
    @FXML private ScrollPane testScrollPane;

    @FXML private RadioButton mastercardRadio, visaRadio, tngRadio, paypalRadio;
    @FXML private Pane typeMastercard, visaType, tngType, paypalType;

    @FXML private TextField txtCardName;
    @FXML private ComboBox<String> banksComboBox;
    @FXML private TextField txtCardHolder;
    @FXML private TextField txtCardNumber;
    @FXML private TextField txtValidMonth;
    @FXML private TextField txtValidDate;
    @FXML private TextArea billTextArea;
    @FXML private CheckBox sendMeCheckBox;
    @FXML private Button addCardButton;

    @FXML
    public void initialize() {
        populateBanks();
        selectRadio(mastercardRadio);
        prefillUserData();
        attachEvents();
    }

    private void prefillUserData() {
        User user = ServiceFactory.getInstance().getAuthService().getCurrentUser();
        if (user == null) {
            user = com.example.car_rental_sys.StatusContainer.currentUser;
        }
        if (user != null) {
            if (txtCardHolder != null) {
                String name = user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getUsername();
                txtCardHolder.setText(name);
            }
            if (billTextArea != null && user.getAddress() != null && !user.getAddress().isBlank()) {
                billTextArea.setText(user.getAddress());
            } else if (billTextArea != null) {
                billTextArea.setText("SRM University-AP, Amaravati, Andhra Pradesh 522502");
            }
        }
    }

    private void populateBanks() {
        if (banksComboBox != null) {
            banksComboBox.getItems().setAll(
                    "HDFC Bank",
                    "State Bank of India (SBI)",
                    "ICICI Bank",
                    "Axis Bank",
                    "Kotak Mahindra Bank",
                    "Citibank N.A.",
                    "JPMorgan Chase"
            );
            banksComboBox.getSelectionModel().selectFirst();
        }
    }

    private void attachEvents() {
        if (backBtn != null) {
            backBtn.setOnMouseClicked(event -> handleCancel());
        }
    }

    @FXML
    public void cardRadioClick(MouseEvent event) {
        Object source = event.getSource();
        if (source == mastercardRadio || source == typeMastercard) {
            selectRadio(mastercardRadio);
            updateCardPreview("mastercard");
        } else if (source == visaRadio || source == visaType) {
            selectRadio(visaRadio);
            updateCardPreview("visa");
        } else if (source == paypalRadio || source == paypalType) {
            selectRadio(paypalRadio);
            updateCardPreview("paypal");
        } else if (source == tngRadio || source == tngType) {
            selectRadio(tngRadio);
            updateCardPreview("tng");
        }
    }

    private void updateCardPreview(String type) {
        try {
            String imgPath = switch (type.toLowerCase()) {
                case "visa" -> "/com/example/car_rental_sys/image/UI/visaWhite.png";
                case "paypal" -> "/com/example/car_rental_sys/image/UI/paypal.png";
                case "tng" -> "/com/example/car_rental_sys/image/UI/tng.png";
                default -> "/com/example/car_rental_sys/image/UI/MasterCardBlack.png";
            };
            URL url = getClass().getResource(imgPath);
            if (url != null && cardImageMain != null) {
                cardImageMain.setImage(new Image(url.toExternalForm()));
            }
        } catch (Exception ignored) {}
    }

    private void selectRadio(RadioButton selected) {
        RadioButton[] radios = {mastercardRadio, visaRadio, tngRadio, paypalRadio};
        for (RadioButton r : radios) {
            if (r != null) r.setSelected(r == selected);
        }
    }

    @FXML
    public void handleCancel() {
        Platform.runLater(() -> {
            SceneManager.switchScene("customerServicePage.fxml");
            Platform.runLater(() -> {
                if (CustomerServiceController.instance != null) {
                    CustomerServiceController.instance.showWalletPage();
                }
            });
        });
    }

    @FXML
    public void handleAddCardAction(ActionEvent event) {
        handleAddCard();
    }

    public void handleAddCard() {
        User user = ServiceFactory.getInstance().getAuthService().getCurrentUser();
        if (user == null) {
            user = com.example.car_rental_sys.StatusContainer.currentUser;
        }

        if (user == null) {
            AlertHelper.showWarning("Login Required", "Please log in to manage your cards.");
            SceneManager.switchScene("loginPage.fxml");
            return;
        }

        String rawNum = txtCardNumber != null ? txtCardNumber.getText().replaceAll("\\s+", "").trim() : "";
        String holder = txtCardHolder != null ? txtCardHolder.getText().trim() : "";

        if (rawNum.isEmpty() || holder.isEmpty()) {
            AlertHelper.showWarning("Incomplete Form", "Please enter the cardholder name and 16-digit card number.");
            return;
        }

        if (rawNum.length() < 12) {
            AlertHelper.showWarning("Invalid Card Number", "Card number must contain at least 12 digits.");
            return;
        }

        String cardType = "Mastercard";
        if (visaRadio != null && visaRadio.isSelected()) cardType = "Visa";
        else if (paypalRadio != null && paypalRadio.isSelected()) cardType = "PayPal";
        else if (tngRadio != null && tngRadio.isSelected()) cardType = "TouchNGo";

        String month = txtValidMonth != null && !txtValidMonth.getText().isBlank() ? txtValidMonth.getText().trim() : "09";
        String year = txtValidDate != null && !txtValidDate.getText().isBlank() ? txtValidDate.getText().trim() : "28";

        BankCard card = new BankCard();
        card.setUserId(user.getUserId());
        card.setCardHolder(holder);
        card.setCardNumber(rawNum);
        card.setCardType(cardType);
        card.setExpiryDate(month + "/" + year);
        card.setCvv("123");
        card.setBalance(new BigDecimal("50000.00"));
        boolean setAsDefault = sendMeCheckBox != null && sendMeCheckBox.isSelected();
        card.setIsDefault(setAsDefault);

        if (addCardButton != null) {
            addCardButton.setDisable(true);
            addCardButton.setText("Adding...");
        }

        final int currentUserId = user.getUserId();
        AsyncExecutor.getInstance().execute(() -> {
            try {
                BankCardDAO cardDAO = DAOFactory.getInstance().getBankCardDAO();
                cardDAO.save(card);
                if (setAsDefault && card.getCardId() > 0) {
                    cardDAO.setDefaultCard(currentUserId, card.getCardId());
                }

                Platform.runLater(() -> {
                    if (addCardButton != null) {
                        addCardButton.setDisable(false);
                        addCardButton.setText("Save & Link Card");
                    }
                    AlertHelper.showSuccess("Card Linked", "Card " + card.getCardType() + " ending in " +
                            rawNum.substring(Math.max(0, rawNum.length() - 4)) + " added to your digital wallet.");
                    handleCancel();
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed saving bank card: " + e.getMessage(), e);
                Platform.runLater(() -> {
                    if (addCardButton != null) {
                        addCardButton.setDisable(false);
                        addCardButton.setText("Save & Link Card");
                    }
                    AlertHelper.showError("Card Error", "Failed to add card: " + e.getMessage());
                });
            }
        });
    }
}
