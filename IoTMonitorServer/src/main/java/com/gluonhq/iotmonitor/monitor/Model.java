package com.gluonhq.iotmonitor.monitor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;


public class Model {

    static ObservableMap<String, Node> nodeMapper = FXCollections.observableHashMap();

    static Node getNodeById(String s) {
        return nodeMapper.computeIfAbsent(s, id -> new Node(s));
    }

}
