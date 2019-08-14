package com.gluonhq.iotmonitor.monitor;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class NodeView extends Region {

    private Node node;

    private Rectangle cpuView = new Rectangle(40, 120);
    private Rectangle memView = new Rectangle(40, 120);
    private Rectangle lastPingView = new Rectangle(84, 30);

    private Label elapsedTimeView = new Label(" -- ");
    private Button reboot = new Button("Reboot");

    public NodeView(Node node){
        this.node = node;
        createUI();
    }

    public String getNodeId() {
        return node.getId();
    }

    private void createUI() {
        HBox hbox = new HBox(4, cpuView, memView);
        StackPane elapsedPane = new StackPane(lastPingView, elapsedTimeView);
        VBox vbox = new VBox(4, hbox, elapsedPane, reboot);
        reboot.setOnAction((e) -> {
            System.err.println("I have to send reboot request");
            if (node.getProxy() != null) {
                node.getProxy().requestReboot();
                Model.nodeMapper.remove(node.getId());
            } else {
                System.err.println("Could not find proxy for node "+node);
            }
        });
        cpuView.setFill(Color.GRAY);
        memView.setFill(Color.GRAY);
        lastPingView.setFill(Color.GRAY);

        elapsedTimeView.textProperty().bind(node.elapsedTime().asString());
        this.node.getStat().cpu().addListener((obs, ov, nv) -> {
            double value = nv.doubleValue();
            cpuView.setHeight(value * 1.2);
            cpuView.setTranslateY(120 - cpuView.getHeight());
            if (value < 50d) {
                cpuView.setFill(Color.GREEN);
            } else if (value < 75d) {
                cpuView.setFill(Color.YELLOW);
            } else {
                cpuView.setFill(Color.RED);
            }
        });

        node.elapsedTime().addListener((obs, ov, nv) -> {
            long elapsed = nv.intValue();
            if (elapsed > 30) {
                // we didn't hear for 30 seconds
                lastPingView.setFill(Color.RED);
            } else if (elapsed > 10) {
                lastPingView.setFill(Color.YELLOW);
            } else {
                lastPingView.setFill(Color.GREEN);
            }
        });

        Label ipLabel = new Label();
        ipLabel.setFont(Font.font(10));
        ipLabel.textProperty().bind(node.lastKnownIp());
        Label idLabel = new Label("ID: " + node.getId());
        idLabel.setFont(Font.font(9));
        VBox infoBox = new VBox(4, idLabel, ipLabel);
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(5));
        borderPane.setCenter(vbox);
        borderPane.setTop(infoBox);
        this.getChildren().add(borderPane);
    }

}
