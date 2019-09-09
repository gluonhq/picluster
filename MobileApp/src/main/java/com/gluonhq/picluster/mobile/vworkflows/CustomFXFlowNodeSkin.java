package com.gluonhq.picluster.mobile.vworkflows;

import eu.mihosoft.vrl.workflow.Connector;
import eu.mihosoft.vrl.workflow.VFlow;
import eu.mihosoft.vrl.workflow.VFlowModel;
import eu.mihosoft.vrl.workflow.VNode;
import eu.mihosoft.vrl.workflow.fx.ConnectorShape;
import eu.mihosoft.vrl.workflow.fx.FXFlowNodeSkinBase;
import eu.mihosoft.vrl.workflow.fx.FXSkinFactory;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public abstract class CustomFXFlowNodeSkin extends FXFlowNodeSkinBase {


    public CustomFXFlowNodeSkin(FXSkinFactory skinFactory, VNode model, VFlow controller) {
        super(skinFactory, model, controller);
    }

    protected abstract Node createView();

    @Override
    public void updateView() {
        // we don't create custom view for flows
        if (getModel() instanceof VFlowModel) {
            return;
        }

        // we don't create a custom view if no value has been defined
        if (getModel().getValueObject().getValue() == null) {
            return;
        }

        getModel().setWidth(160);
        getModel().setHeight(100);


        // create the view
        Node view = createView();

        // add the view to scalable content pane
        if (view != null) {
            StackPane nodePane = new StackPane(view);
            nodePane.setPadding(new Insets(10));
            getNode().setContentPane(nodePane);
        }
    }

    @Override
    protected void addConnector(Connector connector) {
        super.addConnector(connector);
    }

    @Override
    protected ConnectorShape createConnectorShape(Connector connector) {
        return new CustomConnector(getController(), getSkinFactory(), connector, 20);
    }

}
