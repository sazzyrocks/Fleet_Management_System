package com.example.car_rental_sys.ui_components;

import com.example.car_rental_sys.StatusContainer;
import com.example.car_rental_sys.ToolsLib.ImageTools;
import com.example.car_rental_sys.util.EnvConfig;
import com.example.car_rental_sys.util.SceneManager;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * Authentic BrowserFrame popup matching original JxBrowser frame architecture,
 * powered by native JavaFX WebView & WebEngine for 100% JDK 26 modern compatibility.
 */
public class BrowserFrame {
    private int width;
    private int height;
    private String url;
    private double locationX, locationY;

    protected Stage stage = new Stage();
    protected WebView webView = new WebView();
    protected Function<String, Void> callBackFunc = null;
    private String currentSelectedLocation = "SRM University-AP, Amaravati";

    private String captionBarBgc = "#323844";

    public BrowserFrame(int width, int height, String url) {
        this.width = width;
        this.height = height;
        this.url = url;
        try {
            stage.initStyle(StageStyle.UNDECORATED);
        } catch (Exception ignored) {}
        stage.setResizable(false);
    }

    private void initBrowser() {
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);

        // Alert listener for selection and bridge events
        engine.setOnAlert(event -> {
            String message = event.getData();
            if (message != null && !message.isBlank()) {
                callBack(message);
                Platform.runLater(() -> stage.close());
            }
        });

