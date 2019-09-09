package com.gluonhq.picluster.mobile.vworkflows;

import eu.mihosoft.vrl.workflow.VFlow;
import eu.mihosoft.vrl.workflow.VNode;
import eu.mihosoft.vrl.workflow.fx.FXSkinFactory;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class StringFlowNodeSkin extends CustomFXFlowNodeSkin {

    public StringFlowNodeSkin(FXSkinFactory skinFactory, VNode model, VFlow controller) {
        super(skinFactory, model, controller);
    }

    @Override
    protected Node createView() {
        // value
        String value = getModel().getValueObject().getValue().toString();
        TextField textField = new TextField(value);
        textField.setMinWidth(50);
        textField.setPrefWidth(50);
        textField.setMaxWidth(50);
        textField.textProperty().addListener((obs, ov, nv) ->
                getModel().getValueObject().setValue(nv));
        return new StackPane(textField);
    }
}
