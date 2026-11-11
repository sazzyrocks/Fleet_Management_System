package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.StatusContainer;
import com.example.car_rental_sys.ToolsLib.ImageTools;
import com.example.car_rental_sys.model.DeliveryOrder;
import com.example.car_rental_sys.service.DeliveryOrderManager;
import com.example.car_rental_sys.ui_components.MessageFrame;
import com.example.car_rental_sys.ui_components.MessageFrameType;
import com.example.car_rental_sys.ui_components.OrderCard;
import com.example.car_rental_sys.util.AlertHelper;
import com.example.car_rental_sys.util.PlatformTools;
import com.example.car_rental_sys.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controller for the Driver Mission Control & Active Delivery Operations.
 */
public class DriverMainPageController extends Controller {

    public static DriverMainPageController driverMainPageInstance;

    @FXML
    public Pane mainPane;

    @FXML
    private Pane optionPane;

    @FXML
    private Pane middlePanel;

    @FXML
    private Pane settingMainPanel;

    @FXML
    private Pane workplacePane;

    @FXML
    private WebView webview;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Pane processTipPane;

    @FXML
    private Pane processImagePane;

    @FXML
    private Label processTipLabel;

    @FXML
    private Pane sidePane;

    @FXML
    private ImageView renterAvatar;

    @FXML
    private Label renterName;

    @FXML
    private Label renterPost;

    @FXML
    private Pane messagePane;

    @FXML
    private Pane phonePane;

    @FXML
    private Pane closeSideBarPane;

    @FXML
    private ImageView carImageView;

    @FXML
    private Label seatsLabel;

    @FXML
    private Label powerLabel;

    @FXML
    private Label speedLabel;

    @FXML
    private Label startLabel;

    @FXML
    private Label destinationLabel;

    @FXML
    private Label carNumberLabel;

    @FXML
    private Pane topLeftBar;

    @FXML
    private Pane topRightBar;

    @FXML
    private ImageView topLogo;

    @FXML
    private ImageView topClose;

    private double dragXOffset = 0;
    private double dragYOffset = 0;

    private DeliveryOrder currentSelectedOrder = null;
    private final List<OrderCard> currentOrderCards = new ArrayList<>();
    private volatile boolean isInitialMapLoaded = false;

    @FXML
    public void initialize() {
        driverMainPageInstance = this;
        StatusContainer.currentPageController = this;

        setupHeaderDrag();
        setupHorizontalScroll();
        showWorkplace();
        initProcessTipEvent();
        initInteractions();

        // 1. Determine active mission or initial selected order
        DeliveryOrder activeOrder = DeliveryOrderManager.getInstance().getActiveDispatchOrder();
        if (activeOrder == null && StatusContainer.activeDeliveryOrder != null) {
            activeOrder = StatusContainer.activeDeliveryOrder;
        }

        if (activeOrder != null) {
            currentSelectedOrder = activeOrder;
            initWebview();
            showActiveDispatchMode(activeOrder);
        } else {
            List<DeliveryOrder> orders = DeliveryOrderManager.getInstance().getAvailableOrders();
            if (orders.isEmpty()) {
                DeliveryOrderManager.getInstance().resetAll();
                orders = DeliveryOrderManager.getInstance().getAvailableOrders();
            }
            if (!orders.isEmpty()) {
                currentSelectedOrder = orders.get(0);
            }
            initWebview();
            showOrderSelectionMode();
        }
    }

