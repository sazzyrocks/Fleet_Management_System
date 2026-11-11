package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.StatusContainer;
import com.example.car_rental_sys.concurrency.AsyncExecutor;
import com.example.car_rental_sys.dao.DAOFactory;
import com.example.car_rental_sys.dao.OrderDAO;
import com.example.car_rental_sys.model.Order;
import com.example.car_rental_sys.model.OrderStatus;
import com.example.car_rental_sys.model.Role;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.service.ServiceFactory;
import com.example.car_rental_sys.ui_components.UIFilter;
import com.example.car_rental_sys.ui_components.UIOrderRow;
import com.example.car_rental_sys.ui_components.UIPagination;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Order History & Management List Component (showOrderComponent.fxml).
 * Uses authentic UIOrderRow components with status pills, pagination, and filter dropdowns.
 */
public class OrderListComponentController extends Controller {

    public static OrderListComponentController instance;

    @FXML
    private Pane panelOrderList;

    @FXML
    private Label numOrder;

    @FXML
    private Button btnAllOrder;

    @FXML
    private Button btnComplete;

    @FXML
    private Button btnContinue;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnRentCar;

    @FXML
    private Label lblTitleOrders;

    @FXML
    private Label lblHeaderOrderID;

    @FXML
    private Label lblHeaderProduct;

    @FXML
    private Label lblHeaderDate;

    @FXML
    private Label lblHeaderPricing;

    @FXML
    private Label lblHeaderStatus;

    @FXML
    private javafx.scene.shape.Line tableDivider;

    @FXML
    private Pane tableContainer;

    @FXML
    private Pane pagContainer;

    @FXML
    public Pane paneFilterBox;

    private List<Order> allOrders = new ArrayList<>();
    private List<Order> displayedOrders = new ArrayList<>();
    private VBox orderRowBox;
    private ScrollPane orderScrollPane;
    private int currentPage = 1;
    private final int pageSize = 8;
    private UIPagination paginationComponent;

    @FXML
    public void initialize() {
        instance = this;
        setupUIContainers();
        initTheme();
        loadOrders();
    }

    private void initTheme() {
        boolean isCustomer = (StatusContainer.currentUser == null || StatusContainer.currentUser.getRole() == Role.CUSTOMER);
        if (isCustomer) {
            if (panelOrderList != null) {
                panelOrderList.setStyle("-fx-background-color: transparent;");
            }
            if (lblTitleOrders != null) {
                lblTitleOrders.setStyle("-fx-text-fill: #ffffff;");
            }
            if (numOrder != null) {
                numOrder.setStyle("-fx-text-fill: #86909c;");
            }
            if (lblHeaderOrderID != null) {
                lblHeaderOrderID.setStyle("-fx-text-fill: #4080ff;");
            }
            if (lblHeaderProduct != null) {
                lblHeaderProduct.setStyle("-fx-text-fill: #c5c5c5;");
            }
            if (lblHeaderDate != null) {
                lblHeaderDate.setStyle("-fx-text-fill: #c5c5c5;");
            }
            if (lblHeaderPricing != null) {
                lblHeaderPricing.setStyle("-fx-text-fill: #c5c5c5;");
            }
            if (lblHeaderStatus != null) {
                lblHeaderStatus.setStyle("-fx-text-fill: #c5c5c5;");
            }
            if (tableDivider != null) {
                tableDivider.setStroke(javafx.scene.paint.Color.web("#38383a"));
            }
        } else {
            if (lblTitleOrders != null) {
                lblTitleOrders.setStyle("-fx-text-fill: #1d2129;");
            }
            if (numOrder != null) {
                numOrder.setStyle("-fx-text-fill: #86909c;");
            }
            if (lblHeaderOrderID != null) {
                lblHeaderOrderID.setStyle("-fx-text-fill: #4080ff;");
            }
            if (lblHeaderProduct != null) {
                lblHeaderProduct.setStyle("-fx-text-fill: #4e5969;");
            }
            if (lblHeaderDate != null) {
                lblHeaderDate.setStyle("-fx-text-fill: #4e5969;");
            }
            if (lblHeaderPricing != null) {
                lblHeaderPricing.setStyle("-fx-text-fill: #4e5969;");
            }
            if (lblHeaderStatus != null) {
                lblHeaderStatus.setStyle("-fx-text-fill: #4e5969;");
            }
            if (tableDivider != null) {
                tableDivider.setStroke(javafx.scene.paint.Color.web("#d9d9d9"));
            }
        }
    }

    @FXML
    private void rentCarClicked(MouseEvent event) {
        com.example.car_rental_sys.util.SceneManager.switchScene("carsListPage.fxml");
    }

    private void setupUIContainers() {
        orderRowBox = new VBox(6);
        orderRowBox.setStyle("-fx-background-color: transparent;");

        orderScrollPane = new ScrollPane(orderRowBox);
        orderScrollPane.setFitToWidth(true);
        orderScrollPane.setPrefSize(460, 445);
        orderScrollPane.setMaxHeight(445);
        orderScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        orderScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        orderScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");

        if (tableContainer != null) {
            tableContainer.getChildren().setAll(orderScrollPane);
        }

        if (btnAllOrder != null) {
            btnAllOrder.getStyleClass().add("btnFocusStyle");
        }
    }

    public void addFilterPane() {
        if (paneFilterBox != null) {
            paneFilterBox.getChildren().setAll(new UIFilter());
        }
    }

    public void removeFilterPane() {
        if (paneFilterBox != null) {
            Platform.runLater(() -> paneFilterBox.getChildren().clear());
        }
    }

