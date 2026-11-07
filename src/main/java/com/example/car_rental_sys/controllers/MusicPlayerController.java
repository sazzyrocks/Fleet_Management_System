package com.example.car_rental_sys.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for In-Vehicle Infotainment Media and Music Player.
 * Maintains persistent singleton playback state to prevent audio duplication
 * and keep song progress perfectly in sync across cockpit view navigation.
 */
public class MusicPlayerController {

    @FXML
    private ImageView backwardIcon, previousIcon, pauseIcon, nextIcon, forwardIcon;

    @FXML
    private Pane musicCoverPane, processBarPane;

    @FXML
    private Label songsterLabel, songNameLabel, playedTimeLabel, totalTimeLabel;

    // Singleton playback state across scene navigations
    private static MediaPlayer sharedPlayer;
    private static int currentSongIndex = 0;
    private static double currentProgressSeconds = 0;
    private static double totalDurationSeconds = 210;
    private static boolean isPlaying = false;
    private static final List<TrackInfo> playlist = new ArrayList<>();
    private static Timeline progressTimeline;
    private static MusicPlayerController activeInstance;

    private static class TrackInfo {
        final String title;
        final String artist;
        final String filePath;
        final String coverPath;

        TrackInfo(String title, String artist, String filePath, String coverPath) {
            this.title = title;
            this.artist = artist;
            this.filePath = filePath;
            this.coverPath = coverPath;
        }
    }

    @FXML
    private void initialize() {
        activeInstance = this;
        if (playlist.isEmpty()) {
            discoverTracks();
        }
        initProgressBarClick();
        initProgressTimeline();
        syncUI();
    }

    /**
     * Pauses playback when user navigates away from the cockpit.
     */
    public static synchronized void pausePlayback() {
        isPlaying = false;
        if (sharedPlayer != null) {
            try {
                sharedPlayer.pause();
            } catch (Exception ignored) {
            }
        }
        if (activeInstance != null) {
            activeInstance.setPlayIcon(false);
        }
    }

    /**
     * Stops and releases media resources completely.
     */
    public static synchronized void stopPlayback() {
        isPlaying = false;
        currentProgressSeconds = 0;
        if (sharedPlayer != null) {
            try {
                sharedPlayer.stop();
                sharedPlayer.dispose();
            } catch (Exception ignored) {
            }
            sharedPlayer = null;
        }
        if (activeInstance != null) {
            activeInstance.setPlayIcon(false);
            activeInstance.resetProgressBar();
            activeInstance.updateDisplayTimes();
        }
    }

    private void syncUI() {
        if (playlist.isEmpty()) return;

        TrackInfo track = playlist.get(currentSongIndex % playlist.size());
        if (songsterLabel != null) {
            songsterLabel.setText(track.title);
        }
        if (songNameLabel != null) {
            songNameLabel.setText(track.artist);
        }

        renderCover(track);
        updateDisplayTimes();
        updateProgressBar();
        setPlayIcon(isPlaying);

        // If player is not initialized yet, initialize it in paused state
        if (sharedPlayer == null && track.filePath != null) {
            preparePlayer(track, false);
        }
    }

    private void discoverTracks() {
        playlist.clear();

        File musicDir = null;
        File[] candidateDirs = new File[] {
            new File("src/main/resources/com/example/car_rental_sys/music"),
            new File("Fleet_Managemnet_System/src/main/resources/com/example/car_rental_sys/music"),
            new File("target/classes/com/example/car_rental_sys/music"),
            new File("Fleet_Managemnet_System/target/classes/com/example/car_rental_sys/music")
        };

        for (File dir : candidateDirs) {
            if (dir.exists() && dir.isDirectory()) {
                musicDir = dir;
                break;
            }
        }

        if (musicDir != null) {
            File[] files = musicDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
            if (files != null && files.length > 0) {
                java.util.Arrays.sort(files, java.util.Comparator.comparing(File::getName));
                for (File file : files) {
                    String fullPath = file.getAbsolutePath();
                    String fileName = file.getName();
                    int lastDot = fileName.lastIndexOf('.');
                    String baseName = (lastDot > 0) ? fileName.substring(0, lastDot) : fileName;

                    // Automatically check for matching cover image (.png, .jpg, .jpeg)
                    String coverPath = null;
                    File parentDir = file.getParentFile();
                    for (String ext : new String[]{".png", ".jpg", ".jpeg", ".PNG", ".JPG"}) {
                        File candidateCover = new File(parentDir, baseName + ext);
                        if (candidateCover.exists() && candidateCover.isFile()) {
                            coverPath = candidateCover.getAbsolutePath();
                            break;
                        }
                    }

                    // Format: "Song_Name-Artist.mp3"
                    String songTitle;
                    String artist;
                    if (baseName.contains("-")) {
                        String[] parts = baseName.split("-", 2);
                        songTitle = parts[0].replace("_", " ").trim();
                        artist = parts[1].replace("_", " ").trim();
                    } else {
                        songTitle = baseName.replace("_", " ").trim();
                        artist = "Fleet Audio";
                    }

                    playlist.add(new TrackInfo(songTitle, artist, fullPath, coverPath));
                }
            }
        }

        if (playlist.isEmpty()) {
            playlist.add(new TrackInfo("Fleet Radio Station", "Ambient Cruiser", null, null));
            playlist.add(new TrackInfo("Highway Horizon", "Electric Pulse", null, null));
            playlist.add(new TrackInfo("Smooth Commute", "Urban Groove", null, null));
        }
    }

