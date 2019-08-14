package com.gluonhq.iotmonitor.monitor;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Random;

public class Node {

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

        DoubleProperty cpu() {
            return cpu;
        }

        DoubleProperty mem() {
            return mem;
        }
    }

    public Node (String id) {
        this.id = id;
        this.stat = new Stat();
        at = new AnimationTimer() {

            long lastUpdate;
            long lastUpdatedSecond;

            @Override
            public void start() {
                lastUpdate = System.nanoTime();
                lastUpdatedSecond = 0;
                super.start();
            }

            @Override
            public void handle(long now) {
                long diff = (now - lastPing.get()) / 1_000_000_000L;
                elapsedTime.set(diff);
                if (diff > 30) {
                    at.stop();
                }


                long elapsedNanoSeconds = now - lastUpdate ;
                long elapsedSeconds = elapsedNanoSeconds / 1_000_000_000L ; // Every sec

                System.out.println(elapsedSeconds);

                if (elapsedSeconds > lastUpdatedSecond) {
                    getStat().cpu.set(new Random().nextDouble() * 100);
                    getStat().mem.set(new Random().nextDouble() * 100);
                    lastUpdatedSecond = elapsedSeconds;
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
}
