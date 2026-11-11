package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.StatusContainer;
import com.example.car_rental_sys.model.DeliveryOrder;
import com.example.car_rental_sys.service.DeliveryOrderManager;
import com.example.car_rental_sys.ui_components.MessageFrame;
import com.example.car_rental_sys.ui_components.MessageFrameType;
import com.example.car_rental_sys.util.AlertHelper;
import com.example.car_rental_sys.util.PlatformTools;
import com.example.car_rental_sys.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Controller for the Fullscreen In-Cabin Driver Cockpit Navigation Mode.
 */
public class DrivingModeController extends Controller {

    @FXML
    public Pane mainPane;

    @FXML
    private ImageView userAvatar;

    @FXML
    private Pane sideIconLogo;

    @FXML
    private Pane sideIconHome;

    @FXML
    private Pane sideIconVolume;

    @FXML
    private Pane sideIconPhone;

    @FXML
    private Pane sideIconSetting;

    @FXML
    private Pane browserContainer;

    @FXML
    private WebView cockpitWebView;

    @FXML
    private Pane distanceDataPane;

    @FXML
    private Pane progressBarTrack;

    @FXML
    private Pane progressBarFill;

    @FXML
    private ImageView progressIndicator;

    @FXML
    private Label distanceLeftLabel;

    @FXML
    private Label timeLeftLabel;

    // Interactive Cockpit Cards
    @FXML
    private Pane climateCardPane;
    @FXML
    private Pane climateIconBox;
    @FXML
    private ImageView climateIcon;
    @FXML
    private Label climateModeLabel;
    @FXML
    private Label climateTempLabel;

    @FXML
    private Pane seatCardPane;
    @FXML
    private Pane seatIconBox;
    @FXML
    private Label seatPresetLabel;
    @FXML
    private Label seatAngleLabel;

    @FXML
    private Pane ambientCardPane;
    @FXML
    private Pane ambientColorSwatch;
    @FXML
    private Label ambientModeLabel;
    @FXML
    private Label ambientColorNameLabel;

    // Interactive Cockpit State
    private int currentTemp = 24;
    private boolean isCoolMode = true;

    private int seatAngle = 36;
    private int seatWarmerLevel = 0; // 0=off, 1=warm (orange), 2=hot (red)
    private static final String[] PRESET_NAMES = {"SPORT", "COMFORT", "RELAX", "RECLINE"};
    private static final int[] PRESET_ANGLES = {30, 36, 42, 48};
    private int currentPresetIndex = 1; // COMFORT

    private static class AmbientTheme {
        final String name;
        final String hex;
        final String glowRgba;

        AmbientTheme(String name, String hex, String glowRgba) {
            this.name = name;
            this.hex = hex;
            this.glowRgba = glowRgba;
        }
    }

    private static final AmbientTheme[] AMBIENT_THEMES = new AmbientTheme[] {
        new AmbientTheme("Ice Blue", "#0061ef", "rgba(0, 97, 239, 0.45)"),
        new AmbientTheme("Emerald", "#00e676", "rgba(0, 230, 118, 0.45)"),
        new AmbientTheme("Amethyst", "#a855f7", "rgba(168, 85, 247, 0.45)"),
        new AmbientTheme("Amber", "#ff9100", "rgba(255, 145, 0, 0.45)"),
        new AmbientTheme("Crimson", "#ff1744", "rgba(255, 23, 68, 0.45)"),
        new AmbientTheme("Pure White", "#e2e8f0", "rgba(226, 232, 240, 0.45)")
    };
    private int currentAmbientIndex = 0;

    @FXML
    public void initialize() {
        StatusContainer.currentPageController = this;
        if (distanceLeftLabel != null) distanceLeftLabel.setText("21.6 km left");
        if (timeLeftLabel != null) timeLeftLabel.setText("28 mins");
        updateProgressBar(0.0);
        initCockpitMap();
        initInteractiveCards();
    }

