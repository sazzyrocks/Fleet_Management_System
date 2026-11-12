package com.example.car_rental_sys.controllers;







import com.example.car_rental_sys.StatusContainer;



import com.example.car_rental_sys.ToolsLib.DataTools;



import com.example.car_rental_sys.ToolsLib.DateTools;



import com.example.car_rental_sys.ToolsLib.FXTools;



import com.example.car_rental_sys.ToolsLib.ImageTools;



import com.example.car_rental_sys.model.Role;



import com.example.car_rental_sys.model.Vehicle;



import com.example.car_rental_sys.service.AuthService;



import com.example.car_rental_sys.service.ServiceFactory;



import com.example.car_rental_sys.ui_components.BrowserModal;



import com.example.car_rental_sys.ui_components.CommentCard;



import com.example.car_rental_sys.ui_components.MessageFrame;



import com.example.car_rental_sys.ui_components.MessageFrameType;



import com.example.car_rental_sys.util.DatePickerModal;



import com.example.car_rental_sys.util.SceneManager;



import javafx.animation.FadeTransition;



import javafx.application.Platform;



import javafx.fxml.FXML;



import javafx.geometry.VPos;



import javafx.scene.canvas.Canvas;



import javafx.scene.canvas.GraphicsContext;



import javafx.scene.control.Label;



import javafx.scene.control.ScrollPane;



import javafx.scene.image.Image;



import javafx.scene.image.ImageView;



import javafx.scene.input.KeyCode;



import javafx.scene.input.KeyEvent;



import javafx.scene.layout.FlowPane;



import javafx.scene.layout.Pane;



import javafx.scene.paint.Color;



import javafx.scene.text.Font;



import javafx.scene.text.FontWeight;



import javafx.scene.text.TextAlignment;



import javafx.scene.transform.Rotate;



import javafx.scene.transform.Translate;



import javafx.util.Duration;







import java.net.URL;



import java.util.*;







/**



 * Controller for Vehicle Details & Rental Initiation (carDetailsPage.fxml).



 * Restored with authentic radar telemetry charts, key tips animations, car flipping, and review cards.



 */



public class CarDetailsPageController extends Controller {







    private static Vehicle selectedVehicle;







    @FXML



    public Pane mainPane;







    @FXML



    private Pane containPane;







    @FXML



    private ImageView carImageView;







    @FXML



    private ImageView logImageView;







    @FXML



    private ImageView backBtnImageView;







    @FXML



    private ImageView keyTipsImageView;







    @FXML



    private Pane radarPane;







    @FXML



    private ScrollPane commentScrollPane;







    @FXML



    private Label starText;







    @FXML



    private Label modelText;







    @FXML



    private Label speedText;







    @FXML



    private Label seatsText;







    @FXML



    private Label powerText;







    @FXML



    private Label priceText;







    @FXML



    private Label commentsNum;







    @FXML



    private Label viewMore;







    // Available car catalog list for cycling via arrow keys



    private static final String[] CAR_MODELS = {



            "Huracan_EVI",



            "Porsche_718",



            "Mclaren_720s",



            "Ferrari_SF90_Spider",



            "McLaren_620R",



            "Koenigsegg"



    };







    private static final Map<String, String[]> CAR_SPECS = new HashMap<>();



    private static final Map<String, String> CAR_BRANDS = new HashMap<>();



    private static final Map<String, double[]> RADAR_DATA = new HashMap<>();