        loadTargetUrl(engine);
    }

    private void loadTargetUrl(WebEngine engine) {
        if (url == null || url.isBlank()) return;

        if (url.startsWith("http://") || url.startsWith("https://")) {
            engine.load(url);
            return;
        }

        // Try local resource on classpath
        String resPath = url;
        if (resPath.startsWith("src/main/resources")) {
            resPath = resPath.substring("src/main/resources".length());
        }
        if (!resPath.startsWith("/")) {
            resPath = "/" + resPath;
        }

        URL resource = getClass().getResource(resPath);
        if (resource != null) {
            engine.load(resource.toExternalForm());
            return;
        }

        // File path fallback
        File file = new File(url);
        if (file.exists()) {
            engine.load(file.toURI().toString());
        }
    }

    private void initStage() {
        if (stage.getScene() == null) {
            webView.setPrefSize(width, height);
            webView.setMinSize(width, height);

            BorderPane browserContainer = new BorderPane(webView);
            browserContainer.setPrefSize(width, height);
            browserContainer.setLayoutX(0);
            browserContainer.setLayoutY(30);

            Pane root = new Pane();
            root.getChildren().addAll(browserContainer, getCaptionBar(), getCloseButton());

            Scene scene = new Scene(root, this.width, this.height + 30);
            stage.setScene(scene);
        }

        // Center on primary stage if available
        Stage owner = SceneManager.getPrimaryStage();
        if (owner != null && owner.isShowing()) {
            stage.setX(owner.getX() + Math.max(0, (owner.getWidth() - this.width) / 2.0));
            stage.setY(owner.getY() + Math.max(0, (owner.getHeight() - (this.height + 30)) / 2.0));
        }
    }

    private HBox createLocationSelectionBar() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPrefHeight(46);
        bar.setStyle("-fx-background-color: #242933; -fx-padding: 0 14px; -fx-border-color: rgba(255, 255, 255, 0.12); -fx-border-width: 1px 0 0 0;");

        Label selectedLabel = new Label("📍 Selected: " + currentSelectedLocation);
        selectedLabel.setStyle("-fx-text-fill: #e8eaed; -fx-font-weight: bold; -fx-font-size: 12px;");
        HBox.setHgrow(selectedLabel, Priority.ALWAYS);

        // Track user navigation inside Google Maps
        webView.getEngine().locationProperty().addListener((obs, oldUrl, newUrl) -> {
            if (newUrl != null && newUrl.contains("/maps/place/")) {
                try {
                    String part = newUrl.substring(newUrl.indexOf("/maps/place/") + "/maps/place/".length());
                    if (part.contains("/")) {
                        part = part.substring(0, part.indexOf("/"));
                    }
                    String decoded = URLDecoder.decode(part.replace("+", " "), StandardCharsets.UTF_8);
                    if (!decoded.isBlank()) {
                        currentSelectedLocation = decoded;
                        Platform.runLater(() -> selectedLabel.setText("📍 Selected: " + decoded));
                    }
                } catch (Exception ignored) {}
            }
        });

        // Quick Hub chips for easy one-click selection
        Button srmBtn = createHubButton("📍 SRM AP", "SRM University-AP, Amaravati", selectedLabel);
        Button bzaBtn = createHubButton("🚆 Vijayawada", "Vijayawada Junction, Andhra Pradesh", selectedLabel);
        Button vgaBtn = createHubButton("✈️ Airport", "Vijayawada International Airport, Gannavaram", selectedLabel);
        Button aiimsBtn = createHubButton("🏥 AIIMS", "AIIMS Mangalagiri, Andhra Pradesh", selectedLabel);
        Button gntBtn = createHubButton("🏢 Guntur", "Guntur Railway Station, Andhra Pradesh", selectedLabel);

        Button confirmBtn = new Button("✓ Select This Location");
        confirmBtn.setStyle("-fx-background-color: #165dff; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 6px; -fx-padding: 6px 16px; -fx-cursor: hand;");
        confirmBtn.setOnAction(e -> {
            callBack(currentSelectedLocation);
            stage.close();
        });

        bar.getChildren().addAll(selectedLabel, srmBtn, bzaBtn, vgaBtn, aiimsBtn, gntBtn, confirmBtn);
        return bar;
    }

    private Button createHubButton(String label, String fullPlace, Label selectedLabel) {
        Button btn = new Button(label);
        btn.setStyle("-fx-background-color: #15181e; -fx-text-fill: #d1d5db; -fx-font-size: 11px; -fx-background-radius: 12px; -fx-padding: 4px 10px; -fx-cursor: hand; -fx-border-color: #3c4454; -fx-border-radius: 12px;");
        btn.setOnAction(e -> {
            currentSelectedLocation = fullPlace;
            selectedLabel.setText("📍 Selected: " + fullPlace.split(",")[0]);
            webView.getEngine().load("https://www.google.com/maps/search/" + URLEncoder.encode(fullPlace, StandardCharsets.UTF_8) + "/");
        });
        return btn;
    }

    public Label getCaptionBar() {
        Label captionBar = new Label();
        captionBar.setPrefSize(this.width, 30);
        captionBar.setStyle("-fx-background-color: " + captionBarBgc + ";");
        captionBar.setLayoutX(0);
        captionBar.setLayoutY(0);

        captionBar.setOnMousePressed((MouseEvent event) -> {
            locationX = stage.getX() - event.getScreenX();
            locationY = stage.getY() - event.getScreenY();
        });

        captionBar.setOnMouseDragged((MouseEvent event) -> {
            stage.setX(event.getScreenX() + locationX);
            stage.setY(event.getScreenY() + locationY);
        });
        return captionBar;
    }

    public ImageView getCloseButton() {
        ImageView closeIcon = new ImageView();
        closeIcon.setX(this.width - 30);
        closeIcon.setY(5);
        closeIcon.setFitWidth(20);
        closeIcon.setFitHeight(20);

        Image image = ImageTools.getImageObjFromPath("/com/example/car_rental_sys/image/UI/closeIconFrame.png");
        Image imageHover = ImageTools.getImageObjFromPath("/com/example/car_rental_sys/image/UI/closeIconHoverFrame.png");

        if (image != null) closeIcon.setImage(image);

        closeIcon.setOnMouseClicked(e -> stage.close());

        closeIcon.setOnMouseEntered(e -> {
            if (imageHover != null) closeIcon.setImage(imageHover);
            if (stage.getScene() != null) stage.getScene().setCursor(Cursor.HAND);
        });

        closeIcon.setOnMouseExited(e -> {
            if (image != null) closeIcon.setImage(image);
            if (stage.getScene() != null) stage.getScene().setCursor(Cursor.DEFAULT);
        });
        return closeIcon;
    }

    protected void callBack(String message) {
        if (callBackFunc != null) {
            Platform.runLater(() -> callBackFunc.apply(message));
        }
        Platform.runLater(() -> {
            try {
                stage.close();
            } catch (Exception ignored) {}
        });
    }

    public void show() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::show);
            return;
        }
        initBrowser();
        initStage();
        stage.show();
    }

    public void setModality() {
        setModality(Modality.APPLICATION_MODAL);
    }

    public void setModality(Modality modal) {
        try {
            stage.initModality(modal);
        } catch (Exception ignored) {}
        try {
            Stage owner = SceneManager.getPrimaryStage();
            if (owner != null && owner.isShowing()) {
                stage.initOwner(owner);
            }
        } catch (Exception ignored) {}
    }

    public void setFunction(Function<String, Void> func) {
        this.callBackFunc = func;
    }

    /**
     * JavaScript Bridge exposing methods to in-page JavaScript.
     */
    public class JavaBridge {
        public void setLocationText(String text) {
            StatusContainer.locationChose = text;
            StatusContainer.pickUpLocation = text;
            callBack(text);
        }

        public void onDateRangeSelected(String datePayload) {
            callBack(datePayload);
        }

        public String sayHelloTo(String name) {
            return "Hello " + name;
        }
    }
}

