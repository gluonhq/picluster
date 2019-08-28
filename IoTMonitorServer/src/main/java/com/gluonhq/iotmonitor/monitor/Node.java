package com.gluonhq.iotmonitor.monitor;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Node {

    static final int THRESHOLD_PING_TIME = 30;

    private final Stat stat;
    private double uptime;
    private final LongProperty lastPing = new SimpleLongProperty(0);
    private final LongProperty elapsedTime = new SimpleLongProperty(0);
    private final String id;
    private final StringProperty lastKnownIp = new SimpleStringProperty("unknown");

    private final AnimationTimer at;

    // needed for sending messages to the IOT Device
    // proxy.id SHOULD match this.id
    private DataReader.NodeProxy proxy;

    class Stat {
        DoubleProperty cpu = new SimpleDoubleProperty();
        DoubleProperty mem = new SimpleDoubleProperty();
        DoubleProperty temp = new SimpleDoubleProperty();
    }

    public Node (String id) {
        this.id = id;
        this.stat = new Stat();
        at = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long diff = (now - lastPing.get()) / 1_000_000_000L;
                elapsedTime.set(diff);
                if (diff > 30) {
                    at.stop();
                }
            }
        };
        this.lastPing.addListener(o -> {
//            System.err.println("NEW PING DETECTED");
            at.stop();
            at.start();
        });
    }

    public String getId() {
        return this.id;
    }

    public Stat getStat() {
        return this.stat;
    }

    public StringProperty lastKnownIp() {
        return lastKnownIp;
    }

    public LongProperty lastPing() {
        return lastPing;
    }

    public LongProperty elapsedTime() {
        return elapsedTime;
    }

    public void setProxy(DataReader.NodeProxy nodeProxy) {
        this.proxy = nodeProxy;
    }

    public DataReader.NodeProxy getProxy() {
        return this.proxy;
    }

    public void stop() {
        if (at != null) {
            at.stop();
        }
    }
    
    public BooleanBinding unresponsiveProperty() {
        return elapsedTime.greaterThan(THRESHOLD_PING_TIME);
    }
    
    public boolean isUnresponsive() {
        return unresponsiveProperty().get();
    }
}
