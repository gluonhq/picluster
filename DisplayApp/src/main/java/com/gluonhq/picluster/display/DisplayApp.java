package com.gluonhq.picluster.display;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

public class DisplayApp extends Application {

    private static final int SIZE = 8;
    private static final double scale = 0.9;

    private StackPane imagePane;

    @Override
    public void start(Stage primaryStage) {
        Image image = new Image(DisplayApp.class.getResourceAsStream("oracleboat.jpg"));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(image.getWidth() * scale);
        imageView.setPreserveRatio(true);
        imagePane = new StackPane(imageView);
        BorderPane root = new BorderPane(imagePane);
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        Button split = new Button("Split");
        split.setOnAction(e -> splitImage(image));
        root.setBottom(split);
    }

    private void splitImage(Image fullImage) {
        PixelReader reader = fullImage.getPixelReader();
        int SIZE_X = 0, SIZE_Y = 0;
        if (fullImage.getWidth() < fullImage.getHeight()) {
            SIZE_X = (int) Math.floor(Math.sqrt(SIZE));
            SIZE_Y = SIZE / SIZE_X;
        } else {
            SIZE_Y = (int) Math.floor(Math.sqrt(SIZE));
            SIZE_X = SIZE / SIZE_Y;
        }
        int width = (int) (fullImage.getWidth() / SIZE_X);
        int height = (int) (fullImage.getHeight() / SIZE_Y);

        GridPane pane = new GridPane();
        for (int i = 0; i < SIZE_X; i++) {
            for (int j = 0; j < SIZE_Y; j++) {
                double opacity = 0.5 + (double) new Random().nextInt(SIZE) / (double) (2d * SIZE);
                WritableImage wImage = processImageChunk(reader, width, height, i, j, opacity);

                ImageView imageView = new ImageView(wImage);
                imageView.setFitWidth(width * scale);
                imageView.setPreserveRatio(true);
                pane.add(imageView, i, j);
            }
        }

        imagePane.getChildren().setAll(new Group(pane));
    }

    private WritableImage processImageChunk(PixelReader reader, int width, int height, int i, int j, double opacity) {
        WritableImage newImage = new WritableImage(reader, i * width, j * height, width, height);
        PixelReader pixelReader = newImage.getPixelReader();
        WritableImage wImage = new WritableImage((int) newImage.getWidth(), (int) newImage.getHeight());
        PixelWriter pWriter = wImage.getPixelWriter();

        for (int y = 0; y < newImage.getHeight(); y++) {
            for (int x = 0; x < newImage.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                Color newColor = Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
                pWriter.setColor(x, y, newColor);
            }
        }
        return wImage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
