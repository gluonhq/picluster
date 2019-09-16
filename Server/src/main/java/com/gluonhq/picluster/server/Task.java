package com.gluonhq.picluster.server;

import java.util.UUID;

import com.gluonhq.picluster.mobile.model.GluonObject;

public class Task {

    final String id;
    String url;
    String answer;
    boolean processing = false;

    public Task(GluonObject gluonObject) {
        this.id = gluonObject.getUid();
        this.url = gluonObject.getPayload();
    }

}