    static {



        // [seats, dailyPrice, speed, power, rating]



        CAR_SPECS.put("Huracan_EVI", new String[]{"2", "1200", "325", "5.2", "4.9"});



        CAR_SPECS.put("Huracan_EVO", new String[]{"2", "350", "325", "5.2", "4.9"});



        CAR_SPECS.put("Model_3", new String[]{"5", "120", "261", "3.1", "4.8"});



        CAR_SPECS.put("Tesla_Model_3", new String[]{"5", "120", "261", "3.1", "4.8"});



        CAR_SPECS.put("718_Cayman", new String[]{"2", "250", "275", "2.0", "4.8"});



        CAR_SPECS.put("Porsche_718", new String[]{"2", "850", "275", "2.0", "4.8"});



        CAR_SPECS.put("488_Spider", new String[]{"2", "450", "330", "3.9", "4.9"});



        CAR_SPECS.put("Ferrari_488", new String[]{"2", "450", "330", "3.9", "4.9"});



        CAR_SPECS.put("720S", new String[]{"2", "420", "341", "4.0", "5.0"});



        CAR_SPECS.put("McLaren_720S", new String[]{"2", "420", "341", "4.0", "5.0"});



        CAR_SPECS.put("Mclaren_720s", new String[]{"2", "1400", "341", "4.0", "5.0"});



        CAR_SPECS.put("M4_Competition", new String[]{"4", "180", "290", "3.0", "4.7"});



        CAR_SPECS.put("BMW_M4", new String[]{"4", "180", "290", "3.0", "4.7"});



        CAR_SPECS.put("Ferrari_SF90_Spider", new String[]{"2", "1500", "340", "4.0", "4.9"});



        CAR_SPECS.put("McLaren_620R", new String[]{"2", "1350", "322", "3.8", "4.7"});



        CAR_SPECS.put("Koenigsegg", new String[]{"2", "2000", "400", "5.0", "5.0"});







        CAR_BRANDS.put("Huracan_EVI", "lamborghini");



        CAR_BRANDS.put("Huracan_EVO", "lamborghini");



        CAR_BRANDS.put("Model_3", "default");



        CAR_BRANDS.put("Tesla_Model_3", "default");



        CAR_BRANDS.put("718_Cayman", "Porsche");



        CAR_BRANDS.put("Porsche_718", "Porsche");



        CAR_BRANDS.put("488_Spider", "Ferrari");



        CAR_BRANDS.put("Ferrari_488", "Ferrari");



        CAR_BRANDS.put("720S", "McLaren");



        CAR_BRANDS.put("McLaren_720S", "McLaren");



        CAR_BRANDS.put("Mclaren_720s", "McLaren");



        CAR_BRANDS.put("M4_Competition", "default");



        CAR_BRANDS.put("BMW_M4", "default");



        CAR_BRANDS.put("Ferrari_SF90_Spider", "Ferrari");



        CAR_BRANDS.put("McLaren_620R", "McLaren");



        CAR_BRANDS.put("Koenigsegg", "Koenigsegg");







        // [power, comfort, handing, space, allocation, interior]



        RADAR_DATA.put("Huracan_EVI", new double[]{3.8, 4.2, 4.3, 4.2, 3.8, 3.1});



        RADAR_DATA.put("Huracan_EVO", new double[]{3.8, 4.2, 4.3, 4.2, 3.8, 3.1});



        RADAR_DATA.put("Model_3", new double[]{3.5, 4.5, 4.0, 4.6, 4.8, 4.2});



        RADAR_DATA.put("718_Cayman", new double[]{4.9, 4.2, 3.3, 3.0, 4.0, 3.4});



        RADAR_DATA.put("Porsche_718", new double[]{4.9, 4.2, 3.3, 3.0, 4.0, 3.4});



        RADAR_DATA.put("488_Spider", new double[]{3.1, 3.1, 3.8, 3.5, 3.7, 3.1});



        RADAR_DATA.put("720S", new double[]{5.0, 3.3, 3.7, 3.7, 4.0, 3.1});



        RADAR_DATA.put("Mclaren_720s", new double[]{5.0, 3.3, 3.7, 3.7, 4.0, 3.1});



        RADAR_DATA.put("M4_Competition", new double[]{3.9, 4.0, 4.2, 4.1, 4.3, 3.9});



        RADAR_DATA.put("Ferrari_SF90_Spider", new double[]{3.1, 3.1, 3.8, 3.5, 3.7, 3.1});



        RADAR_DATA.put("McLaren_620R", new double[]{3.1, 3.5, 4.5, 3.9, 4.0, 3.1});



        RADAR_DATA.put("Koenigsegg", new double[]{4.3, 3.6, 4.2, 4.5, 3.6, 4.0});



    }







