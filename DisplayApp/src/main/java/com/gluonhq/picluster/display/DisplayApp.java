package com.gluonhq.picluster.display;

import com.gluonhq.iotmonitor.monitor.MonitorNode;
import com.gluonhq.picluster.display.model.Chunk;
import com.gluonhq.picluster.display.service.Service;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class DisplayApp extends Application {

    private static final int PHOTOS = 26;
    private static final int SIZE = 1024;

    private StackPane imagePane;
    private GridPane gridPane;
    private int SIZE_X, SIZE_Y;
    private volatile boolean run;
    private final AtomicLong counter = new AtomicLong(-1);
    private Service service;
    private int currentPic = -1;

    @Override
    public void start(Stage primaryStage) {
        double x = Screen.getScreens().stream()
                .mapToDouble(s -> s.getBounds().getMinX())
                .min().orElse(0);
        double y = Screen.getScreens().stream()
                .mapToDouble(s -> s.getBounds().getMinY())
                .min().orElse(0);
        double w = Screen.getScreens().stream()
                .mapToDouble(s -> s.getBounds().getMaxX())
                .max().orElse(0) - x;
        double h = Screen.getScreens().stream()
                .mapToDouble(s -> s.getBounds().getMaxY())
                .max().orElse(0) - y;

        imagePane = new StackPane();
        imagePane.getStyleClass().add("content");

        MonitorNode monitor = new MonitorNode();
        monitor.setMinHeight(h / 3d);
        monitor.setPrefHeight(h / 3d);
        monitor.setMaxHeight(h / 3d);
        monitor.setPrefWidth(w);

        StackPane stackPane = new StackPane(imagePane);
        stackPane.getChildren().add(monitor);
        StackPane.setAlignment(monitor, Pos.BOTTOM_CENTER);

        Scene scene = new Scene(stackPane, w, h);
        scene.setCursor(Cursor.NONE);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);

        primaryStage.show();
        primaryStage.setX(x);
        primaryStage.setY(y);

        setRandomPhoto();

        service = new Service();
        service.start(this::processImageChunk);
    }

    private void splitImage(Image fullImage) {
        PixelReader reader = fullImage.getPixelReader();

        if (fullImage.getWidth() < fullImage.getHeight()) {
            SIZE_X = (int) Math.floor(Math.sqrt(SIZE));
            SIZE_Y = SIZE / SIZE_X;
        } else {
            SIZE_Y = (int) Math.floor(Math.sqrt(SIZE));
            SIZE_X = SIZE / SIZE_Y;
        }
        int width = (int) (fullImage.getWidth() / SIZE_X);
        int height = (int) (fullImage.getHeight() / SIZE_Y);
        gridPane = new GridPane();
        for (int i = 0; i < SIZE_X; i++) {
            for (int j = 0; j < SIZE_Y; j++) {
                WritableImage wImage = new WritableImage(reader, i * width, j * height, width, height);

                ImageView imageView = new ImageView(wImage);
                imageView.setFitWidth(width);
                imageView.setPreserveRatio(true);

                Rectangle cover = new Rectangle(width, height, Color.BLACK);
                StackPane stackPane = new StackPane(imageView, cover);
                gridPane.add(stackPane, i, j);
            }
        }
        imagePane.getChildren().setAll(new Group(gridPane));
    }

    private void setRandomPhoto() {
        int pic = new Random().nextInt(PHOTOS) + 1;
        if (pic == currentPic) {
            setRandomPhoto();
        }
        currentPic = pic;
        System.out.println("pic = " + pic);
        Image image = new Image(DisplayApp.class.getResourceAsStream(String.format("/photos/pic%02d.jpg", currentPic)));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(imagePane.getScene().getWidth());
        imageView.setPreserveRatio(true);
        imagePane.getChildren().setAll(imageView);

        splitImage(imagePane.snapshot(null, null));
    }

    private double checkImageOverlay() {
        return gridPane.getChildren().stream()
                .filter(StackPane.class::isInstance)
                .map(StackPane.class::cast)
                .mapToDouble(n ->
                        n.getChildren().get(1).getOpacity())
                .sum() / (double) (SIZE_X * SIZE_Y);
    }

    private Node getCover(Chunk chunk) {
        return gridPane.getChildren().stream()
                .filter(StackPane.class::isInstance)
                .filter(n -> GridPane.getColumnIndex(n) == chunk.getX() &&
                        GridPane.getRowIndex(n) == chunk.getY())
                .findFirst()
                .map(StackPane.class::cast)
                .map(n -> n.getChildren().get(1))
                .orElse(null);
    }

    private void processImageChunk(Chunk chunk) {
        final Node cover = getCover(chunk);
        if (cover == null) {
            return;
        }

        double coverOpacity = cover.getOpacity();
        if (coverOpacity == 0.0) {
            processImageChunk(Chunk.getNextChunk(chunk, SIZE_X, SIZE_Y));
        }
        Platform.runLater(() -> {
            cover.setOpacity(Math.max(0, coverOpacity - chunk.getOpacity()));
            double totalOpacity = checkImageOverlay();
            System.out.println("totalOpacity = " + totalOpacity);
            if (totalOpacity < 0.01) {
                setRandomPhoto();
            }
        });
    }

    private WritableImage processImagePixels(Image chunk, double opacity) {
        PixelReader pixelReader = chunk.getPixelReader();
        int width = (int) chunk.getWidth();
        int height = (int) chunk.getHeight();
        WritableImage wImage = new WritableImage(width, height);
        PixelWriter pWriter = wImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                Color newColor = Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
                pWriter.setColor(x, y, newColor);
            }
        }
        return wImage;
    }

    @Override
    public void stop() {
        if (service != null) {
            service.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
