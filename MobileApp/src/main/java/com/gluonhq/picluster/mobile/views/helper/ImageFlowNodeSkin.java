package com.gluonhq.picluster.mobile.views.helper;

import eu.mihosoft.vrl.workflow.ConnectionEvent;
import eu.mihosoft.vrl.workflow.VFlow;
import eu.mihosoft.vrl.workflow.VNode;
import eu.mihosoft.vrl.workflow.fx.FXSkinFactory;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageFlowNodeSkin extends CustomFXFlowNodeSkin {

    private final ColorAdjust colorAdjust;
    private final VNode model;

    public ImageFlowNodeSkin(FXSkinFactory skinFactory, VNode model, VFlow controller) {
        super(skinFactory, model, controller);
        this.model = model;
        colorAdjust = new ColorAdjust(0, 0, 0, 0);
    }

    @Override
    protected Node createView() {
        Image image = (Image) getModel().getValueObject().getValue();
        ImageView imageView = new ImageView(image);
        model.getInputs().get(0).addConnectionEventListener(e -> {
                if (e.getEventType().equals(ConnectionEvent.ADD)) {
                    Object object = e.getSenderConnector().getNode().getValueObject().getValue();
                    if (object instanceof DoubleValue) {
                        DoubleValue value = (DoubleValue) object;
                        if ("Hue".equals(value.getTitle())) {
                            colorAdjust.hueProperty().bind(value.valueProperty().divide(50d).subtract(1d));
                            imageView.setEffect(colorAdjust);
                        }
                    }
                } else {
                    colorAdjust.hueProperty().unbind();
                    imageView.setEffect(null);
                }
            });

        model.getInputs().get(1).addConnectionEventListener(e -> {
            if (e.getEventType().equals(ConnectionEvent.ADD)) {
                Object object = e.getSenderConnector().getNode().getValueObject().getValue();
                if (object instanceof DoubleValue) {
                    DoubleValue value = (DoubleValue) object;
                    if ("Scale".equals(value.getTitle())) {
                        imageView.scaleXProperty().bind((value.valueProperty().divide(10d).subtract(4d)));
                        imageView.scaleYProperty().bind((value.valueProperty().divide(10d).subtract(4d)));
                    }
                }
            } else {
                imageView.scaleXProperty().unbind();
                imageView.scaleYProperty().unbind();
                imageView.setScaleX(1d);
                imageView.setScaleY(1d);
            }
        });

        return imageView;
    }
}