    public static void setSelectedVehicle(Vehicle vehicle) {



        selectedVehicle = vehicle;



        StatusContainer.currentVehicle = vehicle;



        if (vehicle != null) {



            StatusContainer.currentCarID = vehicle.getVehicleId();



            if (vehicle.getModel() != null) {



                StatusContainer.currentCarChose = vehicle.getModel().replace(" ", "_");



            }



        }



    }







    public static Vehicle getSelectedVehicle() {



        return selectedVehicle;



    }







    private void ensureVehicleResolved() {



        if (selectedVehicle == null || (selectedVehicle.getModel() != null && !matchesModel(selectedVehicle, StatusContainer.currentCarChose))) {



            selectedVehicle = resolveVehicle(StatusContainer.currentCarChose);



            if (selectedVehicle != null) {



                setSelectedVehicle(selectedVehicle);



            }



        }



    }







    private boolean matchesModel(Vehicle v, String modelKey) {



        if (v == null || modelKey == null) return false;



        String cleanKey = modelKey.replace("_", " ").toLowerCase();



        String m = v.getModel() != null ? v.getModel().toLowerCase() : "";



        String b = v.getBrand() != null ? v.getBrand().toLowerCase() : "";



        return m.contains(cleanKey) || cleanKey.contains(m) || b.contains(cleanKey) || cleanKey.contains(b);



    }







    public static Vehicle resolveVehicle(String modelKey) {



        try {



            com.example.car_rental_sys.service.VehicleService vs = ServiceFactory.getInstance().getVehicleService();



            List<Vehicle> list = vs.getAllVehicles();



            if (list != null && !list.isEmpty()) {



                String clean = modelKey != null ? modelKey.replace("_", "").toLowerCase() : "";



                for (Vehicle v : list) {



                    String vModel = v.getModel() != null ? v.getModel().replace(" ", "").toLowerCase() : "";



                    String vBrand = v.getBrand() != null ? v.getBrand().replace(" ", "").toLowerCase() : "";



                    if (vModel.contains(clean) || clean.contains(vModel) || clean.contains(vBrand)) {



                        return v;



                    }



                }



                return list.get(0);



            }



        } catch (Exception ignored) {



        }







        Vehicle fallback = new Vehicle();



        fallback.setVehicleId(1);



        fallback.setBrand("Lamborghini");



        fallback.setModel("Huracan EVO");



        fallback.setSeats(2);



        fallback.setDailyRate(new java.math.BigDecimal("350.00"));



        fallback.setImagePath("/com/example/car_rental_sys/image/cars/Huracan_EVI.png");



        return fallback;



    }







    @FXML



    public void initialize() {



        StatusContainer.currentPageController = this;







        if (StatusContainer.currentCarChose == null || StatusContainer.currentCarChose.isBlank()) {



            StatusContainer.currentCarChose = "Huracan_EVI";



        }



        ensureVehicleResolved();







        initContainPaneEvent();



        initImage();



        initStar();



        initSpeedSeatsPowerPrice();



        initLogo();



        initComments();



        backBtnClickEvent();



        initKeyTips();



        initRadarChart();



    }







    private void initImage() {



        String carName = StatusContainer.currentCarChose;



        if (modelText != null) {



            modelText.setText(carName != null ? carName.replace("_", " ") : "Vehicle");



        }







        String customPath = (selectedVehicle != null) ? selectedVehicle.getImagePath() : null;



        Image image = ImageTools.getCarImage(carName, customPath);







        if (image != null && carImageView != null) {



            carImageView.setImage(image);



            carImageView.getTransforms().clear();



            // Horizontal mirror flip translation & rotation



            Translate flipTranslation = new Translate(image.getWidth(), 0);



            Rotate flipRotation = new Rotate(180, Rotate.Y_AXIS);



            carImageView.getTransforms().addAll(flipTranslation, flipRotation);



        }



    }







