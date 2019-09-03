/*
 * Copyright 2012-2016 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * Please cite the following publication(s):
 *
 * M. Hoffer, C.Poliwoda, G.Wittum. Visual Reflection Library -
 * A Framework for Declarative GUI Programming on the Java Platform.
 * Computing and Visualization in Science, 2011, in press.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
 */
package com.gluonhq.picluster.mobile.views.helper;

import eu.mihosoft.vrl.workflow.Connection;
import eu.mihosoft.vrl.workflow.Connector;
import eu.mihosoft.vrl.workflow.VFlow;
import eu.mihosoft.vrl.workflow.fx.ConnectorShape;
import eu.mihosoft.vrl.workflow.fx.FXSkinFactory;
import eu.mihosoft.vrl.workflow.fx.FontUtil;
import eu.mihosoft.vrl.workflow.skin.ConnectionSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;


/**
 * Circle node that represents a connector.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class CustomConnector extends HBox implements ConnectorShape {

    private Connector connector;
    private final VFlow flow;
    private final FXSkinFactory skinFactory;
    private ConnectionSkin connectionSkin;
    private final DoubleProperty radiusProperty = new SimpleDoubleProperty();
    private final Circle circle = new Circle();
    private final Pane textPane = new Pane();
    private final Label legendLabel = new Label();

    public CustomConnector(VFlow flow, FXSkinFactory skinFactory, Connector connector) {
        setConnector(connector);
        this.flow = flow;
        this.skinFactory = skinFactory;

        init();
    }

    public CustomConnector(VFlow flow, FXSkinFactory skinFactory, Connector connector, double radius) {
        radiusProperty.set(radius);
        setConnector(connector);
        this.flow = flow;
        this.skinFactory = skinFactory;

        init();
    }

    public CustomConnector(VFlow flow, FXSkinFactory skinFactory) {
        this.flow = flow;
        this.skinFactory = skinFactory;
        init();
    }
    
    private void init() {
        this.getStyleClass().add("vnode-connector");
        this.setManaged(true);
        setCacheShape(true);
        setCache(true);
        setCacheHint(CacheHint.SPEED);

        radiusProperty().addListener((ov,oldV,newV) -> requestLayout());
        spacingProperty().bind(radiusProperty.divide(4));
        circle.radiusProperty().bind(radiusProperty);
        circle.getStyleClass().add("connector");

        textPane.getStyleClass().add("text-connector");
        textPane.getChildren().add(legendLabel);
        textPane.setManaged(false);

        getChildren().addAll(circle, textPane);
    }

    @Override
    public Connector getConnector() {
        return connector;
    }

    @Override
    public final void setConnector(Connector connector) {
        
        if (getConnector() != null) {
            circle.getStyleClass().remove(getConnector().getType());
        }
        
        this.connector = connector;
        
        if (getConnector() != null) {
            circle.getStyleClass().add(getConnector().getType());
        }
        
    }
    
    private void moveConnectionReceiverToFront() {
        connectionSkin = null;

        if (connector.isInput() && flow.getConnections(connector.getType()).isInputConnected(connector)) {
            for (Connection conn : flow.getConnections(connector.getType()).getConnections()) {
                ConnectionSkin connectionSkin = flow.getNodeSkinLookup().getById(skinFactory, conn);
                if (connectionSkin != null) {
                    this.connectionSkin = connectionSkin;
                    connectionSkin.receiverToFront();
                }
            }
        }
    }
    
    @Override
    public void toFront() {
        super.toFront();
        moveConnectionReceiverToFront();
    }

    @Override
    public DoubleProperty radiusProperty() {
        return radiusProperty;
    }
    
    @Override
    public void setRadius(double radius) {
        radiusProperty().set(radius);
    }
    
    @Override
    public double getRadius() {
        return radiusProperty().get();
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    protected void layoutChildren() {
        double lw = 0d;
        double p = 0d;
        if (getConnector().getLegend() != null) {
            legendLabel.setText(getConnector().getLegend());
            lw = FontUtil.computeStringWidth(legendLabel.getFont(), legendLabel.getText());
            p = getSpacing();
        }
        double r = getRadius() + circle.getStrokeWidth() / 2d;
        double tw = lw + p;
        resize(2d * r + tw, 2d * r);
        if (getConnector().isInput()) {
            textPane.relocate(r * 2d + p, 0);
        } else {
            textPane.relocate(- tw, 0);
        }
        super.layoutChildren();
    }
}
