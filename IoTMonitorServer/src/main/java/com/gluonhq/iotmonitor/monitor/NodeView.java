package com.gluonhq.iotmonitor.monitor;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.colors.Bright;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material.Material;

import java.util.List;

import static com.gluonhq.iotmonitor.monitor.Main.TEST_MODE;

public class NodeView extends Region {

    private static final int HIGH_THRESHOLD   = TEST_MODE ? 7 : 20;
    private static final int MEDIUM_THRESHOLD = TEST_MODE ? 3 : 10;
    private static final PseudoClass PSEUDO_CLASS_LOW    = PseudoClass.getPseudoClass("low"); 
    private static final PseudoClass PSEUDO_CLASS_MEDIUM = PseudoClass.getPseudoClass("medium"); 
    private static final PseudoClass PSEUDO_CLASS_HIGH   = PseudoClass.getPseudoClass("high");
    private List<PseudoClass> states = List.of(PSEUDO_CLASS_LOW, PSEUDO_CLASS_MEDIUM, PSEUDO_CLASS_HIGH);

    private Node node;

    private Tile cpuView = TileBuilder.create()
            .skinType(SkinType.BAR_GAUGE)
            .prefSize(100, 100)
            .minValue(0)
            .maxValue(100)
            .startFromZero(true)
            .title("CPU")
            .gradientStops(new Stop(0, Bright.GREEN),
                    new Stop(0.4, Bright.YELLOW),
                    new Stop(0.8, Bright.RED))
            .strokeWithGradient(true)
            .build();

    private Tile memView = TileBuilder.create()
            .skinType(SkinType.BAR_GAUGE)
            .prefSize(100, 100)
            .minValue(0)
            .maxValue(100)
            .startFromZero(true)
            .title("MEMORY")
            .gradientStops(new Stop(0, Bright.GREEN),
                    new Stop(0.4, Bright.YELLOW),
                    new Stop(0.8, Bright.RED))
            .strokeWithGradient(true)
            .build();
    
    private Label elapsedTimeView = new Label(" -- ");
    private Button reboot = new Button();

    public NodeView(Node node){
        this.node = node;
        createUI();
    }

    public String getNodeId() {
        return node.getId();
    }

    private void createUI() {
        HBox upperBox = new HBox(cpuView, memView);
        upperBox.getStyleClass().add("upper-box");

        StackPane elapsedPane = new StackPane(elapsedTimeView);
        elapsedPane.getStyleClass().add("elapsed-pane");
        HBox.setHgrow(elapsedPane, Priority.ALWAYS);
        HBox lowerBox = new HBox(elapsedPane, reboot);
        lowerBox.getStyleClass().add("lower-box");

        VBox vbox = new VBox(upperBox, lowerBox);
        vbox.getStyleClass().add("container");
        reboot.setGraphic(FontIcon.of(Material.REFRESH, 20));
        reboot.setOnAction((e) -> {
            System.err.println("I have to send reboot request");
            if (node.getProxy() != null) {
                node.getProxy().requestReboot();
                Model.nodeMapper.remove(node.getId());
            } else {
                System.err.println("Could not find proxy for node " + node);
            }
        });

        cpuView.valueProperty().bind(node.getStat().cpu());
        memView.valueProperty().bind(node.getStat().mem());
        elapsedTimeView.textProperty().bind(node.elapsedTime().asString());

        node.elapsedTime().addListener((obs, ov, nv) -> {
            elapsedPane.pseudoClassStateChanged(PSEUDO_CLASS_HIGH, false);
            elapsedPane.pseudoClassStateChanged(PSEUDO_CLASS_MEDIUM, false);
            elapsedPane.pseudoClassStateChanged(PSEUDO_CLASS_LOW, false);
            
            long elapsed = nv.intValue();
            if (elapsed > HIGH_THRESHOLD) {
                // we didn't hear for 30 seconds
                elapsedPane.pseudoClassStateChanged(PSEUDO_CLASS_HIGH, true);
            } else if (elapsed > MEDIUM_THRESHOLD) {
                elapsedPane.pseudoClassStateChanged(PSEUDO_CLASS_MEDIUM, true);
            } else {
                elapsedPane.pseudoClassStateChanged(PSEUDO_CLASS_LOW, true);
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
