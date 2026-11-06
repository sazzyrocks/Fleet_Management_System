package com.example.car_rental_sys.ui_components;



import com.example.car_rental_sys.StatusContainer;

import com.example.car_rental_sys.ToolsLib.FXTools;

import com.example.car_rental_sys.ToolsLib.ImageTools;

import com.example.car_rental_sys.controllers.CarDetailsPageController;

import com.example.car_rental_sys.model.Vehicle;

import javafx.geometry.Pos;

import javafx.scene.Cursor;

import javafx.scene.control.Label;

import javafx.scene.image.Image;

import javafx.scene.image.ImageView;

import javafx.scene.layout.Pane;



/**

 * Authentic Vehicle Card component for the public vehicle catalog (carsListPage.fxml).

 * Features authentic linear gradients, cutout PNGs, capacity, transmission, and price tags.

 */

public class CarCard extends Pane {



    private int cardHeight = 310;

    private int cardWidth = 420;

    private String brandModel;

    private String passengers;

    private String price;

    private String carType;

    private String gradientDark;

    private String gradientLight;

    private Vehicle vehicle;



    public CarCard(String brandModel, String passengers, String price, String carType, String gradientDark, String gradientLight) {

        this(null, brandModel, passengers, price, carType, gradientDark, gradientLight);

    }



    public CarCard(Vehicle vehicle, String brandModel, String passengers, String price, String carType, String gradientDark, String gradientLight) {

        super();

        this.vehicle = vehicle;

        this.brandModel = brandModel;

        this.passengers = passengers;

        this.price = price;

        this.carType = carType;

        this.gradientDark = gradientDark != null ? gradientDark : "#0a1c38";

        this.gradientLight = gradientLight != null ? gradientLight : "#1669e0";

        initialize();

    }



    private void initialize() {

        initSize();

        initStyle();

        initCarBrand(this.brandModel);

        initParameters(this.passengers, this.price, this.carType);

        addCarImage(this.brandModel);

        initHoverEvent();

        addClickEvent();

    }



    private void initSize() {

        this.setPrefHeight(cardHeight);

        this.setPrefWidth(cardWidth);

    }



    private void initStyle() {

        this.setStyle("-fx-background-color: #000; " +

                "-fx-background-radius: 10px; " +

                "-fx-border-radius: 10px; " +

                "-fx-border-width: 1px;" +

                "-fx-padding: 50px;" +

                "-fx-border-insets: 50px;" +

                "-fx-background-insets: 50px;"

        );

    }



    private void initHoverEvent() {

        Pane pane = new Pane();

        pane.setPrefHeight(215);

        pane.setPrefWidth(320);

        pane.setLayoutX(50);

        pane.setLayoutY(48);

        pane.setStyle("-fx-background-color: transparent; " +

                "-fx-background-radius: 10px; " +

                "-fx-border-radius: 10px; "

        );



        pane.setOnMouseEntered(event -> setCursor(Cursor.HAND));

        pane.setOnMouseExited(event -> setCursor(Cursor.DEFAULT));

        this.getChildren().add(pane);

    }



    private void initCarBrand(String brand) {

        String displayName = (brand != null) ? brand.replace("_", " ") : "Luxury Fleet";

        Label carBrand = addLabel(displayName, 80, 70);

        this.getChildren().add(carBrand);

    }



    private void initParameters(String passgerNum, String priceNum, String type) {

        addImage("passage.png", 80, cardHeight - 80, 20, 20, "UI");

        addImage("manual.png", 140, cardHeight - 80, 20, 20, "UI");

        Label passagesNum = addLabel(passgerNum != null ? passgerNum : "2", 110, cardHeight - 80);

        Label manualOrAuto = addLabel(type != null ? type : "Automatic", 170, cardHeight - 80);

        Label priceLbl = addLabel("₹" + (priceNum != null ? priceNum : "850") + "/D", 280, cardHeight - 80);

        priceLbl.setAlignment(Pos.CENTER_RIGHT);



        this.getChildren().addAll(passagesNum, manualOrAuto, priceLbl);

    }



    private void addImage(String imageName, double x, double y, double width, double height, String type) {

        ImageView imageview = new ImageView();

        imageview.setLayoutX(x);

        imageview.setLayoutY(y);

        imageview.setFitHeight(height);

        imageview.setFitWidth(width);



        String path;

        if (type.equalsIgnoreCase("car")) {

            path = "/com/example/car_rental_sys/image/" + imageName;

        } else {

            path = "/com/example/car_rental_sys/image/UI/" + imageName;

        }



        Image img = ImageTools.getImageObjFromPath(path);

        if (img != null) {

            imageview.setImage(img);

        }

        this.getChildren().add(imageview);

    }



    private Label addLabel(String text, double x, double y) {

        Label label = new Label(text);

        label.setLayoutX(x);

        label.setLayoutY(y);

        label.setStyle("-fx-text-fill: #E5E5E5; " +

                "-fx-font-size: 14px; " +

                "-fx-font-weight: normal; ");

        return label;

    }



    private void addCarImage(String imageName) {

        String customPath = (vehicle != null && vehicle.getImagePath() != null) ? vehicle.getImagePath() : null;

        Image img = ImageTools.getCarImage(imageName, customPath);



        ImageView imageview = new ImageView();

        imageview.setLayoutX(50);

        imageview.setLayoutY(50);

        imageview.setFitHeight(216);

        imageview.setFitWidth(320);

        imageview.setPreserveRatio(true);

        imageview.setSmooth(true);

        if (img != null) {

            imageview.setImage(img);

        }

        this.getChildren().add(imageview);



        this.setStyle(this.getStyle() +

                "-fx-background-color: " +

                "linear-gradient(to left," + this.gradientDark + "," + this.gradientLight + ");");

    }



    private void addClickEvent() {

        this.setOnMouseClicked(event -> {

            StatusContainer.currentCarChose = this.brandModel;

            StatusContainer.isFirstViewCar = true;

            if (this.vehicle != null) {

                StatusContainer.currentVehicle = this.vehicle;

                StatusContainer.currentCarID = this.vehicle.getVehicleId();

                CarDetailsPageController.setSelectedVehicle(this.vehicle);

            }

            goToDetailsPage();

        });

    }



    private void goToDetailsPage() {

        FXTools.changeScene("carDetailsPage.fxml");

    }

}

