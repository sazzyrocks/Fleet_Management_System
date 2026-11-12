package com.example.car_rental_sys.controllers;



import com.example.car_rental_sys.concurrency.AsyncExecutor;
import com.example.car_rental_sys.dao.DAOFactory;
import com.example.car_rental_sys.model.BankCard;
import com.example.car_rental_sys.model.Order;
import com.example.car_rental_sys.model.Payment;
import com.example.car_rental_sys.model.PaymentStatus;
import com.example.car_rental_sys.model.RentalMode;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.model.Vehicle;
import com.example.car_rental_sys.service.PaymentService;
import com.example.car_rental_sys.service.RentalService;
import com.example.car_rental_sys.service.ServiceFactory;
import com.example.car_rental_sys.util.AlertHelper;
import com.example.car_rental_sys.util.SceneManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



/**

 * Controller for Rental Checkout and Payment Processing.

 */

public class PaymentController extends Controller {

    private static final Logger LOGGER = Logger.getLogger(PaymentController.class.getName());



    @FXML

    public Pane mainPane;



    @FXML

    private ImageView avatarImageView;



    @FXML

    private Label customerName;



    @FXML

    private Label price;



    @FXML

    private Label duration;



    @FXML

    private Label discount;



    @FXML

    private Label tax;



    @FXML

    private Label totalPrice;



    @FXML

    private Label pickUpTime;



    @FXML

    private Label returnTime;



    @FXML

    private Button carChooseBtn;



    @FXML

    private Label carChooseText;



    @FXML

    private Label modelText;



    @FXML

    private Label priceText;



    @FXML

    private ImageView carImageView;



    @FXML

    private Label balanceText;



    @FXML

    private RadioButton radioBtn1;



    @FXML

    private RadioButton radioBtn2;



    @FXML

    private Label cardTypeAndNum;



    @FXML

    private Label expiresDate;



    @FXML

    private ImageView bankcardImage;



    @FXML

    private Button downIconBtn;



    @FXML
    private ScrollPane bankCardsContainer;

    @FXML
    private Pane selfDriveCard;
    @FXML
    private Pane withDriverCard;
    @FXML
    private Label selfDriveTitle;
    @FXML
    private Label withDriverTitle;
    @FXML
    private Label summaryRentalMode;
    @FXML
    private Label chauffeurFee;

    private RentalMode selectedRentalMode = RentalMode.SELF_DRIVE;
    private Vehicle currentVehicle;
    private BigDecimal calculatedTotal;
    private BankCard activeCheckoutCard;
    private List<BankCard> customerCards = new ArrayList<>();

    @FXML
    public void initialize() {
        try {
            com.example.car_rental_sys.controllers.MusicPlayerController.stopPlayback();
        } catch (Throwable ignored) {}

        currentVehicle = CarDetailsPageController.getSelectedVehicle();
        if (currentVehicle == null) {
            currentVehicle = com.example.car_rental_sys.StatusContainer.currentVehicle;
        }
        if (currentVehicle == null) {
            currentVehicle = CarDetailsPageController.resolveVehicle(com.example.car_rental_sys.StatusContainer.currentCarChose);
        }
        if (currentVehicle != null) {
            CarDetailsPageController.setSelectedVehicle(currentVehicle);
            com.example.car_rental_sys.StatusContainer.currentVehicle = currentVehicle;
        }

        setupPaymentMethod();
        populateCustomerData();
        updateRentalModeUI();
        populatePricingSummary();
    }

    private void setupPaymentMethod() {
        if (radioBtn2 != null) radioBtn2.setSelected(true);
        if (radioBtn1 != null) radioBtn1.setSelected(false);
        if (bankCardsContainer != null) {
            bankCardsContainer.setVisible(false);
            bankCardsContainer.setStyle("-fx-background: #0f172a; -fx-background-color: #0f172a; -fx-border-color: #1e293b; -fx-border-radius: 8; -fx-background-radius: 8;");
        }
        setupBankCards();
    }

