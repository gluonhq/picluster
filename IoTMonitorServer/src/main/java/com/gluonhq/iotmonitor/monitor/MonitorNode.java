package com.gluonhq.iotmonitor.monitor;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.gluonhq.iotmonitor.monitor.Model.nodeMapper;
import static com.gluonhq.iotmonitor.monitor.Model.unresponsiveNodes;

public class MonitorNode extends BorderPane {

    private final List<String> nodeList = new ArrayList<>();

    private NotificationPane notificationPane;
    private FlowPane flowPane;
    private DataReader dr;

    public MonitorNode() {
        getStyleClass().add("monitor-node");
        dr = new DataReader();
        dr.startReading();
        flowPane = new FlowPane(Orientation.HORIZONTAL);
        flowPane.setPadding(new Insets(5));
        flowPane.setHgap(5);
        flowPane.setVgap(5);
        ScrollPane scrollPane = new ScrollPane();
        flowPane.prefWrapLengthProperty().bind(scrollPane.widthProperty().subtract(20));
        scrollPane.setContent(flowPane);
        setCenter(scrollPane);
        setLeft(setUpTopPane());

        notificationPane = new NotificationPane(this);
        final Action showNotification = new Action("Show All", e -> {
            final ListView<Node> unresponsiveNodeListView = new ListView<>(unresponsiveNodes);
            unresponsiveNodeListView.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Node item, boolean empty) {
                    if (empty) {
                        setText(null);
                    } else {
                        setText(formatTextForUnresponsiveNode(item));
                    }
                }
            });
            final Scene scene = new Scene(new StackPane(unresponsiveNodeListView), 500, 500);
            final Stage notificationStage = new Stage();
            notificationStage.setTitle("Unresponsive Devices");
            notificationStage.setScene(scene);
            notificationStage.show();
            notificationPane.hide();
        });

        unresponsiveNodes.addListener((InvalidationListener) o -> {
            if (unresponsiveNodes.isEmpty()) {
                notificationPane.hide();
            } else {
                if (unresponsiveNodes.size() > 2) {
                    notificationPane.getActions().setAll(showNotification);
                }
                notificationPane.show();
            }
        });

        notificationPane.textProperty().bind(Bindings.createStringBinding(() ->
                unresponsiveNodes.stream()
                        .limit(2)
                        .map(this::formatTextForUnresponsiveNode)
                        .reduce("", (s1, s2) -> s1 + s2), unresponsiveNodes)
        );


        nodeMapper.addListener((MapChangeListener<String, Node>) change -> {
            if (change.wasAdded()) {
                final Node node = change.getValueAdded();

                NodeView view = new NodeView(node);
                view.setScaleX(0.6);
                view.setScaleY(0.6);
                nodeList.add(node.getId());
                Collections.sort(nodeList);
                int index = nodeList.indexOf(view.getNodeId());
                flowPane.getChildren().add(index, new Group(view));
            } else if (change.wasRemoved()) {
                flowPane.getChildren().removeIf(n -> {
                    if (n instanceof NodeView) {
                        nodeList.remove(((NodeView) n).getNodeId());
                        return ((NodeView) n).getNodeId().equals(change.getValueRemoved().getId());
                    }
                    return false;
                });
            }
        });
        getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

    }

    public void stop() {
        if (dr != null) {
            dr.stopReading();
        }
        nodeMapper.forEach((k, v) -> v.stop());
    }

    private GridPane setUpTopPane() {

        Counter totalNodeCounter = new Counter("Total Nodes");
        Counter unresponsiveNodeCounter = new Counter("Unresponsive\nNodes");
        final Button fixAll = new Button("Fix All");
        fixAll.setGraphic(FontIcon.of(Material.FLASH_ON, 18));
        fixAll.setOnAction(e -> unresponsiveNodes.clear());

        final GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("top-pane");

        javafx.scene.Node[] gridNodes = {totalNodeCounter, unresponsiveNodeCounter, fixAll};
        for (int i = 0; i < gridNodes.length; i++) {
            gridPane.add(gridNodes[i], 0, i);
            GridPane.setVgrow(gridNodes[i], Priority.ALWAYS);
            GridPane.setValignment(gridNodes[i], VPos.CENTER);
        }

        totalNodeCounter.countLabel.textProperty().bind(Bindings.concat("").concat(Bindings.size(nodeMapper)));
        unresponsiveNodeCounter.countLabel.textProperty().bind(Bindings.concat("").concat(Bindings.size(unresponsiveNodes)));

        return gridPane;
    }

    private String formatTextForUnresponsiveNode(Node item) {
        return String.format("Device %s needs powercycle.\n", item.lastKnownIp().get());
    }
}