    private void initLogo() {



        String carName = StatusContainer.currentCarChose;



        String brand = (selectedVehicle != null && selectedVehicle.getBrand() != null)



                ? selectedVehicle.getBrand()



                : CAR_BRANDS.getOrDefault(carName, "Lamborghini");



        String logoPath = "/com/example/car_rental_sys/image/cars/logo/" + brand.toLowerCase() + "-logo.png";



        Image logo = ImageTools.getImageObjFromPath(logoPath);



        if (logo == null) {



            logo = ImageTools.getImageObjFromPath("/com/example/car_rental_sys/image/cars/logo/" + brand + "-Logo.png");



        }



        if (logo == null) {



            logo = ImageTools.getImageObjFromPath("/com/example/car_rental_sys/image/cars/logo/" + brand + "-logo.png");



        }



        if (logo == null) {



            logo = ImageTools.getImageObjFromPath("/com/example/car_rental_sys/image/cars/logo/default-logo.png");



        }



        if (logImageView != null && logo != null) {



            logImageView.setImage(logo);



        }



    }







    private void initStar() {



        String[] specs = CAR_SPECS.getOrDefault(StatusContainer.currentCarChose, new String[]{"2", "1200", "325", "5.2", "4.9"});



        if (starText != null) {



            starText.setText(specs[4]);



        }



    }







    private void initSpeedSeatsPowerPrice() {



        String[] specs = CAR_SPECS.getOrDefault(StatusContainer.currentCarChose, new String[]{"2", "1200", "325", "5.2", "4.9"});



        if (seatsText != null) seatsText.setText(specs[0]);



        if (priceText != null) priceText.setText("₹" + specs[1] + "/D");



        if (speedText != null) speedText.setText(specs[2] + " km/h");



        if (powerText != null) powerText.setText(specs[3] + " L");



    }







    private void initComments() {



        if (commentScrollPane == null) return;



        FlowPane flowPane = new FlowPane();



        flowPane.setPrefWidth(780);







        // Authentic review cards



        CommentCard c1 = new CommentCard("Marcus Vance", "Absolutely breathtaking performance. Acceleration is unmatched and the cockpit ergonomics are sublime.", "2026-10-25", 24, 5);



        CommentCard c2 = new CommentCard("Sophia Lin", "Effortless pickup at SRM AP campus hub. Pristine vehicle condition with flawless telemetry diagnostics.", "2026-10-21", 18, 5);



        CommentCard c3 = new CommentCard("Daniel Thorne", "A masterclass in automotive engineering. Would rent again without hesitation.", "2026-10-15", 9, 4);







        flowPane.getChildren().addAll(c1, c2, c3);



        if (commentsNum != null) commentsNum.setText("3 comments");







        commentScrollPane.setFitToWidth(true);



        commentScrollPane.setContent(flowPane);



    }







    private void backBtnClickEvent() {



        if (backBtnImageView != null) {



            backBtnImageView.setOnMouseEntered(event -> {



                Image img = ImageTools.getImageObjFromPath("/com/example/car_rental_sys/image/UI/back-blue.png");



                if (img != null) backBtnImageView.setImage(img);



                if (backBtnImageView.getScene() != null) {



                    backBtnImageView.getScene().setCursor(javafx.scene.Cursor.HAND);



                }



            });







            backBtnImageView.setOnMouseExited(event -> {



                Image img = ImageTools.getImageObjFromPath("/com/example/car_rental_sys/image/UI/back-white.png");



                if (img != null) backBtnImageView.setImage(img);



                if (backBtnImageView.getScene() != null) {



                    backBtnImageView.getScene().setCursor(javafx.scene.Cursor.DEFAULT);



                }



            });







            backBtnImageView.setOnMouseClicked(event -> FXTools.changeScene("carsListPage.fxml"));



        }







        if (viewMore != null) {



            viewMore.setOnMouseClicked(event -> FXTools.changeScene("ViewCommentsPage.fxml"));



        }



    }