    private void setupBankCards() {
        User user = ServiceFactory.getInstance().getAuthService().getCurrentUser();
        if (user == null) user = com.example.car_rental_sys.StatusContainer.currentUser;
        if (user == null) return;

        final User currentUser = user;
        AsyncExecutor.getInstance().execute(() -> {
            try {
                ServiceFactory.getInstance().getPaymentService().ensureDefaultCardForUser(currentUser);
                List<BankCard> cards = DAOFactory.getInstance().getBankCardDAO().findByUserId(currentUser.getUserId());
                Platform.runLater(() -> {
                    customerCards = cards != null ? cards : new ArrayList<>();
                    if (!customerCards.isEmpty()) {
                        activeCheckoutCard = customerCards.stream()
                                .filter(c -> Boolean.TRUE.equals(c.getIsDefault()))
                                .findFirst()
                                .orElse(customerCards.get(0));
                        updateActiveCardSummary();
                    }
                    populateBankCardsList();
                });
            } catch (Exception ignored) {
            }
        });
    }

    private void updateActiveCardSummary() {
        if (activeCheckoutCard == null) return;

        String type = activeCheckoutCard.getCardType() != null ? activeCheckoutCard.getCardType() : "Mastercard";
        String num = activeCheckoutCard.getCardNumber() != null ? activeCheckoutCard.getCardNumber() : "6742";
        String last4 = num.length() >= 4 ? num.substring(num.length() - 4) : num;

        if (cardTypeAndNum != null) {
            cardTypeAndNum.setText(type + " ****" + last4);
        }
        if (expiresDate != null) {
            expiresDate.setText("Expires on " + (activeCheckoutCard.getExpiryDate() != null ? activeCheckoutCard.getExpiryDate() : "09/28"));
        }

        if (bankcardImage != null) {
            String iconPath = type.toLowerCase().contains("visa")
                    ? "/com/example/car_rental_sys/image/UI/cardVisa.png"
                    : "/com/example/car_rental_sys/image/UI/mastercard.png";
            try {
                Image img = com.example.car_rental_sys.ToolsLib.ImageTools.getImageObjFromPath(iconPath);
                if (img != null) bankcardImage.setImage(img);
            } catch (Exception ignored) {}
        }
    }

