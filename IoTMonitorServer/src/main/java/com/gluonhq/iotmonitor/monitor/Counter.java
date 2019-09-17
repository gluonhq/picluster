package com.gluonhq.iotmonitor.monitor;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class Counter extends VBox {

    final Label countLabel;

    Counter( String title ) {
        countLabel = new Label();
        countLabel.setStyle("-fx-font-size: 3em; -fx-text-fill: #ffffffc0;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: .9em; -fx-text-fill: #ffffffc0;");
        setSpacing(10);
        getChildren().addAll(countLabel, titleLabel);
        setAlignment(Pos.CENTER);
    }

}
