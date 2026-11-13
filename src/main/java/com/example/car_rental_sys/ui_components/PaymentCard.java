package com.example.car_rental_sys.ui_components;

import com.example.car_rental_sys.StatusContainer;
import com.example.car_rental_sys.ToolsLib.FXTools;
import com.example.car_rental_sys.ToolsLib.ImageTools;
import com.example.car_rental_sys.controllers.showCardDetailsController;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.Objects;

/**
 * Authentic visual bank/wallet card component for BillingComponent.fxml & addBankCardPage.fxml.
 */
public class PaymentCard extends Pane {

    private String cardNumber = "4000123456789010";
    private String cardName = "Alex Johnson";
    private String bankName = "Maybank";
    private String cardBalance = "1250";
    private String cardType;

    public PaymentCard(String type) {
        this.setPrefSize(150, 90);
        this.cardType = type;
        selectCardType(type);
        initEvent();
    }

    public PaymentCard(String type, String number, String cardName, String bankName, String cardBalance) {
        this.setPrefSize(150, 90);
        this.cardNumber = number != null ? number : "4000123456789010";
        this.cardName = cardName != null ? cardName : "Alex Johnson";
        this.bankName = bankName != null ? bankName : "Maybank";
        this.cardBalance = cardBalance != null ? cardBalance : "1250";
        this.cardType = type;
        selectCardType(type);
        initEvent();
    }

    private void selectCardType(String type) {
        String t = type != null ? type.toLowerCase().trim() : "";
        if (t.contains("visa")) {
            initBankCard("visa");
        } else if (t.contains("master")) {
            initBankCard("master");
        } else if (t.contains("tng") || t.contains("touch")) {
            initWalletCard("tng");
        } else if (t.contains("paypal")) {
            initWalletCard("paypal");
        } else if (t.contains("addnew") || t.contains("newcard")) {
            initAddNewCard();
        } else {
            initBankCard("master");
        }
    }

    private void initWalletCard(String type) {
        boolean isTng = type.toLowerCase().contains("tng") || type.toLowerCase().contains("touch");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(22);
        imageView.setFitWidth(28);
        imageView.setLayoutX(12);
        imageView.setLayoutY(10);
        imageView.setPreserveRatio(true);

        String path = isTng
                ? "/com/example/car_rental_sys/image/UI/cardTng.png"
                : "/com/example/car_rental_sys/image/UI/cardPaypal.png";
        Image img = ImageTools.getImageObjFromPath(path);
        if (img != null) imageView.setImage(img);

        String displayName = isTng ? "Touch 'n Go" : "PayPal";
        Label nameLbl = new Label(displayName);
        nameLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold;");
        nameLbl.setLayoutX(44);
        nameLbl.setLayoutY(12);

        Label balLbl = new Label("₹ " + this.cardBalance);
        balLbl.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 14px; -fx-font-weight: bold;");
        balLbl.setLayoutX(12);
        balLbl.setLayoutY(38);

        String last4 = this.cardNumber.length() >= 4
                ? this.cardNumber.substring(this.cardNumber.length() - 4)
                : this.cardNumber;
        Label numLbl = new Label("•••• " + last4);
        numLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: bold;");
        numLbl.setLayoutX(12);
        numLbl.setLayoutY(66);

        this.getChildren().addAll(imageView, nameLbl, balLbl, numLbl);
        if (isTng) {
            this.setStyle("-fx-background-color: linear-gradient(to bottom, #001f3f, #005bac); -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #38bdf8; -fx-border-width: 1px;");
        } else {
            this.setStyle("-fx-background-color: linear-gradient(to bottom, #001833, #00457c); -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #0079c1; -fx-border-width: 1px;");
        }
    }

    private void initBankCard(String type) {
        ImageView imageView = new ImageView();
        imageView.setFitHeight(20);
        imageView.setLayoutX(10);
        imageView.setLayoutY(8);
        imageView.setPreserveRatio(true);

        boolean isVisa = type.toLowerCase().contains("visa");
        String logoPath = isVisa
                ? "/com/example/car_rental_sys/image/UI/cardVisa.png"
                : "/com/example/car_rental_sys/image/UI/cardMasterCard.png";
        imageView.setFitWidth(isVisa ? 24 : 32);
        Image logoImg = ImageTools.getImageObjFromPath(logoPath);
        if (logoImg != null) imageView.setImage(logoImg);

        String last4Digits = this.cardNumber.length() >= 4
                ? this.cardNumber.substring(this.cardNumber.length() - 4)
                : "1234";

        Label numLbl = new Label("•••• " + last4Digits);
        numLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold;");
        numLbl.setLayoutX(10);
        numLbl.setLayoutY(68);

        Label bNameLbl = new Label(this.bankName);
        bNameLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: bold;");
        bNameLbl.setLayoutX(26);
        bNameLbl.setLayoutY(42);

        ImageView wirelessIcon = new ImageView();
        wirelessIcon.setFitHeight(20);
        wirelessIcon.setFitWidth(20);
        wirelessIcon.setLayoutX(118);
        wirelessIcon.setLayoutY(16);
        Image wImg = ImageTools.getImageObjFromPath("/com/example/car_rental_sys/image/UI/wireless.png");
        if (wImg != null) wirelessIcon.setImage(wImg);

        this.getChildren().addAll(imageView, numLbl, bNameLbl, wirelessIcon);
        if (isVisa) {
            this.setStyle("-fx-background-color: linear-gradient(to bottom, #0f172a, #1e3a8a); -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #3b82f6; -fx-border-width: 1px;");
        } else {
            this.setStyle("-fx-background-color: linear-gradient(to bottom, #18181b, #450a0a); -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #f59e0b; -fx-border-width: 1px;");
        }
    }

    private void initAddNewCard() {
        ImageView addNewCard = new ImageView();
        addNewCard.setFitHeight(24);
        addNewCard.setFitWidth(24);
        addNewCard.setLayoutX(63);
        addNewCard.setLayoutY(28);
        Image addImg = ImageTools.getImageObjFromPath("/com/example/car_rental_sys/image/UI/addNewCard.png");
        if (addImg != null) addNewCard.setImage(addImg);

        Label newCard = new Label("+ New Card");
        newCard.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-font-weight: bold;");
        newCard.setLayoutX(43);
        newCard.setLayoutY(61);

        this.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand;");
        this.getChildren().addAll(addNewCard, newCard);
    }

    private void initEvent() {
        this.setOnMouseEntered(event -> this.setCursor(Cursor.HAND));
        this.setOnMouseExited(event -> this.setCursor(Cursor.DEFAULT));

        this.setOnMouseClicked(event -> {
            String t = cardType != null ? cardType.toLowerCase() : "";
            if (t.contains("addnew") || t.contains("newcard")) {
                com.example.car_rental_sys.util.SceneManager.switchScene("addBankCardPage.fxml");
            } else {
                StatusContainer.currentChooseBankCardNum = this.cardNumber;
                if (showCardDetailsController.instance != null) {
                    Platform.runLater(() -> showCardDetailsController.instance.loadCardDetails());
                }
            }
        });
    }
}