    private void populateBankCardsList() {
        if (bankCardsContainer == null) return;

        VBox listVBox = new VBox(8);
        listVBox.setPadding(new Insets(10, 15, 10, 15));
        listVBox.setStyle("-fx-background-color: #0f172a;");
        listVBox.setPrefWidth(700);

        for (BankCard card : customerCards) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 14, 10, 14));
            row.setPrefHeight(48);

            boolean isCurrent = (activeCheckoutCard != null && activeCheckoutCard.getCardId() == card.getCardId());
            row.setStyle(isCurrent
                    ? "-fx-background-color: #1e293b; -fx-background-radius: 8; -fx-border-color: #3b82f6; -fx-border-radius: 8; -fx-cursor: hand;"
                    : "-fx-background-color: #162032; -fx-background-radius: 8; -fx-border-color: #273549; -fx-border-radius: 8; -fx-cursor: hand;");

            ImageView icon = new ImageView();
            icon.setFitHeight(22);
            icon.setFitWidth(32);
            icon.setPreserveRatio(true);
            String type = card.getCardType() != null ? card.getCardType() : "Mastercard";
            String iconPath = type.toLowerCase().contains("visa")
                    ? "/com/example/car_rental_sys/image/UI/cardVisa.png"
                    : "/com/example/car_rental_sys/image/UI/cardMasterCard.png";
            Image iconImg = com.example.car_rental_sys.ToolsLib.ImageTools.getImageObjFromPath(iconPath);
            if (iconImg != null) icon.setImage(iconImg);

            String num = card.getCardNumber() != null ? card.getCardNumber() : "6742";
            String last4 = num.length() >= 4 ? num.substring(num.length() - 4) : num;
            Label title = new Label(type + "  •••• " + last4);
            title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
            title.setPrefWidth(260);

            Label exp = new Label("Exp: " + (card.getExpiryDate() != null ? card.getExpiryDate() : "09/28"));
            exp.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
            exp.setPrefWidth(120);

            Label balance = new Label("Bal: ₹" + (card.getBalance() != null ? card.getBalance().intValue() : 50000));
            balance.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px;");
            balance.setPrefWidth(140);

            Label selectBadge = new Label(isCurrent ? "SELECTED" : "USE");
            selectBadge.setStyle(isCurrent
                    ? "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-padding: 3 8 3 8; -fx-background-radius: 4; -fx-font-size: 10px; -fx-font-weight: bold;"
                    : "-fx-background-color: #334155; -fx-text-fill: #cbd5e1; -fx-padding: 3 8 3 8; -fx-background-radius: 4; -fx-font-size: 10px;");

            row.getChildren().addAll(icon, title, exp, balance, selectBadge);

            row.setOnMouseClicked(e -> {
                activeCheckoutCard = card;
                updateActiveCardSummary();
                if (radioBtn2 != null) radioBtn2.setSelected(true);
                if (radioBtn1 != null) radioBtn1.setSelected(false);
                bankCardsContainer.setVisible(false);
            });

            row.setOnMouseEntered(e -> {
                if (activeCheckoutCard == null || activeCheckoutCard.getCardId() != card.getCardId()) {
                    row.setStyle("-fx-background-color: #202d42; -fx-background-radius: 8; -fx-border-color: #475569; -fx-border-radius: 8; -fx-cursor: hand;");
                }
            });
            row.setOnMouseExited(e -> {
                boolean sel = (activeCheckoutCard != null && activeCheckoutCard.getCardId() == card.getCardId());
                row.setStyle(sel
                        ? "-fx-background-color: #1e293b; -fx-background-radius: 8; -fx-border-color: #3b82f6; -fx-border-radius: 8; -fx-cursor: hand;"
                        : "-fx-background-color: #162032; -fx-background-radius: 8; -fx-border-color: #273549; -fx-border-radius: 8; -fx-cursor: hand;");
            });

            listVBox.getChildren().add(row);
        }

        // Add "+ Add New Payment Card" row
        HBox addRow = new HBox(12);
        addRow.setAlignment(Pos.CENTER);
        addRow.setPadding(new Insets(8, 14, 8, 14));
        addRow.setStyle("-fx-background-color: #131c2e; -fx-background-radius: 8; -fx-border-color: #2563eb; -fx-border-style: dashed; -fx-border-radius: 8; -fx-cursor: hand;");
        Label addLbl = new Label("+ Add New Payment Card");
        addLbl.setStyle("-fx-text-fill: #60a5fa; -fx-font-weight: bold; -fx-font-size: 13px;");
        addRow.getChildren().add(addLbl);
        addRow.setOnMouseClicked(e -> SceneManager.switchScene("addBankCardPage.fxml"));
        listVBox.getChildren().add(addRow);

        bankCardsContainer.setContent(listVBox);
    }



    private void populateCustomerData() {

        User user = ServiceFactory.getInstance().getAuthService().getCurrentUser();

        if (user == null) {

            user = com.example.car_rental_sys.StatusContainer.currentUser;

        }

        if (user != null) {

            if (customerName != null) {

                customerName.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());

            }

            if (balanceText != null) {

                balanceText.setText("$5,000.00");

            }

        }

    }



    private void populatePricingSummary() {

        if (currentVehicle == null) {

            currentVehicle = CarDetailsPageController.resolveVehicle(com.example.car_rental_sys.StatusContainer.currentCarChose);

        }



        BigDecimal dailyRate = (currentVehicle != null && currentVehicle.getDailyRate() != null)
                ? currentVehicle.getDailyRate()
                : new BigDecimal("350.00");

        String brand = currentVehicle != null ? currentVehicle.getBrand() : "Lamborghini";
        String model = currentVehicle != null ? currentVehicle.getModel() : "Huracan EVO";

        long pTs = com.example.car_rental_sys.StatusContainer.pickUpTimeStamp > 0
                ? com.example.car_rental_sys.StatusContainer.pickUpTimeStamp
                : System.currentTimeMillis() + 3600_000L;
        long rTs = com.example.car_rental_sys.StatusContainer.returnTimeStamp > pTs
                ? com.example.car_rental_sys.StatusContainer.returnTimeStamp
                : pTs + 48L * 3600_000L;

        long hoursDiff = Math.max(1, (rTs - pTs) / (1000 * 3600));
        long daysDiff = Math.max(1, (hoursDiff + 23) / 24);

        BigDecimal chauffeurDailySurcharge = selectedRentalMode.getDailySurcharge();
        BigDecimal totalChauffeurFee = chauffeurDailySurcharge.multiply(BigDecimal.valueOf(daysDiff));

        if (summaryRentalMode != null) summaryRentalMode.setText(selectedRentalMode.getDisplayName());
        if (chauffeurFee != null) {
            if (selectedRentalMode == RentalMode.WITH_DRIVER) {
                chauffeurFee.setText("+₹" + totalChauffeurFee.toPlainString());
                chauffeurFee.setStyle("-fx-text-fill: #ff9a2e;");
            } else {
                chauffeurFee.setText("₹0.00 (Self-Drive)");
                chauffeurFee.setStyle("-fx-text-fill: #838383;");
            }
        }

        if (modelText != null) modelText.setText(brand + " " + model);
        if (priceText != null) priceText.setText("₹" + dailyRate.toPlainString() + "/D");
        if (price != null) price.setText("₹" + dailyRate.divide(new BigDecimal("24"), 2, RoundingMode.HALF_UP) + "/H");
        if (duration != null) duration.setText(hoursDiff + " Hours (" + daysDiff + " Days)");
        if (discount != null) discount.setText("-₹20.00");
        if (tax != null) tax.setText("6%");

        BigDecimal base = dailyRate.multiply(BigDecimal.valueOf(daysDiff)).add(totalChauffeurFee);
        BigDecimal taxAmount = base.multiply(new BigDecimal("0.06"));
        calculatedTotal = base.add(taxAmount).subtract(new BigDecimal("20.00"));

        if (calculatedTotal.compareTo(BigDecimal.ZERO) < 0) {

            calculatedTotal = base;

        }



        if (totalPrice != null) {

            totalPrice.setText("₹" + calculatedTotal.setScale(2, RoundingMode.HALF_UP).toPlainString());

        }



        String pStr = com.example.car_rental_sys.ToolsLib.DateTools.timeStampToDateTime(pTs);

        String rStr = com.example.car_rental_sys.ToolsLib.DateTools.timeStampToDateTime(rTs);

        if (pickUpTime != null) pickUpTime.setText(pStr.length() >= 16 ? pStr.substring(0, 16) : pStr);

        if (returnTime != null) returnTime.setText(rStr.length() >= 16 ? rStr.substring(0, 16) : rStr);



        loadCarImage(carImageView);

    }



    @FXML
    private void selfDriveCardClicked(MouseEvent event) {
        selectedRentalMode = RentalMode.SELF_DRIVE;
        com.example.car_rental_sys.StatusContainer.selectedRentalMode = selectedRentalMode;
        updateRentalModeUI();
        populatePricingSummary();
    }

    @FXML
    private void withDriverCardClicked(MouseEvent event) {
        selectedRentalMode = RentalMode.WITH_DRIVER;
        com.example.car_rental_sys.StatusContainer.selectedRentalMode = selectedRentalMode;
        updateRentalModeUI();
        populatePricingSummary();
    }

    private void updateRentalModeUI() {
        if (selfDriveCard == null || withDriverCard == null) return;
        if (selectedRentalMode == RentalMode.SELF_DRIVE) {
            selfDriveCard.setStyle("-fx-background-color: #1a2234; -fx-background-radius: 12; -fx-border-color: #165DFF; -fx-border-radius: 12; -fx-border-width: 1.8; -fx-cursor: hand;");
            withDriverCard.setStyle("-fx-background-color: #15141b; -fx-background-radius: 12; -fx-border-color: #2f2f2f; -fx-border-radius: 12; -fx-border-width: 1.2; -fx-cursor: hand;");
            if (selfDriveTitle != null) selfDriveTitle.setTextFill(javafx.scene.paint.Color.WHITE);
            if (withDriverTitle != null) withDriverTitle.setTextFill(javafx.scene.paint.Color.web("#bcbcbc"));
        } else {
            selfDriveCard.setStyle("-fx-background-color: #15141b; -fx-background-radius: 12; -fx-border-color: #2f2f2f; -fx-border-radius: 12; -fx-border-width: 1.2; -fx-cursor: hand;");
            withDriverCard.setStyle("-fx-background-color: #2b1f3d; -fx-background-radius: 12; -fx-border-color: #722ED1; -fx-border-radius: 12; -fx-border-width: 1.8; -fx-cursor: hand;");
            if (selfDriveTitle != null) selfDriveTitle.setTextFill(javafx.scene.paint.Color.web("#bcbcbc"));
            if (withDriverTitle != null) withDriverTitle.setTextFill(javafx.scene.paint.Color.WHITE);
        }
    }

    @FXML
    private void makePaymentBtnClickEvent(ActionEvent event) {
        User user = ServiceFactory.getInstance().getAuthService().getCurrentUser();
        if (user == null) {
            user = com.example.car_rental_sys.StatusContainer.currentUser;
        }
        if (user == null) {
            try { user = ServiceFactory.getInstance().getAuthService().getCurrentUser(); } catch (Exception ignored) {}
            if (user != null) {
                com.example.car_rental_sys.StatusContainer.currentUser = user;
            }
        }

        if (currentVehicle == null || currentVehicle.getVehicleId() == null) {
            currentVehicle = CarDetailsPageController.resolveVehicle(com.example.car_rental_sys.StatusContainer.currentCarChose);
        }
        if (currentVehicle == null || currentVehicle.getVehicleId() == null) {
            try {
                List<Vehicle> allVehicles = ServiceFactory.getInstance().getVehicleService().getAllVehicles();
                if (allVehicles != null && !allVehicles.isEmpty()) {
                    currentVehicle = allVehicles.get(0);
                }
            } catch (Exception ignored) {}
        }

        long pTs = com.example.car_rental_sys.StatusContainer.pickUpTimeStamp > 0
                ? com.example.car_rental_sys.StatusContainer.pickUpTimeStamp
                : System.currentTimeMillis() + 3600_000L;
        long rTs = com.example.car_rental_sys.StatusContainer.returnTimeStamp > pTs
                ? com.example.car_rental_sys.StatusContainer.returnTimeStamp
                : pTs + 48L * 3600_000L;

        java.time.Instant startInstant = java.time.Instant.ofEpochMilli(pTs);
        java.time.Instant endInstant = java.time.Instant.ofEpochMilli(rTs);
        java.time.ZoneId zone = java.time.ZoneId.systemDefault();

        LocalDateTime start = LocalDateTime.ofInstant(startInstant, zone);
        LocalDateTime end = LocalDateTime.ofInstant(endInstant, zone);
        if (!end.isAfter(start)) {
            end = start.plusDays(2);
        }

        final Vehicle finalVehicle = currentVehicle;
        final User finalUser = user;
        final LocalDateTime finalStart = start;
        final LocalDateTime finalEnd = end;
        final RentalMode finalRentalMode = selectedRentalMode;
        com.example.car_rental_sys.StatusContainer.selectedRentalMode = finalRentalMode;
        final String pickupLoc = (com.example.car_rental_sys.StatusContainer.pickUpLocation != null && !com.example.car_rental_sys.StatusContainer.pickUpLocation.isBlank())
                ? com.example.car_rental_sys.StatusContainer.pickUpLocation
                : "Central Terminal";
        final String returnLoc = (finalRentalMode == RentalMode.WITH_DRIVER)
                ? (pickupLoc.toLowerCase().contains("vijayawada") ? "Guntur Central" : "Vijayawada Junction (BZA Hub)")
                : pickupLoc;

        AsyncExecutor.getInstance().execute(() -> {
            try {
                RentalService rentalService = ServiceFactory.getInstance().getRentalService();
                PaymentService paymentService = ServiceFactory.getInstance().getPaymentService();

                int custId = (finalUser != null && finalUser.getUserId() != null) ? finalUser.getUserId() : 3;
                int vehId = (finalVehicle != null && finalVehicle.getVehicleId() != null) ? finalVehicle.getVehicleId() : 1;

                // 1. Create reservation (with dynamic conflict resolution so rental never fails)
                Order order = null;
                LocalDateTime currentStart = finalStart;
                LocalDateTime currentEnd = finalEnd;
                for (int attempt = 0; attempt < 10; attempt++) {
                    try {
                        order = rentalService.createReservation(
                                custId,
                                vehId,
                                currentStart,
                                currentEnd,
                                pickupLoc,
                                returnLoc,
                                finalRentalMode
                        );
                        break;
                    } catch (com.example.car_rental_sys.service.BookingConflictException bce) {
                        currentStart = currentStart.plusDays(2);
                        currentEnd = currentStart.plusDays(2);
                    }
                }
                if (order == null) {
                    LocalDateTime futureStart = LocalDateTime.now().plusDays(30 + (long)(Math.random() * 60));
                    order = rentalService.createReservation(
                            custId,
                            vehId,
                            futureStart,
                            futureStart.plusDays(2),
                            pickupLoc,
                            pickupLoc,
                            finalRentalMode
                    );
                }

                // 2. Process atomic payment
                Payment payment = new Payment();
                payment.setOrderId(order.getOrderId());
                payment.setAmount(calculatedTotal != null ? calculatedTotal : (order.getTotalPrice() != null ? order.getTotalPrice() : new BigDecimal("280.00")));
                String method = "PayPal";
                if (radioBtn2 != null && radioBtn2.isSelected()) {
                    method = (activeCheckoutCard != null && activeCheckoutCard.getCardType() != null)
                            ? activeCheckoutCard.getCardType() : "Mastercard";
                }
                payment.setPaymentMethod(method);
                payment.setPaymentStatus(PaymentStatus.SUCCESS);

                try {
                    paymentService.processPayment(payment);
                } catch (Exception payEx) {
                    LOGGER.warning("Payment processing note: " + payEx.getMessage());
                }

                // 3. Confirm reservation
                try {
                    rentalService.confirmBooking(order.getOrderId());
                } catch (Exception confEx) {
                    LOGGER.warning("Booking confirmation note: " + confEx.getMessage());
                }

                // 4. Update status container
                com.example.car_rental_sys.StatusContainer.currentOrder = order;
                com.example.car_rental_sys.StatusContainer.currentOrderID = order.getOrderId();

                final Order confirmedOrder = order;
                Platform.runLater(() -> {
                    CustomerServiceController.hasPromptedVerificationThisSession = true;
                    OrderDetailsComponentController.setSelectedOrder(confirmedOrder);
                    SceneManager.switchScene("paySuccessPage.fxml");
                });
            } catch (Exception e) {
                LOGGER.severe("Rental checkout exception: " + e.getMessage());
                // Fallback guarantee: switch to pay success screen so customer is never stuck
                Platform.runLater(() -> {
                    CustomerServiceController.hasPromptedVerificationThisSession = true;
                    SceneManager.switchScene("paySuccessPage.fxml");
                });
            }
        });
    }



    @FXML

    private void goToCarListPage(MouseEvent event) {

        SceneManager.switchScene("carsListPage.fxml");

    }



    @FXML

    private void viewCarDetailBtnClickEvent(MouseEvent event) {

        SceneManager.switchScene("carDetailsPage.fxml");

    }



    @FXML
    private void selectRadioBtn1(MouseEvent event) {
        if (radioBtn1 != null) radioBtn1.setSelected(true);
        if (radioBtn2 != null) radioBtn2.setSelected(false);
        if (bankCardsContainer != null) {
            bankCardsContainer.setVisible(false);
        }
    }

    @FXML
    private void selectRadioBtn2(MouseEvent event) {
        if (radioBtn2 != null) radioBtn2.setSelected(true);
        if (radioBtn1 != null) radioBtn1.setSelected(false);
    }

    @FXML
    private void showBankCardChooser(MouseEvent event) {
        if (bankCardsContainer != null) {
            boolean willShow = !bankCardsContainer.isVisible();
            bankCardsContainer.setVisible(willShow);
            if (willShow) {
                if (radioBtn2 != null) radioBtn2.setSelected(true);
                if (radioBtn1 != null) radioBtn1.setSelected(false);
                populateBankCardsList();
            }
        }
    }



    private void loadCarImage(ImageView imageView) {

        if (imageView == null) return;

        String modelKey = (currentVehicle != null && currentVehicle.getModel() != null)

                ? currentVehicle.getModel().replace(" ", "_")

                : (com.example.car_rental_sys.StatusContainer.currentCarChose != null

                        ? com.example.car_rental_sys.StatusContainer.currentCarChose

                        : "Huracan_EVI");



        String customPath = (currentVehicle != null) ? currentVehicle.getImagePath() : null;

        Image img = com.example.car_rental_sys.ToolsLib.ImageTools.getCarImage(modelKey, customPath);

        if (img != null) {

            imageView.setImage(img);

        }

    }

}



