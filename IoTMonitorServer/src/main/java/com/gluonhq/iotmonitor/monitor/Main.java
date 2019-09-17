package com.gluonhq.iotmonitor.monitor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    static boolean TEST_MODE = "test".equalsIgnoreCase(System.getenv("picluster_mode"));

    private MonitorNode root;

    @Override
    public void start(Stage stage) {
        root = new MonitorNode();
        Scene scene = new Scene(root, 640, 480);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        root.stop();
    }

}

