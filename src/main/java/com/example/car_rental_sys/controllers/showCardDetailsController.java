package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.StatusContainer;
import com.example.car_rental_sys.concurrency.AsyncExecutor;
import com.example.car_rental_sys.dao.BankCardDAO;
import com.example.car_rental_sys.dao.DAOFactory;
import com.example.car_rental_sys.model.BankCard;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.service.ServiceFactory;
import com.example.car_rental_sys.util.AlertHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.List;

/**
 * Controller for displaying and managing a stored Credit/Debit Bank Card.
 */
public class showCardDetailsController extends Controller {

    public static showCardDetailsController instance;

    @FXML
    private Label cardName;

    @FXML
    private Label cardValidDate;

    @FXML
    private Label cardNumber;

    @FXML
    private Label cardBankName;

    @FXML
    private Label cardHolderName;

    @FXML
    private Label cardBillingAddress;

    @FXML
    private Label lblTheme;

    @FXML
    private Pane themeWhite, themeRed, themeBlue, themeGreen, themeOrange;

    @FXML
    private TextArea txtBillingAddress;

    @FXML
    private TextField txtCardName;

    @FXML
    private Button cardEditBtn;

    @FXML
    private Button cardSaveBtn;

    @FXML
    private Button cardDeleteBtn;

    private BankCard activeCard;

    @FXML
    public void initialize() {
        instance = this;
        loadCardDetails();
    }

    public void initText() {
        loadCardDetails();
    }

    public void loadCardDetails() {
        User user = ServiceFactory.getInstance().getAuthService().getCurrentUser();
        if (user == null) user = StatusContainer.currentUser;
        if (user == null) return;

        final User currentUser = user;
        AsyncExecutor.getInstance().execute(() -> {
            try {
                ServiceFactory.getInstance().getPaymentService().ensureDefaultCardForUser(currentUser);
                BankCardDAO cardDAO = DAOFactory.getInstance().getBankCardDAO();
                List<BankCard> cards = cardDAO.findByUserId(currentUser.getUserId());
                if (!cards.isEmpty()) {
                    String selectedNum = StatusContainer.currentChooseBankCardNum;
                    BankCard chosen = null;
                    if (selectedNum != null) {
                        for (BankCard c : cards) {
                            if (selectedNum.equals(c.getCardNumber()) || (c.getCardNumber() != null && c.getCardNumber().endsWith(selectedNum))) {
                                chosen = c;
                                break;
                            }
                        }
                    }
                    if (chosen == null) {
                        chosen = cards.stream()
                                .filter(c -> Boolean.TRUE.equals(c.getIsDefault()))
                                .findFirst()
                                .orElse(cards.get(0));
                    }
                    activeCard = chosen;
                    Platform.runLater(this::renderActiveCard);
                } else {
                    activeCard = null;
                    Platform.runLater(this::renderActiveCard);
                }
            } catch (Exception ignored) {
            }
        });
    }