    private void preparePlayer(TrackInfo track, boolean startPlaying) {
        if (sharedPlayer != null) {
            try {
                sharedPlayer.stop();
                sharedPlayer.dispose();
            } catch (Exception ignored) {
            }
            sharedPlayer = null;
        }

        if (track.filePath == null) return;

        try {
            Media media = new Media(new File(track.filePath).toURI().toString());
            sharedPlayer = new MediaPlayer(media);
            sharedPlayer.setOnReady(() -> {
                Duration duration = sharedPlayer.getMedia().getDuration();
                if (duration != null && !duration.isUnknown()) {
                    totalDurationSeconds = duration.toSeconds();
                    updateDisplayTimes();
                }
                if (currentProgressSeconds > 0) {
                    sharedPlayer.seek(Duration.seconds(currentProgressSeconds));
                }
            });
            sharedPlayer.setOnEndOfMedia(this::nextClickEvent);

            if (startPlaying) {
                isPlaying = true;
                sharedPlayer.play();
                setPlayIcon(true);
            }
        } catch (Exception e) {
            totalDurationSeconds = 210;
        }
    }

    private void loadTrack(int index) {
        if (playlist.isEmpty()) {
            return;
        }
        currentSongIndex = index % playlist.size();
        currentProgressSeconds = 0;
        TrackInfo track = playlist.get(currentSongIndex);
        syncUI();
        preparePlayer(track, isPlaying);
    }

    private void renderCover(TrackInfo track) {
        if (musicCoverPane == null) return;
        musicCoverPane.getChildren().clear();
        ImageView coverView = null;

        if (track.coverPath != null) {
            try {
                File coverFile = new File(track.coverPath);
                if (coverFile.exists()) {
                    Image img = new Image(coverFile.toURI().toString());
                    coverView = new ImageView(img);
                }
            } catch (Exception ignored) {
            }
        }

        if (coverView == null) {
            try {
                String baseName = track.title.replace(" ", "_") + "-" + track.artist.replace(" ", "_") + ".png";
                URL res = getClass().getResource("/com/example/car_rental_sys/music/" + baseName);
                if (res != null) {
                    coverView = new ImageView(new Image(res.toExternalForm()));
                }
            } catch (Exception ignored) {
            }
        }

        if (coverView == null) {
            try {
                URL res = getClass().getResource("/com/example/car_rental_sys/image/UI/car.png");
                if (res != null) {
                    coverView = new ImageView(new Image(res.toExternalForm()));
                }
            } catch (Exception ignored) {
            }
        }

        if (coverView != null) {
            coverView.setFitWidth(90.0);
            coverView.setFitHeight(90.0);
            coverView.setPreserveRatio(false);

            Rectangle clip = new Rectangle(90.0, 90.0);
            clip.setArcWidth(30.0);
            clip.setArcHeight(30.0);
            coverView.setClip(clip);

            musicCoverPane.getChildren().add(coverView);
        }
    }

