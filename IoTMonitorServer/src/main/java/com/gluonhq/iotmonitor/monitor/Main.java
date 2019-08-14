package com.gluonhq.iotmonitor.monitor;

import javafx.application.Application;
import javafx.collections.MapChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.util.Optional;

public class Main extends Application {

    static boolean TEST_MODE = false;

    private FlowPane flowPane;

    private DataReader dr;
    @Override
    public void start(Stage stage) {
        dr = new DataReader();
        dr.startReading();
        BorderPane bp = new BorderPane();
        flowPane = new FlowPane(Orientation.HORIZONTAL);
        ScrollPane scrollPane = new ScrollPane();
        flowPane.prefWrapLengthProperty().bind(scrollPane.widthProperty().subtract(20));
        scrollPane.setContent(flowPane);
        bp.setCenter(scrollPane);

        Scene scene = new Scene(bp, 640, 480);
        stage.setScene(scene);
        stage.show();

        Model.nodeMapper.addListener((MapChangeListener<String, Node>) change -> {
            if (change.wasAdded()) {
                NodeView view = new NodeView(change.getValueAdded());
                flowPane.getChildren().add(view);
            } else if (change.wasRemoved()) {
                Optional<NodeView> view = flowPane.getChildren().stream()
                        .filter(NodeView.class::isInstance)
                        .map(NodeView.class::cast)
                        .filter(n -> n.getNodeId().equals(change.getValueRemoved().getId()))
                        .findFirst();
                view.ifPresent(v -> flowPane.getChildren().remove(v));
            }
        });
    }

    @Override
    public void stop() {
        if (dr != null) {
            dr.stopReading();
        }
        Model.nodeMapper.forEach((k, v) -> v.stop());
    }

}
