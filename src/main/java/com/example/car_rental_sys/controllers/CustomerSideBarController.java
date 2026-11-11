package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.concurrency.AsyncExecutor;
import com.example.car_rental_sys.dao.DAOFactory;
import com.example.car_rental_sys.dao.OrderDAO;
import com.example.car_rental_sys.model.Order;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.service.AuthService;
import com.example.car_rental_sys.service.ServiceFactory;
import com.example.car_rental_sys.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.List;

/**
 * Controller for the Customer Portal Sidebar.
 */
public class CustomerSideBarController extends Controller {

    public static CustomerSideBarController instance;

    @FXML
    private ImageView avatarImageView;

    @FXML
    private Label nameTextLabel;

    @FXML
    private Label emailTextLabel;

    @FXML
    private Pane itemRentCar; // Rent a Car / Browse Catalog

    @FXML
    private Pane item1; // Profile

    @FXML
    private Pane item2; // Message

    @FXML
    private Pane item3; // Wallet

    @FXML
    private Pane item4; // History

    @FXML
    private Pane item5; // Setting

    @FXML
    private Pane item6; // Logout

    @FXML
    private ImageView vipCardImageView;

    @FXML
    private Pane bar1;
    @FXML
    private Pane bar2;
    @FXML
    private Pane bar3;
    @FXML
    private Pane bar4;
    @FXML
    private Pane bar5;

    @FXML
    private Button customerSideBarUpdateBtn;

    @FXML
    private ImageView vipBadgeImageView;

    @FXML
    private Label orderNumLabel;

    @FXML
    public void initialize() {
        instance = this;
        populateCustomerData();
        attachNavigationHandlers();
    }

    public void populateCustomerData() {
        AuthService authService = ServiceFactory.getInstance().getAuthService();
        User currentUser = authService.getCurrentUser();

        if (currentUser != null) {
            if (nameTextLabel != null) {
                nameTextLabel.setText(currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getUsername());
            }
            if (emailTextLabel != null) {
                emailTextLabel.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "customer@fleet.local");
            }

            // Load order history count asynchronously and update VIP tier
            AsyncExecutor.getInstance().execute(() -> {
                try {
                    OrderDAO orderDAO = DAOFactory.getInstance().getOrderDAO();
                    List<Order> userOrders = orderDAO.findByCustomerId(currentUser.getUserId());
                    long validOrders = userOrders.stream()
                            .filter(o -> o.getStatus() != com.example.car_rental_sys.model.OrderStatus.CANCELLED)
                            .count();
                    int count = (int) validOrders;

                    Platform.runLater(() -> {
                        if (orderNumLabel != null) {
                            orderNumLabel.setText(String.format("%02d", count));
                        }
                        updateVipTier(count);
                    });
                } catch (Exception ignored) {
                }
            });
        }
    }

    private void updateVipTier(int orderCount) {
        int tier;
        if (orderCount < 5) {
            tier = 1;
        } else if (orderCount < 10) {
            tier = 2;
        } else if (orderCount < 20) {
            tier = 3;
        } else if (orderCount < 35) {
            tier = 4;
        } else {
            tier = 5;
        }

        String cardPath = switch (tier) {
            case 1 -> "/com/example/car_rental_sys/image/UI/vip1card.png";
            case 2 -> "/com/example/car_rental_sys/image/UI/vip2card.png";
            case 3 -> "/com/example/car_rental_sys/image/UI/vip3card.png";
            case 4 -> "/com/example/car_rental_sys/image/UI/vip4card.png";
            default -> "/com/example/car_rental_sys/image/UI/svipcard.png";
        };

        String badgePath = switch (tier) {
            case 1 -> "/com/example/car_rental_sys/image/UI/vip1.png";
            case 2 -> "/com/example/car_rental_sys/image/UI/vip2.png";
            case 3 -> "/com/example/car_rental_sys/image/UI/vip3.png";
            case 4 -> "/com/example/car_rental_sys/image/UI/vip4.png";
            default -> "/com/example/car_rental_sys/image/UI/svip.png";
        };

        try {
            if (vipCardImageView != null) {
                java.io.InputStream cStream = getClass().getResourceAsStream(cardPath);
                if (cStream != null) {
                    vipCardImageView.setImage(new javafx.scene.image.Image(cStream));
                }
            }
            if (vipBadgeImageView != null) {
                java.io.InputStream bStream = getClass().getResourceAsStream(badgePath);
                if (bStream != null) {
                    vipBadgeImageView.setImage(new javafx.scene.image.Image(bStream));
                }
            }
        } catch (Exception ignored) {}

        Pane[] bars = {bar1, bar2, bar3, bar4, bar5};
        for (int i = 0; i < bars.length; i++) {
            if (bars[i] != null) {
                if (i < tier) {
                    bars[i].setStyle("-fx-background-color: #fcdf7b; -fx-background-radius: 5;");
                } else {
                    bars[i].setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");
                }
            }
        }
    }

    private void attachNavigationHandlers() {
        if (itemRentCar != null) {
            itemRentCar.setOnMouseClicked(event -> rentCarClicked(event));
        }
        if (item1 != null) {
            item1.setOnMouseClicked(event -> {
                if (CustomerServiceController.instance != null) {
                    CustomerServiceController.instance.showProfilePage();
                } else {
                    SceneManager.switchScene("customerServicePage.fxml");
                    Platform.runLater(() -> {
                        if (CustomerServiceController.instance != null) {
                            CustomerServiceController.instance.showProfilePage();
                        }
                    });
                }
            });
        }
        if (item2 != null) {
            item2.setOnMouseClicked(event -> SceneManager.switchScene("messagePage.fxml"));
        }
        if (item3 != null) {
            item3.setOnMouseClicked(event -> {
                if (CustomerServiceController.instance != null) {
                    CustomerServiceController.instance.showWalletPage();
                } else {
                    SceneManager.switchScene("customerServicePage.fxml");
                    Platform.runLater(() -> {
                        if (CustomerServiceController.instance != null) {
                            CustomerServiceController.instance.showWalletPage();
                        }
                    });
                }
            });
        }
        if (item4 != null) {
            item4.setOnMouseClicked(event -> {
                if (CustomerServiceController.instance != null) {
                    CustomerServiceController.instance.showOrderPage();
                } else {
                    SceneManager.switchScene("customerServicePage.fxml");
                    Platform.runLater(() -> {
                        if (CustomerServiceController.instance != null) {
                            CustomerServiceController.instance.showOrderPage();
                        }
                    });
                }
            });
        }
        if (item5 != null) {
            item5.setOnMouseClicked(event -> {
                if (CustomerServiceController.instance != null) {
                    CustomerServiceController.instance.showSettingPage();
                } else {
                    SceneManager.switchScene("customerServicePage.fxml");
                    Platform.runLater(() -> {
                        if (CustomerServiceController.instance != null) {
                            CustomerServiceController.instance.showSettingPage();
                        }
                    });
                }
            });
        }
        if (item6 != null) {
            item6.setOnMouseClicked(event -> handleLogout());
        }
    }

    @FXML
    public void rentCarClicked(MouseEvent event) {
        SceneManager.switchScene("carsListPage.fxml");
    }

    @FXML
    private void updateBtnClick(MouseEvent event) {
        populateCustomerData();
    }

    private void handleLogout() {
        ServiceFactory.getInstance().getAuthService().logout();
        SceneManager.switchScene("loginPage.fxml");
    }
}
