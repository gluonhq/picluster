package com.gluonhq.picluster.mobile.vworkflows;

import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import eu.mihosoft.vrl.workflow.ConnectionEvent;
import eu.mihosoft.vrl.workflow.Connector;
import eu.mihosoft.vrl.workflow.VFlow;
import eu.mihosoft.vrl.workflow.VNode;
import eu.mihosoft.vrl.workflow.fx.FXSkinFactory;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class ExecNodeSkin extends CustomFXFlowNodeSkin {

    public ExecNodeSkin(FXSkinFactory skinFactory, VNode model, VFlow controller) {
        super(skinFactory, model, controller);
    }

    @Override
    protected Node createView() {
        // value
        // TODO: enable if a math expression has been formed and it is valid
        Button send = MaterialDesignIcon.SEND.button(e ->
                ((ExecValue) getModel().getValueObject().getValue()).getValue().run());
        getModel().getConnectors().addListener((ListChangeListener<Connector>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    getModel().getConnectors().get(0).addConnectionEventListener(e ->
                            send.setDisable(e.getEventType() != ConnectionEvent.ADD));
                }
            }
        });
        send.setDisable(true);
        return new StackPane(send);
    }
}
