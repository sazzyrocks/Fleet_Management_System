package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.model.Order;
import com.example.car_rental_sys.util.SceneManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Payment Success Confirmation Screen.
 * Plays the celebratory supercar animation video with native audio,
 * displays reservation confirmation telemetry, and auto-navigates to customer orders.
 */
public class PaySuccessPageController extends Controller {

    private static final Logger LOGGER = Logger.getLogger(PaySuccessPageController.class.getName());

    @FXML public Pane mainPane;
    @FXML private MediaView mediaView;
    @FXML private VBox celebrationCard;
    @FXML private ImageView successIcon;
    @FXML private Label orderTitleLabel;
    @FXML private Label orderSubtitleLabel;
    @FXML private Button btnViewOrders;
    @FXML private Button btnSkip;

    private MediaPlayer videoPlayer;
    private MediaPlayer musicPlayer;
    private PauseTransition autoNavTimer;
    private boolean isNavigating = false;
    private long screenLoadTime = 0L;

    @FXML
    public void initialize() {
        screenLoadTime = System.currentTimeMillis();
        populateOrderSummary();
        playCelebrationVideo();
        setupKeyHandlers();
    }

    private void populateOrderSummary() {
        try {
            Order order = OrderDetailsComponentController.getSelectedOrder();
            if (order != null && orderTitleLabel != null) {
                orderTitleLabel.setText(String.format("Order #%09d • %s Confirmed!",
                        order.getOrderId(),
                        order.getRentalMode() == com.example.car_rental_sys.model.RentalMode.WITH_DRIVER ? "Chauffeur" : "Self-Drive"));
            }
        } catch (Exception ignored) {}
    }

    private void playCelebrationVideo() {
        try {
            URL videoUrl = getClass().getResource("/com/example/car_rental_sys/html/paySuccess/video2.mp4");
            if (videoUrl == null) {
                videoUrl = getClass().getResource("/com/example/car_rental_sys/html/paySuccess/video.mp4");
            }

            if (videoUrl != null) {
                Media media = new Media(videoUrl.toExternalForm());
                videoPlayer = new MediaPlayer(media);
                if (mediaView != null) {
                    mediaView.setMediaPlayer(videoPlayer);
                }
                videoPlayer.setVolume(1.0);
                videoPlayer.setAutoPlay(false);

                videoPlayer.setOnReady(() -> {
                    LOGGER.info("Celebration video loaded successfully, starting playback with audio.");
                    if (videoPlayer != null) {
                        videoPlayer.seek(Duration.ZERO);
                        videoPlayer.play();
                    }
                });

                videoPlayer.setOnEndOfMedia(() -> {
                    LOGGER.info("Celebration video playback ended.");
                    if (videoPlayer != null && videoPlayer.getCurrentTime().toSeconds() < 2.0) {
                        LOGGER.warning("Ignoring premature onEndOfMedia event");
                        return;
                    }
                    showCelebrationCard();
                    scheduleAutoNav(5);
                });

                videoPlayer.setOnError(() -> {
                    LOGGER.warning("Native video playback encountered error: " +
                            (videoPlayer.getError() != null ? videoPlayer.getError().getMessage() : "unknown"));
                    fallbackToAudioAndCard();
                });

                // Global maximum safety timer (22s) so customer is never stuck
                autoNavTimer = new PauseTransition(Duration.seconds(22));
                autoNavTimer.setOnFinished(e -> handleViewOrders());
                autoNavTimer.play();
            } else {
                fallbackToAudioAndCard();
            }
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Failed to initialize native video player: " + t.getMessage(), t);
            fallbackToAudioAndCard();
        }
    }

    private void fallbackToAudioAndCard() {
        showCelebrationCard();

        // Play celebration audio track fallback
        try {
            URL musicUrl = getClass().getResource("/com/example/car_rental_sys/music/Good_Life-G_Eazy&Kehlani.mp3");
            if (musicUrl != null) {
                Media audioMedia = new Media(musicUrl.toExternalForm());
                musicPlayer = new MediaPlayer(audioMedia);
                musicPlayer.setVolume(0.9);
                musicPlayer.play();
            }
        } catch (Throwable ignored) {}

        scheduleAutoNav(6);
    }

    private void showCelebrationCard() {
        Platform.runLater(() -> {
            if (celebrationCard != null && !celebrationCard.isVisible()) {
                celebrationCard.setOpacity(0.0);
                celebrationCard.setScaleX(0.85);
                celebrationCard.setScaleY(0.85);
                celebrationCard.setVisible(true);

                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(500), celebrationCard);
                ft.setToValue(1.0);

                javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(Duration.millis(500), celebrationCard);
                st.setToX(1.0);
                st.setToY(1.0);

                javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(ft, st);
                pt.play();
            }
        });
    }

    private void scheduleAutoNav(int seconds) {
        if (autoNavTimer != null) autoNavTimer.stop();
        autoNavTimer = new PauseTransition(Duration.seconds(seconds));
        autoNavTimer.setOnFinished(e -> handleViewOrders());
        autoNavTimer.play();
    }

    @FXML
    public void handleViewOrders() {
        if (isNavigating) return;
        // Minimum display guard: Prevent accidental instant skips from leftover keys or clicks
        if (System.currentTimeMillis() - screenLoadTime < 1800) {
            LOGGER.info("Skipping premature navigation request (minimum display guard active)");
            return;
        }
        isNavigating = true;

        if (autoNavTimer != null) {
            autoNavTimer.stop();
        }

        // Clean up media players to release audio hardware and stop background sounds
        if (videoPlayer != null) {
            try {
                videoPlayer.stop();
                videoPlayer.dispose();
            } catch (Throwable ignored) {}
            videoPlayer = null;
        }

        if (musicPlayer != null) {
            try {
                musicPlayer.stop();
                musicPlayer.dispose();
            } catch (Throwable ignored) {}
            musicPlayer = null;
        }

        // Session guard: Prevent verification dialog loop when returning to customer orders
        CustomerServiceController.hasPromptedVerificationThisSession = true;

        Platform.runLater(() -> SceneManager.switchScene("customerServicePage.fxml"));
    }

    private void setupKeyHandlers() {
        Platform.runLater(() -> {
            if (mainPane != null) {
                mainPane.setFocusTraversable(true);
                mainPane.requestFocus();
                mainPane.setOnKeyPressed(event -> {
                    // Do NOT allow ENTER to skip (ENTER is used to submit payment)
                    if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.SPACE) {
                        handleViewOrders();
                    }
                });
            }
        });
    }
}
