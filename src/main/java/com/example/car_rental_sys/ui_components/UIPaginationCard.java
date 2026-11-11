package com.example.car_rental_sys.ui_components;

import com.example.car_rental_sys.StatusContainer;
import com.example.car_rental_sys.model.Role;
import javafx.scene.Cursor;
import javafx.scene.control.Button;

/**
 * Authentic pagination pill button component with theme-aware styling and active states.
 */
public class UIPaginationCard extends Button {
    private final int cardHeight = 30;
    private final int cardWidth = 32;
    private boolean isActive = false;
    private String content;

    public UIPaginationCard(int number) {
        super();
        this.setPrefSize(cardWidth, cardHeight);
        this.setCursor(Cursor.HAND);
        initPageNumber(String.valueOf(number));
    }

    public void setActive(boolean active) {
        this.isActive = active;
        applyStyle();
    }

    public boolean isActive() {
        return isActive;
    }

    public void initPageNumber(String number) {
        switch (number) {
            case "-1":
            case "-2":
                content = "...";
                break;
            case "-3":
                content = "<";
                break;
            case "-4":
                content = ">";
                break;
            default:
                content = number;
                break;
        }
        this.setText(content);
        applyStyle();
    }

    private void applyStyle() {
        boolean isCustomer = (StatusContainer.currentUser == null || StatusContainer.currentUser.getRole() == Role.CUSTOMER);

        if ("...".equals(content)) {
            this.setDisable(true);
            this.setStyle("-fx-background-color: transparent; -fx-text-fill: #86909c; -fx-font-weight: bold;");
            return;
        }

        if (isActive) {
            this.setStyle("-fx-background-color: #165dff; -fx-text-fill: #ffffff; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 12px;");
        } else {
            if (isCustomer) {
                this.setStyle("-fx-background-color: #1f2329; -fx-text-fill: #c5c5c5; -fx-background-radius: 4px; -fx-border-color: #38383a; -fx-border-radius: 4px; -fx-font-size: 12px;");
            } else {
                this.setStyle("-fx-background-color: #f2f3f5; -fx-text-fill: #4e5969; -fx-background-radius: 4px; -fx-border-color: #e5e6eb; -fx-border-radius: 4px; -fx-font-size: 12px;");
            }
        }
    }
}
