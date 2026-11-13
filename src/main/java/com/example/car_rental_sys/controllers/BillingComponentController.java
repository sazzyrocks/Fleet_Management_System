package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.StatusContainer;
import com.example.car_rental_sys.concurrency.AsyncExecutor;
import com.example.car_rental_sys.dao.DAOFactory;
import com.example.car_rental_sys.model.BankCard;
import com.example.car_rental_sys.model.Order;
import com.example.car_rental_sys.model.Payment;
import com.example.car_rental_sys.model.PaymentStatus;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.service.ServiceFactory;
import com.example.car_rental_sys.ui_components.PaymentCard;
import com.example.car_rental_sys.ui_components.UICusBillRow;
import com.example.car_rental_sys.ui_components.UIPagination;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for Customer Billing Overview, Wallet, and Payment Transactions.
 * Restored with authentic PaymentCard visuals and UICusBillRow transaction components.
 */
public class BillingComponentController extends Controller {

    public static BillingComponentController instance;

    @FXML
    private ScrollPane billCardList;

    @FXML
    private Pane billCardDetailBox;

    @FXML
    private Button billAllBtn;

    @FXML
    private Button billCompleteBtn;

    @FXML
    private Button billPendingBtn;

    @FXML
    private Button billCancelBtn;

    @FXML
    private Pane cusBillTable;

    @FXML
    private Pane pagContainer;

    private List<Payment> allPayments = new ArrayList<>();
    private List<Payment> displayedPayments = new ArrayList<>();
    private VBox billTableBox;

    @FXML
    public void initialize() {
        instance = this;
        setupCards();
        setupTable();
        loadBillingData();
    }

    public void setupCards() {
        User user = ServiceFactory.getInstance().getAuthService().getCurrentUser();
        if (user == null) user = StatusContainer.currentUser;
        if (user == null) return;

        final User currentUser = user;
        AsyncExecutor.getInstance().execute(() -> {
            try {
                ServiceFactory.getInstance().getPaymentService().ensureDefaultCardForUser(currentUser);
                List<BankCard> cards = DAOFactory.getInstance().getBankCardDAO().findByUserId(currentUser.getUserId());

                Platform.runLater(() -> {
                    FlowPane cardPane = new FlowPane();
                    cardPane.setHgap(20);
                    cardPane.setVgap(15);
                    cardPane.setPadding(new Insets(10, 15, 10, 15));
                    cardPane.setStyle("-fx-background-color: transparent;");

                    String name = currentUser.getFullName() != null && !currentUser.getFullName().isBlank() 
                            ? currentUser.getFullName() : currentUser.getUsername();

                    for (BankCard card : cards) {
                        String type = card.getCardType() != null ? card.getCardType().toLowerCase() : "mastercard";
                        String balance = card.getBalance() != null ? card.getBalance().toPlainString() : "50000";
                        String bank = "HDFC Bank";
                        if (type.contains("visa")) bank = "State Bank of India";
                        else if (type.contains("paypal")) bank = "PayPal Digital";
                        else if (type.contains("tng") || type.contains("touch")) bank = "Touch 'n Go eWallet";

                        PaymentCard pc = new PaymentCard(type, card.getCardNumber(), name, bank, balance);
                        cardPane.getChildren().add(pc);
                    }

                    PaymentCard addNew = new PaymentCard("addNewCard");
                    cardPane.getChildren().add(addNew);

                    if (billCardList != null) {
                        billCardList.setContent(cardPane);
                        billCardList.setFitToHeight(true);
                        billCardList.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
                    }

                    if (showCardDetailsController.instance != null) {
                        showCardDetailsController.instance.loadCardDetails();
                    }
                });
            } catch (Exception ignored) {
            }
        });
    }

    private void setupTable() {
        billTableBox = new VBox(6);
        billTableBox.setPadding(new Insets(6, 10, 6, 10));
        billTableBox.setStyle("-fx-background-color: transparent;");

        if (cusBillTable != null) {
            cusBillTable.getChildren().setAll(billTableBox);
        }

        if (pagContainer != null) {
            pagContainer.getChildren().setAll(new UIPagination(10, 5));
        }
    }

