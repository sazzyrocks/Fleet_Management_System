package com.example.car_rental_sys.ui_components;

import com.example.car_rental_sys.model.DeliveryOrder;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Modern JavaFX OrderCard component matching original UI specifications.
 * Displays vehicle model, license plate, preview thumbnail, rental interval,
 * and dispatch action buttons.
 */
public class OrderCard extends Pane {

    private final DeliveryOrder order;
    private boolean isSelected = false;

    private static final String NORMAL_STYLE =
            "-fx-background-color: white; -fx-background-radius: 14; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 8, 0, 0, 2); " +
            "-fx-cursor: hand;";

    private static final String SELECTED_STYLE =
            "-fx-background-color: white; -fx-background-radius: 14; " +
            "-fx-border-color: #165dff; -fx-border-radius: 14; -fx-border-width: 2.2; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(22,93,255,0.35), 12, 0, 0, 3); " +
            "-fx-cursor: hand;";

    public OrderCard(DeliveryOrder order,
                     Consumer<DeliveryOrder> onSelect,
                     Consumer<DeliveryOrder> onAccept,
                     Consumer<DeliveryOrder> onDismiss) {
        this.order = order;
        setPrefSize(270, 145);
        setMinSize(270, 145);
        setMaxSize(270, 145);
        setStyle(NORMAL_STYLE);

        // 1. Car Model Label
        Label modelLabel = new Label(order.getCarModel().replace("_", " "));
        modelLabel.setLayoutX(12);
        modelLabel.setLayoutY(12);
        modelLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        modelLabel.setStyle("-fx-text-fill: #1d2129;");

        // 2. Plate Number Label
        Label plateLabel = new Label(order.getCarNumber());
        plateLabel.setLayoutX(13);
        plateLabel.setLayoutY(34);
        plateLabel.setFont(Font.font("System", 12));
        plateLabel.setStyle("-fx-text-fill: #86909c;");

        // 2B. Optional [DEMO] badge if fallback data
        Label demoBadge = null;
        if (order.isDemo()) {
            demoBadge = new Label("DEMO");
            demoBadge.setLayoutX(100);
            demoBadge.setLayoutY(34);
            demoBadge.setFont(Font.font("System", FontWeight.BOLD, 9));
            demoBadge.setStyle("-fx-background-color: rgba(255, 125, 0, 0.18); -fx-text-fill: #ff7d00; -fx-background-radius: 4; -fx-padding: 1 5 1 5;");
        }

        // 3. Eye / Dismiss Button
        Pane hideBtn = createActionButton("/com/example/car_rental_sys/image/UI/hide.png", "#78909c", 172, 12);
        hideBtn.setOnMouseClicked(e -> {
            e.consume();
            if (onDismiss != null) onDismiss.accept(order);
        });

        // 4. Checkmark / Accept Button
        Pane chooseBtn = createActionButton("/com/example/car_rental_sys/image/UI/choose.png", "#00b42a", 216, 12);
        chooseBtn.setOnMouseClicked(e -> {
            e.consume();
            if (onAccept != null) onAccept.accept(order);
        });

        // 5. Car Thumbnail Gradient Box
        Pane carThumbBox = new Pane();
        carThumbBox.setLayoutX(12);
        carThumbBox.setLayoutY(64);
        carThumbBox.setPrefSize(120, 68);
        String bgGradient = String.format("-fx-background-color: linear-gradient(to left, %s, %s); -fx-background-radius: 10;",
                order.getDarkColor(), order.getLightColor());
        carThumbBox.setStyle(bgGradient);

        ImageView carImg = new ImageView();
        carImg.setFitWidth(104);
        carImg.setFitHeight(56);
        carImg.setLayoutX(8);
        carImg.setLayoutY(6);
        carImg.setPreserveRatio(true);
        carImg.setScaleX(-1); // Flip horizontally to match original catalog style

        String carRes = "/com/example/car_rental_sys/image/cars/" + order.getCarModel() + ".png";
        try (InputStream stream = getClass().getResourceAsStream(carRes)) {
            if (stream != null) {
                carImg.setImage(new Image(stream));
            }
        } catch (Exception ignored) {}

        carThumbBox.getChildren().add(carImg);

        // 6. Dates Column
        Label startDateLabel = new Label(order.getStartDate());
        startDateLabel.setLayoutX(146);
        startDateLabel.setLayoutY(70);
        startDateLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        startDateLabel.setStyle("-fx-text-fill: #1d2129;");

        Label toLabel = new Label("To");
        toLabel.setLayoutX(172);
        toLabel.setLayoutY(89);
        toLabel.setFont(Font.font("System", 11));
        toLabel.setStyle("-fx-text-fill: #86909c;");

        Label endDateLabel = new Label(order.getEndDate());
        endDateLabel.setLayoutX(146);
        endDateLabel.setLayoutY(108);
        endDateLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        endDateLabel.setStyle("-fx-text-fill: #1d2129;");

        // 7. Rental Mode Badge
        Label modeBadge = new Label(order.isChauffeur() ? "CHAUFFEUR" : "SELF-DRIVE");
        modeBadge.setLayoutX(146);
        modeBadge.setLayoutY(48);
        modeBadge.setFont(Font.font("System", FontWeight.BOLD, 9));
        if (order.isChauffeur()) {
            modeBadge.setStyle("-fx-background-color: rgba(114, 46, 209, 0.15); -fx-text-fill: #722ED1; -fx-background-radius: 4; -fx-padding: 1 5 1 5;");
        } else {
            modeBadge.setStyle("-fx-background-color: rgba(0, 180, 42, 0.15); -fx-text-fill: #00b42a; -fx-background-radius: 4; -fx-padding: 1 5 1 5;");
        }

        getChildren().addAll(modelLabel, plateLabel, hideBtn, chooseBtn, carThumbBox, modeBadge, startDateLabel, toLabel, endDateLabel);
        if (demoBadge != null) {
            getChildren().add(demoBadge);
        }

        // Click on card selects this order
        setOnMouseClicked(e -> {
            if (onSelect != null) onSelect.accept(order);
        });

        // Hover feedback
        setOnMouseEntered(e -> {
            if (!isSelected) {
                setStyle(NORMAL_STYLE + " -fx-border-color: #c9cdd4; -fx-border-radius: 14; -fx-border-width: 1.5;");
            }
        });

        setOnMouseExited(e -> {
            if (!isSelected) {
                setStyle(NORMAL_STYLE);
            }
        });
    }

    private Pane createActionButton(String iconPath, String bgColor, double x, double y) {
        Pane btn = new Pane();
        btn.setLayoutX(x);
        btn.setLayoutY(y);
        btn.setPrefSize(36, 26);
        btn.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 6; -fx-cursor: hand;", bgColor));

        ImageView icon = new ImageView();
        icon.setFitWidth(18);
        icon.setFitHeight(18);
        icon.setLayoutX(9);
        icon.setLayoutY(4);
        icon.setPreserveRatio(true);

        try (InputStream stream = getClass().getResourceAsStream(iconPath)) {
            if (stream != null) {
                icon.setImage(new Image(stream));
            }
        } catch (Exception ignored) {}

        btn.getChildren().add(icon);

        btn.setOnMouseEntered(e -> btn.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 6; -fx-cursor: hand; -fx-scale-x: 1.08; -fx-scale-y: 1.08;", bgColor)));
        btn.setOnMouseExited(e -> btn.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 6; -fx-cursor: hand; -fx-scale-x: 1.0; -fx-scale-y: 1.0;", bgColor)));

        return btn;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        setStyle(selected ? SELECTED_STYLE : NORMAL_STYLE);
    }

    public DeliveryOrder getOrder() {
        return order;
    }
}

