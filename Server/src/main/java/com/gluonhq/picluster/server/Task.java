package com.gluonhq.picluster.server;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class Task {

    final String id;
    String uid;
    String url;
    String answer;
    CountDownLatch latch = new CountDownLatch(1);
    boolean processing = false;

    public Task() {
        this.id = UUID.randomUUID().toString();
    }

}
