package com.gluonhq.iotmonitor.monitor;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;

import java.util.Optional;

import static com.gluonhq.iotmonitor.monitor.Node.THRESHOLD_PING_TIME;

public class Main extends Application {

    static boolean TEST_MODE = false;

    private NotificationPane notificationPane;
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

        notificationPane = new NotificationPane(bp);
        ObservableList<String> notificationPaneTexts = FXCollections.observableArrayList();
        final Action notificationAction = new Action("Show All", e -> {
            final Scene scene = new Scene(new StackPane(new ListView<>(notificationPaneTexts)), 500, 500);
            final Stage notificationStage = new Stage();
            notificationStage.setTitle("Unresponsive Devices");
            notificationStage.setScene(scene);
            notificationStage.show();
            notificationPane.hide();
        });
        notificationPaneTexts.addListener((InvalidationListener) o -> {
            if (notificationPaneTexts.isEmpty()) {
                notificationPane.hide();
            } else {
                if (notificationPaneTexts.size() > 2) {
                    notificationPane.getActions().setAll(notificationAction);
                }
                notificationPane.show();
            }
        });

        notificationPane.textProperty().bind(Bindings.createStringBinding(() -> {
            return notificationPaneTexts.stream().limit(2).reduce("", (s1, s2) -> s1 + s2);
        }, notificationPaneTexts));
        
        notificationPane.addEventHandler(NotificationPane.ON_HIDING, e -> {
            notificationPaneTexts.clear();
        });

        Scene scene = new Scene(notificationPane, 640, 480);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        Model.nodeMapper.addListener((MapChangeListener<String, Node>) change -> {
            if (change.wasAdded()) {
                final Node node = change.getValueAdded();
                final StringBinding elapsedTime = Bindings.createStringBinding(() -> {
                    return String.format("Device %s needs powercycle.\n", node.lastKnownIp().get());
                }, node.lastKnownIp());
                node.elapsedTime().addListener((observable, oldValue, newValue) -> {
                    if (newValue.intValue() > THRESHOLD_PING_TIME) {
                        if (!notificationPaneTexts.contains(elapsedTime.getValue())) {
                            notificationPaneTexts.add(elapsedTime.getValue());
                        }
                    } else {
                        notificationPaneTexts.remove(elapsedTime.getValue());
                    }
                });

                NodeView view = new NodeView(node);
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