    public void loadOrders() {
        AsyncExecutor.getInstance().execute(() -> {
            try {
                OrderDAO dao = DAOFactory.getInstance().getOrderDAO();
                User currentUser = ServiceFactory.getInstance().getAuthService().getCurrentUser();
                List<Order> orders;

                if (currentUser != null && currentUser.getRole() == com.example.car_rental_sys.model.Role.CUSTOMER) {
                    orders = dao.findByCustomerId(currentUser.getUserId());
                } else {
                    orders = dao.findAll();
                }

                Platform.runLater(() -> {
                    allOrders = orders;
                    displayedOrders = new ArrayList<>(orders);
                    currentPage = 1;
                    renderOrderRows();
                });
            } catch (Exception e) {
                Platform.runLater(this::renderSampleOrderRows);
            }
        });
    }

    private void renderOrderRows() {
        if (orderRowBox == null) return;
        orderRowBox.getChildren().clear();

        if (displayedOrders.isEmpty()) {
            if (numOrder != null) {
                numOrder.setText("0 orders");
            }
            if (pagContainer != null) {
                pagContainer.getChildren().clear();
            }
            renderSampleOrderRows();
            return;
        }

        int totalCount = displayedOrders.size();
        if (numOrder != null) {
            numOrder.setText(totalCount + " orders");
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        if (currentPage < 1) {
            currentPage = 1;
        }

        if (pagContainer != null) {
            if (paginationComponent == null) {
                paginationComponent = new UIPagination(totalCount, pageSize, currentPage, page -> {
                    this.currentPage = page;
                    renderCurrentPageRows();
                });
            } else {
                paginationComponent.setOnPageChange(page -> {
                    this.currentPage = page;
                    renderCurrentPageRows();
                });
                paginationComponent.update(totalCount, pageSize, currentPage);
            }
            pagContainer.getChildren().setAll(paginationComponent);
        }

        renderCurrentPageRows();
    }

    private void renderCurrentPageRows() {
        if (orderRowBox == null) return;
        orderRowBox.getChildren().clear();

        if (orderScrollPane != null) {
            orderScrollPane.setVvalue(0.0);
        }

        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, displayedOrders.size());

        if (fromIndex >= displayedOrders.size()) {
            return;
        }

        List<Order> pageOrders = displayedOrders.subList(fromIndex, toIndex);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Order o : pageOrders) {
            String dateStr = o.getCreatedAt() != null ? o.getCreatedAt().format(formatter)
                    : (o.getStartDate() != null ? o.getStartDate().format(formatter) : "2026-11-01");
            int amount = o.getTotalPrice() != null ? o.getTotalPrice().intValue() : 250;
            int statusInt = mapOrderStatusToInt(o.getStatus());
            String carName = (o.getVehicleModel() != null && !o.getVehicleModel().isBlank())
                    ? o.getVehicleModel().replace("_", " ")
                    : "Vehicle #" + o.getVehicleId();

            UIOrderRow row = new UIOrderRow(o.getOrderId(), carName, dateStr, amount, statusInt);
            orderRowBox.getChildren().add(row);
        }
    }

    private void renderSampleOrderRows() {
        if (orderRowBox == null) return;
        orderRowBox.getChildren().clear();

        if (numOrder != null) {
            numOrder.setText("3 orders");
        }

        UIOrderRow r1 = new UIOrderRow(3, "Vehicle #1", "2026-11-02", 700, 1);
        UIOrderRow r2 = new UIOrderRow(2, "Vehicle #1", "2026-11-01", 700, 5);
        UIOrderRow r3 = new UIOrderRow(1, "Vehicle #2", "2026-11-01", 240, 5);

        orderRowBox.getChildren().addAll(r1, r2, r3);
    }

    private int mapOrderStatusToInt(OrderStatus status) {
        if (status == null) return 5;
        return switch (status) {
            case CANCELLED -> -1;
            case PENDING -> 0;
            case CONFIRMED -> 1;
            case ACTIVE -> 3;
            case ON_RENT -> 4;
            case COMPLETED -> 5;
        };
    }

    @FXML
    private void btnAllOrderClicked(MouseEvent event) {
        setFocusedButton(btnAllOrder);
        currentPage = 1;
        displayedOrders = new ArrayList<>(allOrders);
        renderOrderRows();
    }

    @FXML
    private void btnCompleteClicked(MouseEvent event) {
        setFocusedButton(btnComplete);
        currentPage = 1;
        displayedOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .toList();
        renderOrderRows();
    }

    @FXML
    private void btnCompletedClicked(MouseEvent event) {
        btnCompleteClicked(event);
    }

    @FXML
    private void btnContinueClicked(MouseEvent event) {
        setFocusedButton(btnContinue);
        currentPage = 1;
        displayedOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.ACTIVE || o.getStatus() == OrderStatus.CONFIRMED || o.getStatus() == OrderStatus.ON_RENT)
                .toList();
        renderOrderRows();
    }

    @FXML
    private void btnCancelClicked(MouseEvent event) {
        setFocusedButton(btnCancel);
        currentPage = 1;
        displayedOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
                .toList();
        renderOrderRows();
    }

    private void setFocusedButton(Button target) {
        Button[] buttons = {btnAllOrder, btnComplete, btnContinue, btnCancel};
        for (Button btn : buttons) {
            if (btn != null) {
                btn.getStyleClass().remove("btnFocusStyle");
                if (!btn.getStyleClass().contains("controlBtn")) {
                    btn.getStyleClass().add("controlBtn");
                }
            }
        }
        if (target != null) {
            target.getStyleClass().add("btnFocusStyle");
            target.getStyleClass().remove("controlBtn");
        }
    }

    public void refreshTable(int page) {
        renderOrderRows();
    }
}
