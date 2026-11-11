package com.example.car_rental_sys.ui_components;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.util.function.Consumer;

/**
 * Authentic pagination bar component for tables and order lists.
 * Supports active page indicators, next/prev actions, and page switch callbacks.
 */
public class UIPagination extends Pane {

    private int totalNumber = 10;
    private int perPage = 8;
    private int currentPageNumber = 1;
    private int totalPageNumber = 1;
    private Consumer<Integer> onPageChange;

    public UIPagination() {
        this(10, 8, 1, null);
    }

    public UIPagination(int total, int perPage) {
        this(total, perPage, 1, null);
    }

    public UIPagination(int total, int perPage, int currentPage, Consumer<Integer> onPageChange) {
        this.totalNumber = total;
        this.perPage = perPage;
        this.totalPageNumber = Math.max(1, (int) Math.ceil((double) total / perPage));
        this.currentPageNumber = Math.max(1, Math.min(currentPage, totalPageNumber));
        this.onPageChange = onPageChange;
        initUI();
    }

    public void setOnPageChange(Consumer<Integer> listener) {
        this.onPageChange = listener;
    }

    public void update(int total, int perPage, int currentPage) {
        this.totalNumber = total;
        this.perPage = perPage;
        this.totalPageNumber = Math.max(1, (int) Math.ceil((double) total / perPage));
        this.currentPageNumber = Math.max(1, Math.min(currentPage, totalPageNumber));
        initUI();
    }

    public int getCurrentPage() {
        return currentPageNumber;
    }

    public int getTotalPages() {
        return totalPageNumber;
    }

    private void initUI() {
        this.getChildren().clear();
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER);

        // Previous button (<)
        UIPaginationCard prevBtn = new UIPaginationCard(-3);
        if (currentPageNumber <= 1) {
            prevBtn.setDisable(true);
            prevBtn.setOpacity(0.4);
        } else {
            prevBtn.setOnAction(e -> goToPage(currentPageNumber - 1));
        }
        box.getChildren().add(prevBtn);

        // Page number buttons
        if (totalPageNumber <= 7) {
            for (int i = 1; i <= totalPageNumber; i++) {
                final int pageNum = i;
                UIPaginationCard pageCard = new UIPaginationCard(pageNum);
                if (pageNum == currentPageNumber) {
                    pageCard.setActive(true);
                } else {
                    pageCard.setActive(false);
                    pageCard.setOnAction(e -> goToPage(pageNum));
                }
                box.getChildren().add(pageCard);
            }
        } else {
            addPageButton(box, 1);
            if (currentPageNumber > 3) {
                box.getChildren().add(new UIPaginationCard(-1)); // ellipsis
            }
            int start = Math.max(2, currentPageNumber - 1);
            int end = Math.min(totalPageNumber - 1, currentPageNumber + 1);
            for (int i = start; i <= end; i++) {
                addPageButton(box, i);
            }
            if (currentPageNumber < totalPageNumber - 2) {
                box.getChildren().add(new UIPaginationCard(-1)); // ellipsis
            }
            addPageButton(box, totalPageNumber);
        }

        // Next button (>)
        UIPaginationCard nextBtn = new UIPaginationCard(-4);
        if (currentPageNumber >= totalPageNumber) {
            nextBtn.setDisable(true);
            nextBtn.setOpacity(0.4);
        } else {
            nextBtn.setOnAction(e -> goToPage(currentPageNumber + 1));
        }
        box.getChildren().add(nextBtn);

        this.getChildren().add(box);
    }

    private void addPageButton(HBox box, int pageNum) {
        UIPaginationCard card = new UIPaginationCard(pageNum);
        if (pageNum == currentPageNumber) {
            card.setActive(true);
        } else {
            card.setActive(false);
            card.setOnAction(e -> goToPage(pageNum));
        }
        box.getChildren().add(card);
    }

    private void goToPage(int page) {
        if (page == currentPageNumber || page < 1 || page > totalPageNumber) {
            return;
        }
        this.currentPageNumber = page;
        initUI();
        if (onPageChange != null) {
            onPageChange.accept(page);
        }
    }
}