    private void initWebview() {
        if (webview == null) return;
        WebEngine engine = webview.getEngine();
        engine.setJavaScriptEnabled(true);

        engine.setOnAlert(event -> {
            String message = event.getData();
            if ("OPEN_DRIVE_MODE".equals(message)) {
                Platform.runLater(() -> SceneManager.switchScene("drivingModePage.fxml"));
            }
        });

        // Set isInitialMapLoaded strictly without executing scripts during page transition
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                isInitialMapLoaded = true;
            }
        });

        double startLat = 16.4632;
        double startLng = 80.5078;
        String currentLoc = "SRM University-AP, Amaravati";
        double destLat = 16.5175;
        double destLng = 80.6200;
        String destination = "Vijayawada Junction (BZA Hub)";

        if (currentSelectedOrder != null) {
            startLat = currentSelectedOrder.getStartLat();
            startLng = currentSelectedOrder.getStartLng();
            currentLoc = currentSelectedOrder.getStartLocation();
            destLat = currentSelectedOrder.getDestLat();
            destLng = currentSelectedOrder.getDestLng();
            destination = currentSelectedOrder.getDestLocation();
        }

        loadDirectionsUrl(engine, startLat, startLng, destLat, destLng, currentLoc, destination);
    }

    private void loadDirectionsUrl(WebEngine engine, double sLat, double sLng, double dLat, double dLng, String sName, String dName) {
        URL resource = getClass().getResource("/com/example/car_rental_sys/html/directions.html");
        if (resource != null) {
            String url = resource.toExternalForm() + String.format(
                    Locale.US,
                    "?startLat=%.4f&startLng=%.4f&destLat=%.4f&destLng=%.4f&startName=%s&destName=%s",
                    sLat, sLng, dLat, dLng,
                    URLEncoder.encode(sName, StandardCharsets.UTF_8),
                    URLEncoder.encode(dName, StandardCharsets.UTF_8)
            );
            engine.load(url);
        }
    }

    private void updateMapRoute(double sLat, double sLng, double dLat, double dLng, String sName, String dName) {
        if (webview == null) return;
        WebEngine engine = webview.getEngine();
        loadDirectionsUrl(engine, sLat, sLng, dLat, dLng, sName, dName);
    }

    public void showOrderSelectionMode() {
        if (processTipPane != null) processTipPane.setVisible(false);
        if (scrollPane != null) {
            scrollPane.setVisible(true);
            refreshOrderCards();
        }
    }

    public void refreshOrderCards() {
        if (scrollPane == null) return;
        currentOrderCards.clear();

        List<DeliveryOrder> orders = DeliveryOrderManager.getInstance().getAvailableOrders();
        if (orders.isEmpty()) {
            DeliveryOrderManager.getInstance().resetAll();
            orders = DeliveryOrderManager.getInstance().getAvailableOrders();
        }

        if (currentSelectedOrder == null && !orders.isEmpty()) {
            currentSelectedOrder = orders.get(0);
        }

        HBox container = new HBox(16);
        container.setPadding(new Insets(10, 16, 10, 16));
        container.setAlignment(Pos.CENTER_LEFT);
        container.setStyle("-fx-background-color: transparent;");

        for (DeliveryOrder order : orders) {
            OrderCard card = new OrderCard(order, this::selectOrder, this::confirmOrderAcceptance, this::dismissOrder);
            if (currentSelectedOrder != null && card.getOrder().getOrderId() == currentSelectedOrder.getOrderId()) {
                card.setSelected(true);
            }
            currentOrderCards.add(card);
            container.getChildren().add(card);
        }

        scrollPane.setContent(container);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        if (currentSelectedOrder != null) {
            updateSidebarInfo(currentSelectedOrder);
        }
    }

    public void selectOrder(DeliveryOrder order) {
        if (order == null) return;
        this.currentSelectedOrder = order;

        for (OrderCard card : currentOrderCards) {
            card.setSelected(card.getOrder().getOrderId() == order.getOrderId());
        }

        updateSidebarInfo(order);

        updateMapRoute(order.getStartLat(), order.getStartLng(), order.getDestLat(), order.getDestLng(),
                order.getStartLocation(), order.getDestLocation());
    }

    public void confirmOrderAcceptance(DeliveryOrder order) {
        String msg = "Do you want to accept " + order.getRenterName() + "'s order?";
        MessageFrame confirmFrame = new MessageFrame(MessageFrameType.CONFIRM, msg, "Confirm Notification");
        confirmFrame.setSuccessCallbackFunc((i) -> {
            DeliveryOrderManager.getInstance().acceptOrder(order);
            StatusContainer.currentContinueOrderID = order.getOrderId();
            StatusContainer.activeDeliveryOrder = order;
            showActiveDispatchMode(order);
            new MessageFrame(MessageFrameType.SUCCESS, "Order accepted successfully!").show();
            return null;
        });
        confirmFrame.setFailedCallbackFunc((i) -> {
            confirmFrame.close();
            return null;
        });
        confirmFrame.show();
    }

    public void dismissOrder(DeliveryOrder order) {
        DeliveryOrderManager.getInstance().dismissOrder(order.getOrderId());
        if (currentSelectedOrder != null && currentSelectedOrder.getOrderId() == order.getOrderId()) {
            currentSelectedOrder = null;
        }
        refreshOrderCards();
    }

    public void showActiveDispatchMode(DeliveryOrder order) {
        if (order == null) return;
        this.currentSelectedOrder = order;

        if (scrollPane != null) scrollPane.setVisible(false);
        if (processTipPane != null) {
            processTipPane.setVisible(true);
            processTipPane.toFront();
        }
        if (processTipLabel != null) {
            if (order.isChauffeur()) {
                processTipLabel.setText("Chauffeur in service: Private driver assigned");
            } else {
                processTipLabel.setText("Trip in progress: Heading to deliver vehicle to client");
            }
        }

        updateSidebarInfo(order);

        updateMapRoute(order.getStartLat(), order.getStartLng(), order.getDestLat(), order.getDestLng(),
                order.getStartLocation(), order.getDestLocation());
    }

    private void updateSidebarInfo(DeliveryOrder order) {
        if (renterName != null) {
            String nameText = order.getRenterName();
            if (order.isDemo()) {
                nameText = "[DEMO] " + nameText;
            }
            renterName.setText(nameText);
        }
        if (renterPost != null) {
            String roleText = order.getRenterRole();
            if (order.isChauffeur()) {
                roleText += " • Chauffeur Assignment";
            } else {
                roleText += " • Self-Drive Delivery";
            }
            renterPost.setText(roleText);
        }
        if (carNumberLabel != null) carNumberLabel.setText(order.getCarNumber());
        if (seatsLabel != null) seatsLabel.setText(order.getSeats());
        if (powerLabel != null) powerLabel.setText(order.getEngine());
        if (speedLabel != null) speedLabel.setText(order.getTopSpeed());
        if (startLabel != null) startLabel.setText(order.getStartLocation());
        if (destinationLabel != null) destinationLabel.setText(order.getDestLocation());

        setImageFromClasspath(carImageView, "/com/example/car_rental_sys/image/cars/" + order.getCarModel() + ".png");
        setImageFromClasspath(renterAvatar, order.getRenterAvatar());
        ImageTools.setImageShapeToCircle(renterAvatar);
    }

    private void initProcessTipEvent() {
        if (processTipPane != null) {
            processTipPane.setOnMouseClicked(event -> {
                SceneManager.switchScene("drivingModePage.fxml");
            });
        }
        if (processImagePane != null) {
            processImagePane.setOnMouseClicked(event -> {
                SceneManager.switchScene("drivingModePage.fxml");
            });
        }
    }

    private void initInteractions() {
        if (messagePane != null) {
            messagePane.setOnMouseClicked(this::messageClick);
        }
        if (phonePane != null) {
            phonePane.setOnMouseClicked(this::phoneClick);
        }
        if (closeSideBarPane != null) {
            closeSideBarPane.setOnMouseClicked(event -> SceneManager.switchScene("mainPage.fxml"));
        }
    }

    public void showWorkplace() {
        if (optionPane != null) {
            optionPane.setVisible(false);
        }
        if (workplacePane != null) {
            workplacePane.setVisible(true);
            workplacePane.toFront();
        }
    }

    public void showSettingsView(String fxmlName) {
        if (workplacePane != null) {
            workplacePane.setVisible(false);
        }
        if (optionPane != null) {
            optionPane.setVisible(true);
            optionPane.toFront();
            if (settingMainPanel != null) {
                loadSubView(settingMainPanel, fxmlName);
            }
        }
    }

    public void showSettingPage() {
        showSettingsView("SettingPage.fxml");
        loadSubView(middlePanel, "MiddleBar.fxml");
    }

    public void showAccSecurity() {
        showSettingsView("AccountSecurityPage.fxml");
        loadSubView(middlePanel, "MiddleBar.fxml");
    }

    public void showPasswordPage() {
        showSettingsView("PasswordPage.fxml");
        loadSubView(middlePanel, "MiddleBar.fxml");
    }

    public void showProfilePage() {
        showSettingsView("PersonalInfoPage.fxml");
        loadSubView(middlePanel, "InfoMiddleBar.fxml");
    }

    private void loadSubView(Pane targetPane, String fxmlName) {
        if (targetPane == null) return;
        try {
            java.net.URL resource = getClass().getResource("/com/example/car_rental_sys/fxml/" + fxmlName);
            if (resource != null) {
                javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(resource);
                targetPane.getChildren().setAll(root);
            }
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(DriverMainPageController.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Failed to load sub-view: " + fxmlName, e);
        }
    }

    @FXML
    private void messageClick(MouseEvent event) {
        String clientName = (currentSelectedOrder != null) ? currentSelectedOrder.getRenterName() : "SAZAL GOREY";
        String clientPhone = (currentSelectedOrder != null) ? currentSelectedOrder.getRenterPhone() : "+91 93463 89758";
        // Launch WhatsApp Message chat directly
        PlatformTools.openWhatsAppMessage(clientPhone, clientName);
    }

    @FXML
    private void phoneClick(MouseEvent event) {
        String clientPhone = (currentSelectedOrder != null) ? currentSelectedOrder.getRenterPhone() : "+91 93463 89758";
        // Way 1: Launch Windows Phone Link / cellular telephony dialer (tel:)
        PlatformTools.openDialer(clientPhone);
    }

    @FXML
    private void closeSideBarPaneClick(MouseEvent event) {
        if (sidePane != null) {
            sidePane.setVisible(!sidePane.isVisible());
        }
    }

    @FXML
    private void logoClicked(MouseEvent event) {
        SceneManager.switchScene("mainPage.fxml");
    }

    @FXML
    private void closeIconClicked(MouseEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void closeHover(MouseEvent event) {
        setImageFromClasspath(topClose, "/com/example/car_rental_sys/image/UI/closeIconHover.png");
    }

    @FXML
    private void closeNormal(MouseEvent event) {
        setImageFromClasspath(topClose, "/com/example/car_rental_sys/image/UI/closeIconBlack.png");
    }

    private void setImageFromClasspath(ImageView imageView, String resourcePath) {
        if (imageView != null && resourcePath != null) {
            try (java.io.InputStream stream = getClass().getResourceAsStream(resourcePath)) {
                if (stream != null) {
                    imageView.setImage(new javafx.scene.image.Image(stream));
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void setupHorizontalScroll() {
        if (scrollPane == null) return;
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0 && event.getDeltaX() == 0) {
                double delta = -event.getDeltaY();
                double contentWidth = (scrollPane.getContent() != null)
                        ? scrollPane.getContent().getBoundsInLocal().getWidth()
                        : 0;
                double viewWidth = scrollPane.getViewportBounds().getWidth();
                double scrollable = contentWidth - viewWidth;
                if (scrollable > 0) {
                    double step = (delta * 2.5) / scrollable;
                    scrollPane.setHvalue(Math.max(0.0, Math.min(1.0, scrollPane.getHvalue() + step)));
                } else {
                    double step = delta > 0 ? 0.08 : -0.08;
                    scrollPane.setHvalue(Math.max(0.0, Math.min(1.0, scrollPane.getHvalue() + step)));
                }
                event.consume();
            }
        });
    }

    private void setupHeaderDrag() {
        setupDragOn(topLeftBar);
        setupDragOn(topRightBar);
    }

    private void setupDragOn(Pane pane) {
        if (pane == null) return;
        pane.setOnMousePressed(event -> {
            javafx.stage.Stage stage = SceneManager.getPrimaryStage();
            if (stage != null) {
                dragXOffset = stage.getX() - event.getScreenX();
                dragYOffset = stage.getY() - event.getScreenY();
            }
        });

        pane.setOnMouseDragged(event -> {
            javafx.stage.Stage stage = SceneManager.getPrimaryStage();
            if (stage != null) {
                stage.setX(event.getScreenX() + dragXOffset);
                stage.setY(event.getScreenY() + dragYOffset);
            }
        });
    }
}