    private void loadBillingData() {
        AsyncExecutor.getInstance().execute(() -> {
            try {
                User user = ServiceFactory.getInstance().getAuthService().getCurrentUser();
                if (user == null) user = StatusContainer.currentUser;
                final int customerId = (user != null) ? user.getUserId() : 1;

                List<Order> customerOrders = DAOFactory.getInstance().getOrderDAO().findByCustomerId(customerId);
                Set<Integer> customerOrderIds = customerOrders.stream()
                        .map(Order::getOrderId)
                        .collect(Collectors.toSet());

                List<Payment> payments = DAOFactory.getInstance().getPaymentDAO().findAll().stream()
                        .filter(p -> p.getOrderId() != null && customerOrderIds.contains(p.getOrderId()))
                        .toList();

                Platform.runLater(() -> {
                    allPayments = payments;
                    displayedPayments = new ArrayList<>(payments);
                    renderBillRows();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    allPayments = new ArrayList<>();
                    displayedPayments = new ArrayList<>();
                    renderBillRows();
                });
            }
        });
    }

    private void renderBillRows() {
        if (billTableBox == null) return;
        billTableBox.getChildren().clear();

        if (displayedPayments.isEmpty()) {
            Label emptyLbl = new Label("No recent transaction records found.");
            emptyLbl.setStyle("-fx-text-fill: #86909c; -fx-font-size: 13px; -fx-padding: 20 0 0 10;");
            billTableBox.getChildren().add(emptyLbl);
            return;
        }

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

        for (Payment p : displayedPayments) {
            String dateStr = p.getPaidAt() != null ? p.getPaidAt().format(dateFmt) : "2026-11-01";
            String timeStr = p.getPaidAt() != null ? p.getPaidAt().format(timeFmt) : "10:30:00";
            int amount = p.getAmount() != null ? p.getAmount().intValue() : 250;
            int status = (p.getPaymentStatus() == PaymentStatus.SUCCESS) ? 5 : ((p.getPaymentStatus() == PaymentStatus.FAILED) ? -1 : 0);

            UICusBillRow row = new UICusBillRow("rental", dateStr, timeStr, amount, status);
            billTableBox.getChildren().add(row);
        }
    }

    @FXML
    private void billAllBtnClicked(MouseEvent event) {
        setFocusBtn(billAllBtn);
        displayedPayments = new ArrayList<>(allPayments);
        renderBillRows();
    }

    @FXML
    private void billCompleteBtnClicked(MouseEvent event) {
        setFocusBtn(billCompleteBtn);
        displayedPayments = allPayments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                .toList();
        renderBillRows();
    }

    @FXML
    private void billPendingBtnClicked(MouseEvent event) {
        setFocusBtn(billPendingBtn);
        displayedPayments = allPayments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PENDING)
                .toList();
        renderBillRows();
    }

    @FXML
    private void billCancelBtnClicked(MouseEvent event) {
        setFocusBtn(billCancelBtn);
        displayedPayments = allPayments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.FAILED || p.getPaymentStatus() == PaymentStatus.REFUNDED)
                .toList();
        renderBillRows();
    }

    private void setFocusBtn(Button target) {
        Button[] btns = {billAllBtn, billCompleteBtn, billPendingBtn, billCancelBtn};
        for (Button b : btns) {
            if (b != null) {
                b.getStyleClass().remove("focusCusBillButton");
                if (!b.getStyleClass().contains("cusBillButton")) {
                    b.getStyleClass().add("cusBillButton");
                }
            }
        }
        if (target != null) {
            target.getStyleClass().add("focusCusBillButton");
            target.getStyleClass().remove("cusBillButton");
        }
    }

    public void refreshTable(int page) {
        renderBillRows();
    }
}
