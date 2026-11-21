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

import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Payment Success Confirmation Screen.
 * Plays the celebratory supercar animation video with native audio for all car bookings,
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

    // Static references to guarantee single-instance playback and prevent Windows Media Foundation lockouts
    private static MediaPlayer activeVideoPlayer;

    private MediaPlayer videoPlayer;
    private PauseTransition autoNavTimer;
    private boolean isNavigating = false;
    private boolean hasRetried = false;
    private long screenLoadTime = 0L;

    /**
     * Globally halts and disposes any active video or audio players to release hardware decoders.
     */
    public static synchronized void cleanupActivePlayers() {
        if (activeVideoPlayer != null) {
            try {
                activeVideoPlayer.stop();
                activeVideoPlayer.dispose();
                LOGGER.info("Active celebration video player successfully disposed.");
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "Error disposing active video player: " + t.getMessage(), t);
            }
            activeVideoPlayer = null;
        }
    }

    @FXML
    public void initialize() {
        screenLoadTime = System.currentTimeMillis();
        populateOrderSummary();
        bindMediaViewDimensions();
        playCelebrationVideo();
        setupKeyHandlers();
    }

    private void bindMediaViewDimensions() {
        Platform.runLater(() -> {
            if (mediaView != null && mainPane != null) {
                mediaView.fitWidthProperty().bind(mainPane.widthProperty());
                mediaView.fitHeightProperty().bind(mainPane.heightProperty());
            }
        });
    }

    private void populateOrderSummary() {
        try {
            Order order = OrderDetailsComponentController.getSelectedOrder();
            if (order != null) {
                if (orderTitleLabel != null) {
                    orderTitleLabel.setText(String.format("Order #%09d • %s Confirmed!",
                            order.getOrderId(),
                            order.getRentalMode() == com.example.car_rental_sys.model.RentalMode.WITH_DRIVER ? "Chauffeur" : "Self-Drive"));
                }
                if (orderSubtitleLabel != null) {
                    String carName = order.getVehicleModel();
                    if (carName != null && !carName.isBlank()) {
                        orderSubtitleLabel.setText(String.format("Your %s is reserved! Telemetry & dispatch route initialized.", carName));
                    } else {
                        orderSubtitleLabel.setText("Your supercar reservation is confirmed! Driver dispatch is in progress.");
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private String resolveVideoSource(boolean useSecondary) {
        String primaryName = useSecondary ? "video.mp4" : "video2.mp4";
        String secondaryName = useSecondary ? "video2.mp4" : "video.mp4";

        File[] candidateFiles = new File[]{
                new File("src/main/resources/com/example/car_rental_sys/html/paySuccess/" + primaryName),
                new File("Fleet_Managemnet_System/src/main/resources/com/example/car_rental_sys/html/paySuccess/" + primaryName),
                new File("target/classes/com/example/car_rental_sys/html/paySuccess/" + primaryName),
                new File("Fleet_Managemnet_System/target/classes/com/example/car_rental_sys/html/paySuccess/" + primaryName),
                new File("src/main/resources/com/example/car_rental_sys/html/paySuccess/" + secondaryName),
                new File("Fleet_Managemnet_System/src/main/resources/com/example/car_rental_sys/html/paySuccess/" + secondaryName)
        };

        for (File candidate : candidateFiles) {
            if (candidate.exists() && candidate.isFile()) {
                return candidate.toURI().toString();
            }
        }

        URL res = getClass().getResource("/com/example/car_rental_sys/html/paySuccess/" + primaryName);
        if (res == null) {
            res = getClass().getResource("/com/example/car_rental_sys/html/paySuccess/" + secondaryName);
        }
        return res != null ? res.toExternalForm() : null;
    }

    private void playCelebrationVideo() {
        try {
            cleanupActivePlayers();

            String mediaUri = resolveVideoSource(false);
            if (mediaUri != null) {
                Media media = new Media(mediaUri);
                activeVideoPlayer = new MediaPlayer(media);
                videoPlayer = activeVideoPlayer;

                if (mediaView != null) {
                    mediaView.setMediaPlayer(videoPlayer);
                }
                videoPlayer.setVolume(1.0);
                videoPlayer.setAutoPlay(true);

                Runnable startPlayback = () -> {
                    LOGGER.info("Celebration video loaded successfully, starting playback with audio.");
                    if (videoPlayer != null) {
                        videoPlayer.setVolume(1.0);
                        videoPlayer.seek(Duration.ZERO);
                        videoPlayer.play();
                    }
                };

                videoPlayer.setOnReady(startPlayback);
                if (videoPlayer.getStatus() == MediaPlayer.Status.READY) {
                    startPlayback.run();
                }

                videoPlayer.setOnEndOfMedia(() -> {
                    LOGGER.info("Celebration video playback ended.");
                    showCelebrationCard();
                    scheduleAutoNav(5);
                });

                videoPlayer.setOnError(() -> {
                    LOGGER.warning("Celebration video playback encountered error: " +
                            (videoPlayer.getError() != null ? videoPlayer.getError().getMessage() : "unknown"));
                    if (!hasRetried) {
                        hasRetried = true;
                        Platform.runLater(this::retryVideoPlayback);
                    } else {
                        showCelebrationCard();
                        scheduleAutoNav(8);
                    }
                });

                // Global maximum safety timer (22s) so customer is never stuck
                autoNavTimer = new PauseTransition(Duration.seconds(22));
                autoNavTimer.setOnFinished(e -> handleViewOrders());
                autoNavTimer.play();
            } else {
                LOGGER.warning("Could not resolve celebration video source file.");
                showCelebrationCard();
                scheduleAutoNav(8);
            }
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Failed to initialize native video player: " + t.getMessage(), t);
            if (!hasRetried) {
                hasRetried = true;
                Platform.runLater(this::retryVideoPlayback);
            } else {
                showCelebrationCard();
                scheduleAutoNav(8);
            }
        }
    }

    private void retryVideoPlayback() {
        try {
            cleanupActivePlayers();
            String fallbackUri = resolveVideoSource(true);
            if (fallbackUri != null) {
                Media media = new Media(fallbackUri);
                activeVideoPlayer = new MediaPlayer(media);
                videoPlayer = activeVideoPlayer;
                if (mediaView != null) {
                    mediaView.setMediaPlayer(videoPlayer);
                }
                videoPlayer.setAutoPlay(true);
                videoPlayer.setVolume(1.0);
                videoPlayer.setOnReady(() -> {
                    if (videoPlayer != null) {
                        videoPlayer.seek(Duration.ZERO);
                        videoPlayer.play();
                    }
                });
                if (videoPlayer.getStatus() == MediaPlayer.Status.READY) {
                    videoPlayer.play();
                }
                videoPlayer.setOnEndOfMedia(() -> {
                    showCelebrationCard();
                    scheduleAutoNav(5);
                });
                videoPlayer.setOnError(() -> {
                    showCelebrationCard();
                    scheduleAutoNav(8);
                });
            } else {
                showCelebrationCard();
                scheduleAutoNav(8);
            }
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Retry celebration video failed: " + t.getMessage(), t);
            showCelebrationCard();
            scheduleAutoNav(8);
        }
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

        // Clean up media players to release audio hardware
        cleanupActivePlayers();

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
