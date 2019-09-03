package com.gluonhq.picluster.mobile.views;

import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.picluster.mobile.Main;
import java.util.ResourceBundle;
import javafx.fxml.FXML;

public class IntroPresenter extends GluonPresenter<Main> {

    @FXML
    private View intro;

    @FXML
    private ResourceBundle resources;
    
    public void initialize() {
        intro.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = getApp().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> 
                        getApp().getDrawer().open()));
                appBar.setTitleText("Intro");
                appBar.getActionItems().add(MaterialDesignIcon.SEARCH.button(e -> 
                        System.out.println("Search")));
            }
        });
    }
    
    @FXML
    void buttonClick() {
        AppViewManager.MAIN_VIEW.switchView();
    }
    
}
