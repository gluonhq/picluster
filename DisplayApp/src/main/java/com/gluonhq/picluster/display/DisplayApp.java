package com.gluonhq.picluster.display;

import com.gluonhq.picluster.display.model.Chunk;
import com.gluonhq.picluster.display.service.Service;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
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

import java.util.concurrent.atomic.AtomicLong;

public class DisplayApp extends Application {

    private static final int SIZE = 1024;

    private StackPane imagePane;
    private GridPane gridPane;
    private int SIZE_X, SIZE_Y;
    private volatile boolean run;
    private final AtomicLong counter = new AtomicLong(-1);
    private Service service;

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

        Image image = new Image(DisplayApp.class.getResourceAsStream("oracleboat.jpg"));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(w);
        imageView.setPreserveRatio(true);
        imagePane = new StackPane(imageView);

        Scene scene = new Scene(imagePane, w, h);
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
        primaryStage.setX(x);
        primaryStage.setY(y);

        splitImage(imageView.snapshot(null, null));

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

    private void processImageChunk(Chunk chunk) {
        Platform.runLater(() -> {
            gridPane.getChildren().stream()
                    .filter(StackPane.class::isInstance)
                    .map(StackPane.class::cast)
                    .filter(n -> GridPane.getColumnIndex(n) == chunk.getX() &&
                            GridPane.getRowIndex(n) == chunk.getY())
                    .findFirst()
                    .ifPresent(n -> {
                        Rectangle cover = (Rectangle) n.getChildren().get(1);
                        cover.setOpacity(Math.max(0, cover.getOpacity() - chunk.getOpacity()));
                    });
            counter.set(-1);
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
        service.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
