package com.gluonhq.picluster.display;

import com.gluonhq.picluster.display.model.Chunk;
import com.gluonhq.picluster.display.service.Service;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Random;
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
//        Button split = new Button("Start");
//        split.setOnAction(e -> {
//            run = ! run;
//            if (run) {
//                processTask();
//            }
//
//        });
//        StackPane.setAlignment(split, Pos.TOP_LEFT);
//        imagePane.getChildren().add(split);

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
                gridPane.add(imageView, i, j);
            }
        }
        imagePane.getChildren().setAll(new Group(gridPane));
    }

    private void processTask() {
        counter.set(-1);
        Thread thread = new Thread(() -> {
            long count = 0 ;
            while (run) {
                count++;
                if (counter.getAndSet(count) == -1) {
                    int chunkId_X = new Random().nextInt(SIZE_X);
                    int chunkId_Y = new Random().nextInt(SIZE_Y);
                    double opacity = 0.5 + (double) new Random().nextInt(SIZE) / (2d * SIZE);
                    processImageChunk(new Chunk(chunkId_X, chunkId_Y, opacity));
                    try {
                        Thread.sleep(10 + new Random().nextInt(10));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void processImageChunk(Chunk chunk) {
        Platform.runLater(() -> {
            gridPane.getChildren().stream()
                    .filter(ImageView.class::isInstance)
                    .map(ImageView.class::cast)
                    .filter(n -> GridPane.getColumnIndex(n) == chunk.getX() &&
                            GridPane.getRowIndex(n) == chunk.getY())
                    .findFirst()
//                .ifPresent(n -> n.setOpacity(opacity)));
                    .ifPresent(n -> {
                        Image newImage = processImagePixels(n.getImage(), chunk.getOpacity());
                        n.setImage(newImage);
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
