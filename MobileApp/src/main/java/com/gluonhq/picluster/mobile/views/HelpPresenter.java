package com.gluonhq.picluster.mobile.views;

import com.gluonhq.charm.down.Platform;
import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.DisplayService;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.picluster.mobile.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.ResourceBundle;

public class HelpPresenter extends GluonPresenter<MainApp> {

    @FXML private View help;
    @FXML private Label helpLabel;
    @FXML private HBox topBox;

    @FXML
    private ResourceBundle resources;
    
    public void initialize() {
        help.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                getApp().getAppBar().setVisible(false);
            }
        });

        helpLabel.setOnMouseClicked(e -> getApp().goHome());

        if (Platform.isIOS() && ! topBox.getStyleClass().contains("ios")) {
            topBox.getStyleClass().add("ios");
        }
        boolean notch = Services.get(DisplayService.class).map(DisplayService::hasNotch).orElse(false);
        if (notch && ! topBox.getStyleClass().contains("notch")) {
            topBox.getStyleClass().add("notch");
        }
    }
    
}