    private void initKeyTips() {
        if (keyTipsImageView == null) return;

        Image tips = ImageTools.getImageObjFromPath("/com/example/car_rental_sys/image/UI/pressLeftAndRight.gif");
        if (tips != null) {
            keyTipsImageView.setImage(tips);
            keyTipsImageView.setOpacity(1.0);

            FadeTransition fade = new FadeTransition(Duration.seconds(1), keyTipsImageView);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setDelay(Duration.seconds(4));
            fade.setOnFinished(e -> {
                keyTipsImageView.setImage(null);
            });
            fade.play();
        }
    }







    private void initRadarChart() {



        if (radarPane == null) return;



        radarPane.getChildren().clear();







        double[] radar = RADAR_DATA.getOrDefault(StatusContainer.currentCarChose, new double[]{3.8, 4.2, 4.3, 4.2, 3.8, 3.1});



        Canvas canvas = drawRadarCanvas(397, 311, radar);



        canvas.setId("nativeRadarCanvas");







        radarPane.getChildren().add(canvas);



    }







    private Canvas drawRadarCanvas(double width, double height, double[] values) {



        Canvas canvas = new Canvas(width, height);



        GraphicsContext gc = canvas.getGraphicsContext2D();







        double cx = width / 2.0;



        double cy = height / 2.0;



        double radius = Math.min(width, height) * 0.35; // ~108px







        String[] labels = {"Power", "Comfort", "Handling", "Space", "Allocation", "Interior"};



        int n = 6;



        double startAngleRad = Math.toRadians(120);







        // 1. Concentric hexagon rings (grid)



        gc.setLineWidth(1.0);



        gc.setStroke(Color.web("#444444"));







        for (int ring = 1; ring <= 5; ring++) {



            double r = radius * (ring / 5.0);



            double[] xPoints = new double[n];



            double[] yPoints = new double[n];



            for (int i = 0; i < n; i++) {



                double angle = startAngleRad + (i * 2 * Math.PI / n);



                xPoints[i] = cx + r * Math.cos(angle);



                yPoints[i] = cy - r * Math.sin(angle);



            }



            gc.strokePolygon(xPoints, yPoints, n);



        }







        // 2. Radial axis lines & text labels



        gc.setFont(Font.font("System", FontWeight.BOLD, 12));



        gc.setTextAlign(TextAlignment.CENTER);



        gc.setTextBaseline(VPos.CENTER);







        for (int i = 0; i < n; i++) {



            double angle = startAngleRad + (i * 2 * Math.PI / n);



            double endX = cx + radius * Math.cos(angle);



            double endY = cy - radius * Math.sin(angle);







            // Axis line



            gc.setStroke(Color.web("#555555"));



            gc.setLineWidth(1.2);



            gc.strokeLine(cx, cy, endX, endY);







            // Label position



            double labelR = radius + 22;



            double lx = cx + labelR * Math.cos(angle);



            double ly = cy - labelR * Math.sin(angle);







            gc.setFill(Color.web("#dcdcdc"));



            gc.fillText(labels[i], lx, ly);



        }







        // 3. Data polygon



        double[] dataX = new double[n];



        double[] dataY = new double[n];



        for (int i = 0; i < n; i++) {



            double val = (i < values.length) ? values[i] : 3.5;



            double r = radius * Math.min(1.0, Math.max(0.1, val / 5.0));



            double angle = startAngleRad + (i * 2 * Math.PI / n);



            dataX[i] = cx + r * Math.cos(angle);



            dataY[i] = cy - r * Math.sin(angle);



        }







        // Fill polygon (blue with opacity)



        gc.setFill(Color.web("#165dff", 0.35));



        gc.fillPolygon(dataX, dataY, n);







        // Stroke polygon



        gc.setStroke(Color.web("#165dff"));



        gc.setLineWidth(2.8);



        gc.strokePolygon(dataX, dataY, n);







        // 4. Dot markers at each vertex



        for (int i = 0; i < n; i++) {



            // Glow



            gc.setFill(Color.web("#CAE943", 0.35));



            gc.fillOval(dataX[i] - 7, dataY[i] - 7, 14, 14);







            // Inner dot



            gc.setFill(Color.web("#CAE943"));



            gc.fillOval(dataX[i] - 4, dataY[i] - 4, 8, 8);







            // Dot border



            gc.setStroke(Color.web("#ffffff"));



            gc.setLineWidth(1.2);



            gc.strokeOval(dataX[i] - 4, dataY[i] - 4, 8, 8);



        }







        return canvas;



    }







