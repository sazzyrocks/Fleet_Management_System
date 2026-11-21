package com.example.car_rental_sys.ToolsLib;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * Image processing and loading utilities for authentic UI visual components.
 */
public class ImageTools {

    public static Image getImageObjFromPath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }

        // Clean prefix
        String cleanPath = path;
        if (cleanPath.startsWith("file:")) {
            cleanPath = cleanPath.substring(5);
        }

        // Try file path on disk
        File file = new File(cleanPath);
        if (file.exists()) {
            return new Image(file.toURI().toString());
        }

        // Try classpath resource
        String resPath = path;
        if (resPath.startsWith("src/main/resources")) {
            resPath = resPath.substring("src/main/resources".length());
        }
        if (!resPath.startsWith("/")) {
            resPath = "/" + resPath;
        }

        try (InputStream is = ImageTools.class.getResourceAsStream(resPath)) {
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception ignored) {
        }

        URL url = ImageTools.class.getResource(resPath);
        if (url != null) {
            return new Image(url.toExternalForm());
        }

        // Fallback file attempt
        try {
            return new Image(new File(cleanPath).toURI().toString());
        } catch (Exception e) {
            return null;
        }
    }

    public static void setImageShapeToCircle(ImageView imageView) {
        if (imageView == null) return;
        double radius = Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2.0;
        if (radius <= 0) radius = 20.0;
        Circle clip = new Circle(radius, radius, radius);
        imageView.setClip(clip);
    }

    public static void centerImage(ImageView imageView) {
        if (imageView == null || imageView.getImage() == null) return;
        Image img = imageView.getImage();
        double ratioX = imageView.getFitWidth() / Math.max(1, img.getWidth());
        double ratioY = imageView.getFitHeight() / Math.max(1, img.getHeight());
        double rePosition = Math.min(ratioX, ratioY);

        double w = img.getWidth() * rePosition;
        double h = img.getHeight() * rePosition;

        imageView.setX((imageView.getFitWidth() - w) / 2);
        imageView.setY((imageView.getFitHeight() - h) / 2);
    }

    public static Image getCarImageObjFromCarID(int carID) {
        try {
            var vOpt = com.example.car_rental_sys.service.ServiceFactory.getInstance().getVehicleService().getVehicleById(carID);
            if (vOpt.isPresent()) {
                var v = vOpt.get();
                return getCarImage(v.getModel(), v.getImagePath());
            }
        } catch (Exception ignored) {
        }
        return getCarImage("Huracan_EVI");
    }

    public static Image getCarImage(String modelName) {
        return getCarImage(modelName, null);
    }

    public static Image getCarImage(String modelName, String imagePath) {
        // 1. If explicit imagePath is provided, try loading it
        if (imagePath != null && !imagePath.isBlank()) {
            Image img = getImageObjFromPath(imagePath);
            if (img != null && !img.isError()) {
                return img;
            }
        }

        // 2. Try loading by modelName
        if (modelName != null && !modelName.isBlank()) {
            String key = modelName.trim().replace(" ", "_");
            Image img = getImageObjFromPath("/com/example/car_rental_sys/image/cars/" + key + ".png");
            if (img != null && !img.isError()) {
                return img;
            }

            // Fuzzy match known patterns
            String lower = key.toLowerCase();
            String matched = null;
            if (lower.contains("huracan") || lower.contains("evo") || lower.contains("evi")) {
                matched = "Huracan_EVI.png";
            } else if (lower.contains("718") || lower.contains("cayman")) {
                matched = "Porsche_718.png";
            } else if (lower.contains("488") || lower.contains("spider") || lower.contains("sf90")) {
                matched = "Ferrari_SF90_Spider.png";
            } else if (lower.contains("720") || lower.contains("720s")) {
                matched = "Mclaren_720s.png";
            } else if (lower.contains("model_3") || lower.contains("model 3") || lower.contains("tesla")) {
                matched = "Model_3.png";
            } else if (lower.contains("m4") || lower.contains("bmw")) {
                matched = "M4_Competition.png";
            } else if (lower.contains("amg") || lower.contains("mercedes")) {
                matched = "Mercedes_AMG_GT.png";
            } else if (lower.contains("r8") || lower.contains("audi")) {
                matched = "Audi_R8.png";
            } else if (lower.contains("vantage") || lower.contains("aston")) {
                matched = "Aston_Martin_Vantage.png";
            } else if (lower.contains("mustang") || lower.contains("ford")) {
                matched = "Ford_Mustang_GT.png";
            } else if (lower.contains("gtr") || lower.contains("nissan")) {
                matched = "Nissan_GTR.png";
            } else if (lower.contains("corvette")) {
                matched = "Chevrolet_Corvette.png";
            } else if (lower.contains("p1")) {
                matched = "McLaren_P1.png";
            } else if (lower.contains("senna")) {
                matched = "McLaren_Senna.png";
            } else if (lower.contains("supra")) {
                matched = "Toyota_GR_Supra.png";
            } else if (lower.contains("koenigsegg")) {
                matched = "Koenigsegg.png";
            }

            if (matched != null) {
                img = getImageObjFromPath("/com/example/car_rental_sys/image/cars/" + matched);
                if (img != null && !img.isError()) {
                    return img;
                }
            }
        }

        // 3. Robust fallback to default supercar
        Image fallback = getImageObjFromPath("/com/example/car_rental_sys/image/cars/Huracan_EVI.png");
        if (fallback != null && !fallback.isError()) {
            return fallback;
        }
        return getImageObjFromPath("/com/example/car_rental_sys/image/cars/Porsche_718.png");
    }
}