    private void initCockpitMap() {
        if (cockpitWebView == null) return;

        // Clip WebView with smooth rounded corners matching cockpit panel
        Rectangle clip = new Rectangle(300, 490);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        cockpitWebView.setClip(clip);

        WebEngine engine = cockpitWebView.getEngine();
        engine.setJavaScriptEnabled(true);

        DeliveryOrder activeOrder = DeliveryOrderManager.getInstance().getActiveDispatchOrder();
        if (activeOrder == null && StatusContainer.activeDeliveryOrder != null) {
            activeOrder = StatusContainer.activeDeliveryOrder;
        }

        double startLat = 16.4632;
        double startLng = 80.5078;
        String currentLoc = "SRM University-AP, Amaravati";
        double destLat = 16.5175;
        double destLng = 80.6200;
        String destination = "Vijayawada Junction (BZA Hub)";

        if (activeOrder != null) {
            startLat = activeOrder.getStartLat();
            startLng = activeOrder.getStartLng();
            currentLoc = activeOrder.getStartLocation();
            destLat = activeOrder.getDestLat();
            destLng = activeOrder.getDestLng();
            destination = activeOrder.getDestLocation();
        } else {
            var cached = com.example.car_rental_sys.service.ServiceFactory.getInstance().getGeoLocationService().getCachedLocation();
            if (cached != null && cached.success() && cached.latitude() != 0 && cached.longitude() != 0) {
                currentLoc = cached.city() + ", " + cached.regionName();
                startLat = cached.latitude();
                startLng = cached.longitude();
            }
        }

        final String finalDest = destination;

        // Telemetry bridge listener from in-map car drive events
        engine.setOnAlert(event -> {
            String data = event.getData();
            if (data == null) return;

            if (data.startsWith("STEP:")) {
                // Format: STEP:18.4 km:22:15
                String[] parts = data.split(":");
                if (parts.length >= 4) {
                    final String dStr = parts[1];
                    final String tStr = parts[2];
                    final String pctStr = parts[3];
                    Platform.runLater(() -> {
                        if (distanceLeftLabel != null) distanceLeftLabel.setText(dStr + " left");
                        if (timeLeftLabel != null) timeLeftLabel.setText(tStr + " mins");
                        try {
                            double pct = Double.parseDouble(pctStr) / 100.0;
                            updateProgressBar(pct);
                        } catch (Exception ignored) {}
                    });
                }
            } else if ("ARRIVED".equals(data)) {
                Platform.runLater(() -> {
                    if (distanceLeftLabel != null) distanceLeftLabel.setText("0 km left");
                    if (timeLeftLabel != null) timeLeftLabel.setText("Arrived");
                    updateProgressBar(1.0);
                    AlertHelper.showInfo("Destination Reached", "You have arrived at the passenger pickup point:\n" + finalDest);
                });
            }
        });

        loadCockpitUrl(engine, startLat, startLng, destLat, destLng, currentLoc, destination);
    }

