package com.gluonhq.picluster.mobile.views;

import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.picluster.mobile.MainApp;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class IntroPresenter extends GluonPresenter<MainApp> {

    @FXML private View intro;
    @FXML private Label helpLabel;

    @FXML
    private ResourceBundle resources;
    
    public void initialize() {
        intro.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                getApp().getAppBar().setVisible(false);
            }
        });

        helpLabel.setOnMouseClicked(e -> AppViewManager.HELP_VIEW.switchView());
    }
    
    @FXML
    void buttonClick() {
        AppViewManager.MAIN_VIEW.switchView();
    }
    
}