    private void renderActiveCard() {
        User user = ServiceFactory.getInstance().getAuthService().getCurrentUser();
        if (user == null) user = StatusContainer.currentUser;

        if (activeCard == null) {
            if (cardName != null) cardName.setText("No Card");
            if (cardBankName != null) cardBankName.setText("No Bank");
            if (cardHolderName != null) cardHolderName.setText(user != null && user.getFullName() != null ? user.getFullName() : "-");
            if (cardNumber != null) cardNumber.setText("No card linked");
            if (cardValidDate != null) cardValidDate.setText("--/--");
            if (cardBillingAddress != null) {
                cardBillingAddress.setText(user != null && user.getAddress() != null && !user.getAddress().isBlank() 
                        ? user.getAddress() : "SRM University-AP, Amaravati, Andhra Pradesh 522502");
            }
            return;
        }

        String type = activeCard.getCardType() != null ? activeCard.getCardType() : "Mastercard";
        if (cardName != null) cardName.setText(type);
        if (cardBankName != null) {
            if (type.equalsIgnoreCase("visa")) cardBankName.setText("State Bank of India");
            else if (type.equalsIgnoreCase("paypal")) cardBankName.setText("PayPal Digital");
            else if (type.equalsIgnoreCase("tng")) cardBankName.setText("Touch 'n Go");
            else cardBankName.setText("HDFC Bank");
        }
        if (cardHolderName != null) {
            String holder = activeCard.getCardHolder();
            if (holder == null || holder.isBlank() || holder.equalsIgnoreCase("Wong Chun Wei") || holder.equalsIgnoreCase("Alex Johnson")) {
                if (user != null && user.getFullName() != null && !user.getFullName().isBlank()) {
                    holder = user.getFullName();
                } else if (user != null) {
                    holder = user.getUsername();
                } else {
                    holder = "Fleet Client";
                }
            }
            cardHolderName.setText(holder);
        }

        if (cardNumber != null && activeCard.getCardNumber() != null) {
            String num = activeCard.getCardNumber();
            String masked = num.length() > 4 ? "**** **** **** " + num.substring(num.length() - 4) : "**** " + num;
            cardNumber.setText(masked);
        }

        if (cardValidDate != null) {
            cardValidDate.setText(activeCard.getExpiryDate() != null ? activeCard.getExpiryDate() : "09/28");
        }

        if (cardBillingAddress != null) {
            if (user != null && user.getAddress() != null && !user.getAddress().isBlank()) {
                cardBillingAddress.setText(user.getAddress());
            } else {
                cardBillingAddress.setText("SRM University-AP, Amaravati, Andhra Pradesh 522502");
            }
        }
    }

    @FXML
    private void cardEditClicked(MouseEvent event) {
        toggleEditUI(true);
    }

    @FXML
    private void cardSaveClicked(MouseEvent event) {
        if (activeCard != null && txtCardName != null && !txtCardName.getText().isBlank()) {
            activeCard.setCardType(txtCardName.getText().trim());
            AsyncExecutor.getInstance().execute(() -> {
                try {
                    DAOFactory.getInstance().getBankCardDAO().update(activeCard);
                    Platform.runLater(() -> {
                        renderActiveCard();
                        toggleEditUI(false);
                        AlertHelper.showSuccess("Card Updated", "Card details updated.");
                        if (BillingComponentController.instance != null) {
                            BillingComponentController.instance.setupCards();
                        }
                    });
                } catch (Exception ignored) {
                }
            });
        } else {
            toggleEditUI(false);
        }
    }

    @FXML
    private void cardDeleteClicked(MouseEvent event) {
        if (activeCard == null) return;
        boolean confirm = AlertHelper.showConfirmation("Remove Card", "Are you sure you want to remove this card from your wallet?");
        if (!confirm) return;

        final int cardId = activeCard.getCardId();
        AsyncExecutor.getInstance().execute(() -> {
            try {
                DAOFactory.getInstance().getBankCardDAO().deleteById(cardId);
                StatusContainer.currentChooseBankCardNum = null;
                Platform.runLater(() -> {
                    activeCard = null;
                    if (cardNumber != null) cardNumber.setText("No card linked");
                    toggleEditUI(false);
                    AlertHelper.showSuccess("Card Removed", "Card removed from wallet.");
                    if (BillingComponentController.instance != null) {
                        BillingComponentController.instance.setupCards();
                    } else {
                        loadCardDetails();
                    }
                });
            } catch (Exception ignored) {
            }
        });
    }

    private void toggleEditUI(boolean editing) {
        if (txtCardName != null) {
            txtCardName.setVisible(editing);
            if (editing && cardName != null) txtCardName.setText(cardName.getText());
        }
        if (txtBillingAddress != null) txtBillingAddress.setVisible(editing);
        if (cardName != null) cardName.setVisible(!editing);
        if (cardBillingAddress != null) cardBillingAddress.setVisible(!editing);

        if (cardSaveBtn != null) cardSaveBtn.setVisible(editing);
        if (cardDeleteBtn != null) cardDeleteBtn.setVisible(editing);
        if (cardEditBtn != null) cardEditBtn.setVisible(!editing);
    }

    @FXML
    private void themeWhiteClicked(MouseEvent event) {}

    @FXML
    private void themeRedClicked(MouseEvent event) {}

    @FXML
    private void themeBlueClicked(MouseEvent event) {}

    @FXML
    private void themeGreenClicked(MouseEvent event) {}

    @FXML
    private void themeOrangeClicked(MouseEvent event) {}
}

