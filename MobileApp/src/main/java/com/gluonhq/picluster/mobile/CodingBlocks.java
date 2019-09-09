package com.gluonhq.picluster.mobile;

import com.gluonhq.picluster.mobile.views.AppViewManager;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.visual.Swatch;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class CodingBlocks extends MobileApplication {

    @Override
    public void init() {
        AppViewManager.registerViews(this);
    }

    @Override
    public void postInit(Scene scene) {
        Swatch.BLUE_GREY.assignTo(scene);

        scene.getStylesheets().add(CodingBlocks.class.getResource("style.css").toExternalForm());
        ((Stage) scene.getWindow()).getIcons().add(new Image(CodingBlocks.class.getResourceAsStream("/icon.png")));
    }
}
