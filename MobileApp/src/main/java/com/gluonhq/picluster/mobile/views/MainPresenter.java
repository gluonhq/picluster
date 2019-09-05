package com.gluonhq.picluster.mobile.views;

import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.connect.GluonObservableObject;
import com.gluonhq.picluster.mobile.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.gluonhq.picluster.mobile.model.Block;
import com.gluonhq.picluster.mobile.model.Model;
import com.gluonhq.picluster.mobile.service.Service;
import com.gluonhq.picluster.mobile.views.helper.CompareFlowNodeSkin;
import com.gluonhq.picluster.mobile.views.helper.CompareValue;
import com.gluonhq.picluster.mobile.views.helper.DoubleFlowNodeSkin;
import com.gluonhq.picluster.mobile.views.helper.DoubleValue;
import com.gluonhq.picluster.mobile.views.helper.FunctionFlowNodeSkin;
import com.gluonhq.picluster.mobile.views.helper.FunctionValue;
import com.gluonhq.picluster.mobile.views.helper.OperatorFlowNodeSkin;
import com.gluonhq.picluster.mobile.views.helper.OperatorValue;
import com.gluonhq.picluster.mobile.views.helper.StringFlowNodeSkin;
import eu.mihosoft.vrl.workflow.Connection;
import eu.mihosoft.vrl.workflow.Connector;
import eu.mihosoft.vrl.workflow.DefaultValueObject;
import eu.mihosoft.vrl.workflow.FlowFactory;
import eu.mihosoft.vrl.workflow.VFlow;
import eu.mihosoft.vrl.workflow.VNode;
import eu.mihosoft.vrl.workflow.ValueObject;
import eu.mihosoft.vrl.workflow.VisualizationRequest;
import eu.mihosoft.vrl.workflow.fx.FXValueSkinFactory;
import eu.mihosoft.vrl.workflow.fx.ScalableContentPane;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javax.inject.Inject;

public class MainPresenter extends GluonPresenter<Main> {

    private final static boolean TEST_MODE = "test".equalsIgnoreCase(System.getenv("picluster_mode"));

    @FXML private View main;
    @FXML private Button loopButton;
    @FXML private Button ifButton;
    @FXML private Button varButton;
    @FXML private Button numButton;
    @FXML private Button mathButton;
    @FXML private Button functionButton;

    @FXML private ResourceBundle resources;

    @Inject private Service service;

    private VFlow flow;

