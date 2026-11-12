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
        switch (type.toLowerCase()) {
            case "visa":
                initBankCard("visa");
                break;
            case "mastercard":
            case "master":
                initBankCard("master");
                break;
            case "tng":
                initWalletCard("tng");
                break;
            case "paypal":
                initWalletCard("paypal");
                break;
            default:
                initAddNewCard();
                break;
        }
    }

    private void initWalletCard(String type) {
        ImageView imageView = new ImageView();
        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        imageView.setLayoutX(10);
        imageView.setLayoutY(6);

        String path = Objects.equals(type, "tng")
                ? "/com/example/car_rental_sys/image/UI/cardTng.png"
                : "/com/example/car_rental_sys/image/UI/cardPaypal.png";
        Image img = ImageTools.getImageObjFromPath(path);
        if (img != null) imageView.setImage(img);

        Label nameLbl = new Label(this.cardName);
        nameLbl.setStyle("-fx-text-fill: #86909C; -fx-font-size: 11px; -fx-font-weight: bold;");
        nameLbl.setLayoutX(44);
        nameLbl.setLayoutY(10);

        Label balLbl = new Label("₹ " + this.cardBalance + ".00");
        balLbl.setStyle("-fx-text-fill: #0E42D2; -fx-font-size: 16px; -fx-font-weight: bold;");
        balLbl.setLayoutX(16);
        balLbl.setLayoutY(40);

        this.getChildren().addAll(imageView, nameLbl, balLbl);
        this.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #C9CDD4; -fx-border-width: 1px;");
    }

    private void initBankCard(String type) {
        ImageView imageView = new ImageView();
        imageView.setFitHeight(20);
        imageView.setLayoutX(10);
        imageView.setLayoutY(6);

        String logoPath = Objects.equals(type, "visa")
                ? "/com/example/car_rental_sys/image/UI/cardVisa.png"
                : "/com/example/car_rental_sys/image/UI/cardMasterCard.png";
        imageView.setFitWidth(Objects.equals(type, "visa") ? 20 : 32);
        Image logoImg = ImageTools.getImageObjFromPath(logoPath);
        if (logoImg != null) imageView.setImage(logoImg);

        String last4Digits = this.cardNumber.length() >= 4
                ? this.cardNumber.substring(this.cardNumber.length() - 4)
                : "1234";

        Label numLbl = new Label("**" + last4Digits);
        numLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold;");
        numLbl.setLayoutX(10);
        numLbl.setLayoutY(70);

        Label bNameLbl = new Label(this.bankName);
        bNameLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: bold;");
        bNameLbl.setLayoutX(26);
        bNameLbl.setLayoutY(44);

        ImageView wirelessIcon = new ImageView();
        wirelessIcon.setFitHeight(24);
        wirelessIcon.setFitWidth(24);
        wirelessIcon.setLayoutX(118);
        wirelessIcon.setLayoutY(20);
        Image wImg = ImageTools.getImageObjFromPath("/com/example/car_rental_sys/image/UI/wireless.png");
        if (wImg != null) wirelessIcon.setImage(wImg);

        this.getChildren().addAll(imageView, numLbl, bNameLbl, wirelessIcon);
        if ("visa".equalsIgnoreCase(type)) {
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
            if ("addNewCard".equalsIgnoreCase(cardType)) {
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

