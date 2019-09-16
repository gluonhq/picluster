package com.gluonhq.picluster.server;

import com.gluonhq.picluster.mobile.GluonCloudLinkService;
import com.gluonhq.picluster.mobile.model.GluonObject;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ExternalRequestHandler {

    private static final Logger logger = Logger.getLogger(ExternalRequestHandler.class.getName());

    public static final String GLUONLIST_BLOCKS = "blocks-v1";
    private static final long PROCESS_BLOCKS_INTERVAL = 10;

    private static final int TIMEOUT_SECONDS = 10;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AutonomousDatabaseWriter autonomousDatabaseWriter;
    private final GluonCloudLinkService gluonCloudLinkService;

    public ExternalRequestHandler(AutonomousDatabaseWriter autonomousDatabaseWriter, GluonCloudLinkService gluonCloudLinkService) {
        this.autonomousDatabaseWriter = autonomousDatabaseWriter;
        this.gluonCloudLinkService = gluonCloudLinkService;
    }

    public void startListening() {
        this.scheduler.scheduleAtFixedRate(() -> processBlocks(), 0, PROCESS_BLOCKS_INTERVAL, SECONDS);
    }

    private void processBlocks() {
        List<GluonObject> gluonObjectBlocks = gluonCloudLinkService.getList(GLUONLIST_BLOCKS);
        logger.info("Processing blocks, got: " + gluonObjectBlocks.size());
        gluonObjectBlocks.stream()
                .map(Task::new)
                .filter(task -> !TaskQueue.taskAlreadyAdded(task))
                .forEach(TaskQueue::add);
    }
}