    public void initialize() {
        main.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = getApp().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e ->
                        getApp().getDrawer().open()));
                appBar.setTitleText("Main");
                appBar.getActionItems().add(MaterialDesignIcon.SEND.button(e ->
                        send()));
            }
        });

        // create scalable root pane
        ScalableContentPane canvas = new ScalableContentPane();
        canvas.getStyleClass().setAll("vflow-background");
        canvas.setMinScaleX(0.5);
        canvas.setMinScaleY(0.5);
        canvas.setMaxScaleX(1.0);
        canvas.setMaxScaleY(1.0);

        flow = FlowFactory.newFlow();
        flow.setVisible(true);

        FXValueSkinFactory fXSkinFactory = new FXValueSkinFactory(canvas);
        fXSkinFactory.addSkinClassForValueType(OperatorValue.class, OperatorFlowNodeSkin.class);
        fXSkinFactory.addSkinClassForValueType(FunctionValue.class, FunctionFlowNodeSkin.class);
        fXSkinFactory.addSkinClassForValueType(CompareValue.class, CompareFlowNodeSkin.class);
        fXSkinFactory.addSkinClassForValueType(DoubleValue.class, DoubleFlowNodeSkin.class);
        fXSkinFactory.addSkinClassForValueType(String.class, StringFlowNodeSkin.class);
        flow.setSkinFactories(fXSkinFactory);

        main.setCenter(canvas);

        numButton.setOnAction(e -> {
            VNode n1 = createNode("0", "Number");
            addOutputConnector(n1, "data", true);
        });

        varButton.setOnAction(e -> {
            VNode n1 = createNode("a", "Variable");
            addInputConnector(n1, "data", true);
            addOutputConnector(n1, "data", true);
        });

        loopButton.setOnAction(e -> {
            VNode n1 = createNode(null, "For loop");
            addInputConnector(n1, "data", true);
            addOutputConnector(n1, "data", true);
        });

        ifButton.setOnAction(e -> {
            VNode n1 = createNode(new CompareValue("Compare"), "Compare");
            addInputConnector(n1, "data", true);
            addInputConnector(n1, "data", false);
            addOutputConnector(n1, "data", true).setLegend("TRUE");
            addOutputConnector(n1, "data", false).setLegend("FALSE");
        });

        mathButton.setOnAction(e -> {
            VNode n1 = createNode(new OperatorValue("Operator"), "Math");
            addInputConnector(n1, "data", true);
            addInputConnector(n1, "data", false);
            addOutputConnector(n1, "data", true);
        });
        functionButton.setOnAction(e -> {
            VNode n1 = createNode(new FunctionValue("Function"), "Functions");
            addInputConnector(n1, "data", true);
            addOutputConnector(n1, "data", true);
        });
    }

    private VNode createNode(Object object, String title) {
        ValueObject valueObject = new DefaultValueObject();
        if (object != null) {
            valueObject.setValue(object);
        }
        VNode n1 = flow.newNode(valueObject);
        int size = flow.getNodes().size();
        n1.setX(size * 10);
        n1.setY(size * 10);
        n1.setTitle(title);
        return n1;
    }

    private Connector addInputConnector(VNode node, String type, boolean main) {
        Connector c1i = node.addInput(type);
        c1i.getVisualizationRequest().set(VisualizationRequest.KEY_CONNECTOR_AUTO_LAYOUT, true);
        if (main) {
            node.setMainInput(c1i);
        }
        return c1i;
    }
    private Connector addOutputConnector(VNode node, String type, boolean main) {
        Connector c1o = node.addOutput(type);
        c1o.getVisualizationRequest().set(VisualizationRequest.KEY_CONNECTOR_AUTO_LAYOUT, true);
        if (main) {
            node.setMainOutput(c1o);
        }
        return c1o;
    }

    private void send() {
        // serialize and send to GCL with RF
        Model model = new Model();
        List<Block> blocks = new ArrayList<>();
        for (VNode n : flow.getNodes()) {
            Block block = new Block(n.getId(), n.getTitle());
            List<String> inputs = new ArrayList<>();
            for (Connector c : n.getInputs()) {
                if (flow.getConnections(c.getType()).isInputConnected(c)) {
                    for (Connection conn : flow.getConnections(c.getType()).getAllWith(c)) {
                        inputs.add(conn.getSender().getNode().getId());
                    }
                }
            }
            block.setInputs(inputs);
            List<String> outputs = new ArrayList<>();
            for (Connector c : n.getOutputs()) {
                if (flow.getConnections(c.getType()).isOutputConnected(c)) {
                    for (Connection conn : flow.getConnections(c.getType()).getAllWith(c)) {
                        outputs.add(conn.getSender().getNode().getId());
                    }
                }
            }
            block.setOutputs(outputs);
            Object v = n.getValueObject().getValue();
            if (v instanceof CompareValue) {
                block.setValue(((CompareValue) v).getValue().toString());
            } else if (v instanceof OperatorValue) {
                block.setValue(((OperatorValue) v).getValue().toString());
            } else {
                block.setValue(v.toString());
            }
            blocks.add(block);
        }
        model.setBlocks(blocks);
        System.out.println("model = " + model);
        if (TEST_MODE) {
            try {
                ProcessBuilder pb = new ProcessBuilder("curl", "http://127.0.0.1:8080/ID?text");
                pb.start();
            } catch (Exception e) {}
        } else {
            GluonObservableObject<String> modelGluonObservableObject = service.sendBlocks(model);
            modelGluonObservableObject.setOnSucceeded(e -> System.out.println("sent " + e));
            modelGluonObservableObject.exceptionProperty().addListener((obs, ov, nv) -> System.out.println("failed: " + nv));
        }
    }
}