    private void loadCockpitUrl(WebEngine engine, double sLat, double sLng, double dLat, double dLng, String sName, String dName) {
        URL resource = getClass().getResource("/com/example/car_rental_sys/html/cockpitMap.html");
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

    private void updateProgressBar(double ratio) {
        if (progressBarFill != null && progressBarTrack != null) {
            double maxWidth = progressBarTrack.getPrefWidth() > 0 ? progressBarTrack.getPrefWidth() : 222.0;
            double fillWidth = Math.max(12.0, Math.min(maxWidth, maxWidth * ratio));
            progressBarFill.setPrefWidth(fillWidth);
            if (progressIndicator != null) {
                progressIndicator.setLayoutX(fillWidth - 14.0);
            }
        }
    }

    @FXML
    private void userAvatarClicked(MouseEvent event) {
        SceneManager.switchScene("PersonalInfoPage.fxml");
    }

    @FXML
    private void backToMainPage(MouseEvent event) {
        SceneManager.switchScene("mainPage.fxml");
    }

    @FXML
    private void backToDriverMainPage(MouseEvent event) {
        SceneManager.switchScene("driverServicePage.fxml");
    }

    @FXML
    private void phoneIconClicked(MouseEvent event) {
        DeliveryOrder activeOrder = DeliveryOrderManager.getInstance().getActiveDispatchOrder();
        String clientPhone = (activeOrder != null) ? activeOrder.getRenterPhone() : "+91 93463 89758";
        // Way 1: Launch Windows Phone Link / cellular dialer (tel:)
        PlatformTools.openDialer(clientPhone);
    }

    @FXML
    private void finishDeliveryClicked(MouseEvent event) {
        DeliveryOrder activeOrder = DeliveryOrderManager.getInstance().getActiveDispatchOrder();
        boolean isChauffeur = (activeOrder != null && activeOrder.isChauffeur());

        String confirmMsg = isChauffeur
                ? "Finish chauffeur duty? This concludes all rides for the client and marks the rental complete."
                : "Handover vehicle to customer? This completes vehicle delivery and marks car on-rent.";

        MessageFrame messageFrame = new MessageFrame(MessageFrameType.CONFIRM, confirmMsg, "Confirm Action");
        messageFrame.setSuccessCallbackFunc((i) -> {
            DeliveryOrderManager.getInstance().completeActiveDelivery();
            MusicPlayerController.stopPlayback();
            StatusContainer.currentContinueOrderID = -1;
            StatusContainer.activeDeliveryOrder = null;
            messageFrame.close();
            SceneManager.switchScene("driverServicePage.fxml");
            return null;
        });
        messageFrame.setFailedCallbackFunc((i) -> {
            messageFrame.close();
            return null;
        });
        messageFrame.show();
    }

    @FXML
    private void backToSettingPage(MouseEvent event) {
        SceneManager.switchScene("SettingPage.fxml");
    }

    // ==========================================
    // Interactive Cockpit Cards Logic
    // ==========================================

    @FXML
    private void tempUpClicked(MouseEvent event) {
        if (event != null) event.consume();
        if (currentTemp < 30) {
            currentTemp++;
            updateClimateUI();
        }
    }

    @FXML
    private void tempDownClicked(MouseEvent event) {
        if (event != null) event.consume();
        if (currentTemp > 16) {
            currentTemp--;
            updateClimateUI();
        }
    }

    @FXML
    private void climateIconBoxClicked(MouseEvent event) {
        if (event != null) event.consume();
        isCoolMode = !isCoolMode;
        updateClimateUI();
    }

    private void updateClimateUI() {
        if (climateTempLabel != null) {
            climateTempLabel.setText(currentTemp + " ℃");
        }
        if (climateModeLabel != null) {
            climateModeLabel.setText(isCoolMode ? "A/C AUTO" : "A/C HEAT");
            climateModeLabel.setTextFill(javafx.scene.paint.Color.web(isCoolMode ? "#00d2ff" : "#ff9100"));
        }
        if (climateIconBox != null) {
            String bg = isCoolMode ? "#1a3b47" : "#4a331a";
            climateIconBox.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 15; -fx-cursor: hand;");
        }
    }

    @FXML
    private void seatUpClicked(MouseEvent event) {
        if (event != null) event.consume();
        if (seatAngle > 26) {
            seatAngle -= 2;
            updateSeatAngleDisplay();
        }
    }

    @FXML
    private void seatDownClicked(MouseEvent event) {
        if (event != null) event.consume();
        if (seatAngle < 52) {
            seatAngle += 2;
            updateSeatAngleDisplay();
        }
    }

    @FXML
    private void seatCardClicked(MouseEvent event) {
        currentPresetIndex = (currentPresetIndex + 1) % PRESET_ANGLES.length;
        seatAngle = PRESET_ANGLES[currentPresetIndex];
        updateSeatAngleDisplay();
    }

    @FXML
    private void seatWarmerClicked(MouseEvent event) {
        if (event != null) event.consume();
        seatWarmerLevel = (seatWarmerLevel + 1) % 3;
        updateSeatWarmerUI();
    }

    private void updateSeatAngleDisplay() {
        if (seatAngleLabel != null) {
            seatAngleLabel.setText(seatAngle + " °");
        }
        if (seatPresetLabel != null) {
            String preset = "CUSTOM";
            for (int i = 0; i < PRESET_ANGLES.length; i++) {
                if (PRESET_ANGLES[i] == seatAngle) {
                    preset = PRESET_NAMES[i];
                    currentPresetIndex = i;
                    break;
                }
            }
            seatPresetLabel.setText(preset);
            seatPresetLabel.setTextFill(javafx.scene.paint.Color.web("CUSTOM".equals(preset) ? "#aaaaaa" : "#ffb84d"));
        }
    }

    private void updateSeatWarmerUI() {
        if (seatIconBox != null) {
            if (seatWarmerLevel == 0) {
                seatIconBox.setStyle("-fx-background-color: #434b53; -fx-background-radius: 15; -fx-cursor: hand;");
            } else if (seatWarmerLevel == 1) {
                seatIconBox.setStyle("-fx-background-color: #633e14; -fx-background-radius: 15; -fx-cursor: hand; -fx-border-color: #ff9100; -fx-border-radius: 15; -fx-border-width: 1.5;");
            } else {
                seatIconBox.setStyle("-fx-background-color: #6b1f1f; -fx-background-radius: 15; -fx-cursor: hand; -fx-border-color: #ff3344; -fx-border-radius: 15; -fx-border-width: 1.5;");
            }
        }
    }

    @FXML
    private void ambientCardClicked(MouseEvent event) {
        currentAmbientIndex = (currentAmbientIndex + 1) % AMBIENT_THEMES.length;
        updateAmbientUI();
    }

    private void updateAmbientUI() {
        AmbientTheme theme = AMBIENT_THEMES[currentAmbientIndex];
        if (ambientColorNameLabel != null) {
            ambientColorNameLabel.setText(theme.name);
        }
        if (ambientColorSwatch != null) {
            ambientColorSwatch.setStyle("-fx-background-color: " + theme.hex + "; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, " + theme.glowRgba + ", 16, 0.2, 0, 0);");
        }
        if (ambientModeLabel != null) {
            ambientModeLabel.setTextFill(javafx.scene.paint.Color.web(theme.hex));
        }

        // Dynamically tint card borders to match the selected ambient lighting theme!
        String cardBorder = "-fx-background-color: #292d32; -fx-background-radius: 30; -fx-border-color: " + theme.glowRgba + "; -fx-border-radius: 30; -fx-border-width: 1.2;";
        if (climateCardPane != null) climateCardPane.setStyle(cardBorder);
        if (seatCardPane != null) seatCardPane.setStyle(cardBorder + " -fx-cursor: hand;");
        if (ambientCardPane != null) ambientCardPane.setStyle(cardBorder + " -fx-cursor: hand;");
    }

    private void initInteractiveCards() {
        updateClimateUI();
        updateSeatAngleDisplay();
        updateSeatWarmerUI();
        updateAmbientUI();

        if (climateCardPane != null) {
            climateCardPane.setOnScroll(scrollEvent -> {
                if (scrollEvent.getDeltaY() > 0) tempUpClicked(null);
                else if (scrollEvent.getDeltaY() < 0) tempDownClicked(null);
                scrollEvent.consume();
            });
        }
        if (seatCardPane != null) {
            seatCardPane.setOnScroll(scrollEvent -> {
                if (scrollEvent.getDeltaY() > 0) seatUpClicked(null);
                else if (scrollEvent.getDeltaY() < 0) seatDownClicked(null);
                scrollEvent.consume();
            });
        }
    }
}
