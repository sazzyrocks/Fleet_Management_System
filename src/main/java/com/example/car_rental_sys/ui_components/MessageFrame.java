package com.example.car_rental_sys.ui_components;

import com.example.car_rental_sys.StatusContainer;
import com.example.car_rental_sys.ToolsLib.ImageTools;
import com.example.car_rental_sys.util.SceneManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Objects;
import java.util.function.Function;

/**
 * Authentic floating modal toast / alert notification.
 */
public class MessageFrame {
    double transparent = 0.7;
    int titleSpace = 5;

    protected String type;
    protected String message;
    protected String title = "";
    protected Function<Integer, Void> successCallback = null;
    protected Function<Integer, Void> failedCallback = null;

    protected Pane mainPane;
    protected final Pane mainBGCPane = new VBox();
    protected final VBox messageVPane = new VBox();
    protected final Pane messageBoxContainer = new Pane();
    protected String iconPath = null;

    public MessageFrame(String type) {
        this(type, "");
    }

    public MessageFrame(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public MessageFrame(String type, String message, String title) {
        this.type = type;
        this.message = message;
        this.title = title;
    }

    public void show() {
        resolveMainPane();
        if (mainPane == null) return;
        addBackgroundPane();
        addMessageBox();
        addIcon();
        addTitle();
        addMessage();
        addBtn();
        cleanUp();
    }

    private void resolveMainPane() {
        if (StatusContainer.currentPageController != null && StatusContainer.currentPageController.mainPane != null) {
            mainPane = StatusContainer.currentPageController.mainPane;
            return;
        }
        if (SceneManager.getPrimaryStage() != null && SceneManager.getPrimaryStage().getScene() != null) {
            Parent root = SceneManager.getPrimaryStage().getScene().getRoot();
            if (root instanceof Pane) {
                mainPane = (Pane) root;
            }
        }
    }

    private void addBackgroundPane() {
        mainBGCPane.setPrefSize(1280, 832);
        mainBGCPane.setLayoutX(0);
        mainBGCPane.setLayoutY(0);
        mainBGCPane.setStyle("-fx-background-color: rgba(0,0,0," + transparent + ");");
        mainPane.getChildren().add(mainBGCPane);
    }

    private void addMessageBox() {
        int width = 465;
        int height = 235;
        messageBoxContainer.setPrefSize(width, height);
        messageBoxContainer.setLayoutX((1280 - width) / 2.0);
        messageBoxContainer.setLayoutY((832 - height) / 2.0 - 50);
        messageBoxContainer.setStyle("-fx-background-color: transparent;");

        messageVPane.setPrefSize(width, height);
        messageVPane.setLayoutX((1280 - width) / 2.0);
        messageVPane.setLayoutY((832 - height) / 2.0 - 50);
        messageVPane.setStyle("-fx-background-color: #2a2a2b; -fx-background-radius: 10px;");
        messageBoxContainer.getChildren().add(messageVPane);

        mainBGCPane.getChildren().add(messageBoxContainer);
    }

    private void addIcon() {
        String iconPathRoot = "/com/example/car_rental_sys/image/UI/";
        switch (type) {
            case MessageFrameType.SUCCESS:
                iconPath = iconPathRoot + "success.png";
                break;
            case MessageFrameType.ERROR:
                iconPath = iconPathRoot + "error.png";
                break;
            case MessageFrameType.WARNING:
            case MessageFrameType.CONFIRM:
                iconPath = iconPathRoot + "warning.png";
                break;
            case MessageFrameType.NOTIFICATION:
            case MessageFrameType.FEEDBACK:
            default:
                iconPath = iconPathRoot + "notification.png";
                break;
        }
    }

    private void addTitle() {
        String titleText = type;
        Label titleLbl = new Label(titleText);

        Image img = ImageTools.getImageObjFromPath(iconPath);
        if (img != null) {
            ImageView imageView = new ImageView(img);
            imageView.setFitWidth(20);
            imageView.setFitHeight(20);
            titleLbl.setGraphic(imageView);
        }

        titleLbl.setPrefHeight(80);
        titleLbl.setAlignment(Pos.CENTER_LEFT);
        titleLbl.setGraphicTextGap(10);
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(titleLbl);
        messageVPane.getChildren().add(borderPane);
    }

    private void addMessage() {
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14px;");
        VBox.setMargin(messageLabel, new Insets(0, 40, 0, 40));
        messageLabel.setWrapText(true);
        messageVPane.getChildren().add(messageLabel);
    }

    private void addBtn() {
        if (Objects.equals(type, MessageFrameType.CONFIRM)) {
            addTwoBtn();
        } else {
            addSingleBtn();
        }
    }

    private void addSingleBtn() {
        Button btn = new Button("OK");
        btn.setStyle("-fx-background-color: #165dff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px;");
        btn.setPrefSize(100, 32);
        btn.setOnAction(e -> callSuccessCallback());

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(btn);
        VBox.setMargin(borderPane, new Insets(40, 0, 0, 0));
        messageVPane.getChildren().add(borderPane);
    }

    private void addTwoBtn() {
        Button btn1 = new Button("OK");
        btn1.setStyle("-fx-background-color: #165dff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px;");
        btn1.setPrefSize(100, 32);
        btn1.setOnMouseClicked(e -> callSuccessCallback());

        Button btn2 = new Button("Cancel");
        btn2.setStyle("-fx-background-color: #f53f3f; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px;");
        btn2.setPrefSize(100, 32);
        btn2.setOnMouseClicked(e -> callFailedCallback());

        HBox hBox = new HBox(20);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(btn1, btn2);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(hBox);
        VBox.setMargin(borderPane, new Insets(40, 0, 0, 0));
        messageVPane.getChildren().add(borderPane);
    }

    public void close() {
        if (mainPane != null) {
            mainPane.getChildren().remove(mainBGCPane);
        }
    }

    private void callSuccessCallback() {
        if (this.successCallback != null) {
            this.successCallback.apply(1);
        }
        close();
    }

    private void callFailedCallback() {
        if (this.failedCallback != null) {
            this.failedCallback.apply(1);
        }
        close();
    }

    public void setSuccessCallbackFunc(Function<Integer, Void> func) {
        this.successCallback = func;
    }

    public void setFailedCallbackFunc(Function<Integer, Void> func) {
        this.failedCallback = func;
    }

    private void cleanUp() {
        if (Objects.equals(type, MessageFrameType.FEEDBACK) && mainPane != null) {
            mainPane.getChildren().remove(mainBGCPane);
        }
    }
}