    private void initProgressTimeline() {
        if (progressTimeline != null) {
            progressTimeline.stop();
        }
        progressTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (isPlaying) {
                if (sharedPlayer != null) {
                    currentProgressSeconds = sharedPlayer.getCurrentTime().toSeconds();
                } else {
                    currentProgressSeconds += 1.0;
                    if (currentProgressSeconds >= totalDurationSeconds) {
                        nextClickEvent();
                        return;
                    }
                }
                updateDisplayTimes();
                updateProgressBar();
            }
        }));
        progressTimeline.setCycleCount(Animation.INDEFINITE);
        progressTimeline.play();
    }

    private void updateDisplayTimes() {
        if (playedTimeLabel != null) {
            playedTimeLabel.setText(formatSeconds(currentProgressSeconds));
        }
        if (totalTimeLabel != null) {
            totalTimeLabel.setText(formatSeconds(totalDurationSeconds));
        }
    }

    private void updateProgressBar() {
        if (processBarPane != null && totalDurationSeconds > 0) {
            double maxWidth = 280.0;
            if (processBarPane.getParent() instanceof Pane) {
                double parentW = ((Pane) processBarPane.getParent()).getPrefWidth();
                if (parentW > 0) maxWidth = parentW;
            }
            double progress = Math.max(0.0, Math.min(1.0, currentProgressSeconds / totalDurationSeconds));
            processBarPane.setPrefWidth(progress * maxWidth);
        }
    }

    private void initProgressBarClick() {
        if (processBarPane != null && processBarPane.getParent() instanceof Pane) {
            Pane trackBackground = (Pane) processBarPane.getParent();
            trackBackground.setStyle(trackBackground.getStyle() + "; -fx-cursor: hand;");
            trackBackground.setOnMouseClicked(event -> {
                if (totalDurationSeconds <= 0) return;
                double maxWidth = trackBackground.getPrefWidth() > 0 ? trackBackground.getPrefWidth() : 280.0;
                double clickX = Math.max(0, Math.min(maxWidth, event.getX()));
                currentProgressSeconds = (clickX / maxWidth) * totalDurationSeconds;
                if (sharedPlayer != null) {
                    try {
                        sharedPlayer.seek(Duration.seconds(currentProgressSeconds));
                    } catch (Exception ignored) {
                    }
                }
                updateDisplayTimes();
                updateProgressBar();
            });
        }
    }

    private void resetProgressBar() {
        if (processBarPane != null) {
            processBarPane.setPrefWidth(0);
        }
    }

    private String formatSeconds(double sec) {
        long totalSec = Math.max(0, (long) sec);
        long minutes = totalSec / 60;
        long seconds = totalSec % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @FXML
    private void pauseClickEvent() {
        isPlaying = !isPlaying;
        if (isPlaying) {
            if (sharedPlayer == null) {
                TrackInfo track = playlist.get(currentSongIndex % playlist.size());
                preparePlayer(track, true);
            } else {
                try {
                    sharedPlayer.play();
                } catch (Exception ignored) {
                }
            }
            setPlayIcon(true);
        } else {
            if (sharedPlayer != null) {
                try {
                    sharedPlayer.pause();
                } catch (Exception ignored) {
                }
            }
            setPlayIcon(false);
        }
    }

    private void setPlayIcon(boolean playing) {
        if (pauseIcon == null) {
            return;
        }
        try {
            String iconPath = playing
                    ? "/com/example/car_rental_sys/image/UI/playIcon_05.gif"
                    : "/com/example/car_rental_sys/image/UI/pause.png";
            URL resource = getClass().getResource(iconPath);
            if (resource != null) {
                pauseIcon.setImage(new Image(resource.toExternalForm()));
            }
        } catch (Exception ignored) {
        }
    }

    @FXML
    private void nextClickEvent() {
        if (playlist.isEmpty()) return;
        int nextIndex = (currentSongIndex + 1) % playlist.size();
        loadTrack(nextIndex);
    }

    @FXML
    private void previousClickEvent() {
        if (playlist.isEmpty()) return;
        int prevIndex = (currentSongIndex - 1 + playlist.size()) % playlist.size();
        loadTrack(prevIndex);
    }

    @FXML
    private void add10() {
        currentProgressSeconds = Math.min(totalDurationSeconds, currentProgressSeconds + 10);
        if (sharedPlayer != null) {
            try {
                sharedPlayer.seek(Duration.seconds(currentProgressSeconds));
            } catch (Exception ignored) {
            }
        }
        updateDisplayTimes();
        updateProgressBar();
    }

    @FXML
    private void minus10() {
        currentProgressSeconds = Math.max(0, currentProgressSeconds - 10);
        if (sharedPlayer != null) {
            try {
                sharedPlayer.seek(Duration.seconds(currentProgressSeconds));
            } catch (Exception ignored) {
            }
        }
        updateDisplayTimes();
        updateProgressBar();
    }
}
