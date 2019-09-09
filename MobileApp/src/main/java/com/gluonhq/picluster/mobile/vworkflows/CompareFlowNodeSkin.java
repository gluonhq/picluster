package com.gluonhq.picluster.mobile.vworkflows;

import com.gluonhq.charm.glisten.control.DropdownButton;
import eu.mihosoft.vrl.workflow.VFlow;
import eu.mihosoft.vrl.workflow.VNode;
import eu.mihosoft.vrl.workflow.fx.FXSkinFactory;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

public class CompareFlowNodeSkin extends CustomFXFlowNodeSkin {

    public CompareFlowNodeSkin(FXSkinFactory skinFactory, VNode model, VFlow controller) {
        super(skinFactory, model, controller);
    }

    @Override
    protected Node createView() {
        DropdownButton button = new DropdownButton();
        for (CompareValue.TEST op : CompareValue.TEST.values()) {
            MenuItem item = new MenuItem(op.getTest());
            item.getProperties().put("TEST", op);
            button.getItems().add(item);
        }
        button.selectedItemProperty().addListener((obs, ov, nv) ->
                ((CompareValue) getModel().getValueObject().getValue())
                        .setValue((CompareValue.TEST) nv.getProperties().get("TEST")));
        return button;
    }
}