    private void initContainPaneEvent() {



        if (mainPane == null) return;



        mainPane.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {



            if (keyEvent.getCode() == KeyCode.LEFT) {



                changeCar(-1);



            } else if (keyEvent.getCode() == KeyCode.RIGHT) {



                changeCar(1);



            }



            keyEvent.consume();



        });



    }







    private void changeCar(int direction) {



        int currentIndex = 0;



        for (int i = 0; i < CAR_MODELS.length; i++) {



            if (CAR_MODELS[i].equalsIgnoreCase(StatusContainer.currentCarChose)) {



                currentIndex = i;



                break;



            }



        }







        int nextIndex = currentIndex + direction;



        if (nextIndex < 0) nextIndex = CAR_MODELS.length - 1;



        if (nextIndex >= CAR_MODELS.length) nextIndex = 0;







        StatusContainer.currentCarChose = CAR_MODELS[nextIndex];



        Vehicle newV = resolveVehicle(CAR_MODELS[nextIndex]);



        if (newV != null) {



            setSelectedVehicle(newV);



        }



        FXTools.changeScene("carDetailsPage.fxml");



    }







    @FXML



    private void rentNowBtnClickEvent() {



        ensureVehicleResolved();







        AuthService authService = ServiceFactory.getInstance().getAuthService();



        com.example.car_rental_sys.model.User currentUser = authService.getCurrentUser() != null



                ? authService.getCurrentUser()



                : StatusContainer.currentUser;







        if (currentUser != null) {



            if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.DRIVER) {



                MessageFrame messageFrame = new MessageFrame(MessageFrameType.CONFIRM, "You are currently logged in as staff. Please log in as a customer to rent a vehicle.");



                messageFrame.setSuccessCallbackFunc((i) -> {



                    FXTools.changeScene("loginPage.fxml");



                    return null;



                });



                messageFrame.show();



                return;



            }



        }







        // Ensure dates are valid



        if (StatusContainer.pickUpTimeStamp == 0 || StatusContainer.returnTimeStamp == 0 || StatusContainer.returnTimeStamp <= StatusContainer.pickUpTimeStamp) {
            showDatePicker();
            return;
        }







        // Ensure location is valid



        if (StatusContainer.pickUpLocation == null || StatusContainer.pickUpLocation.isBlank()) {



            StatusContainer.pickUpLocation = "SRM University-AP, Amaravati";



            StatusContainer.locationChose = StatusContainer.pickUpLocation;



        }







        // Check if user is logged in



        if (currentUser == null) {



            StatusContainer.loginEntrance = "carDetails";



            FXTools.changeScene("loginPage.fxml");



            return;



        }







        if (selectedVehicle != null) {



            setSelectedVehicle(selectedVehicle);



        }







        // Proceed to payment page



        FXTools.changeScene("paymentPage.fxml");



    }







    private void showDatePicker() {



        DatePickerModal modal = new DatePickerModal(



                SceneManager.getPrimaryStage(),



                (pickStr, retStr) -> {



                    StatusContainer.pickDateTime = pickStr;



                    StatusContainer.returnDateTime = retStr;



                    StatusContainer.pickUpTimeStamp = DateTools.dateTimeToTimestamp(pickStr);



                    StatusContainer.returnTimeStamp = DateTools.dateTimeToTimestamp(retStr);



                    rentNowBtnClickEvent();



                }



        );



        modal.show();



    }







    private void showLocationPicker() {



        String url = "/com/example/car_rental_sys/html/googleMap.html";



        BrowserModal browserModal = new BrowserModal(800, 600, url);



        browserModal.setModality();



        browserModal.setFunction(message -> {



            if (message != null && !message.isBlank()) {



                StatusContainer.pickUpLocation = message;



                StatusContainer.locationChose = message;



                Platform.runLater(this::rentNowBtnClickEvent);



            }



            return null;



        });



        browserModal.show();



    }



}



