package com.gluonhq.picluster.server;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /*
     * This is the main entrypoint for the server on Ubuntu.
     * It deals with
     * 1. requests from external clients (currently sending a URL containing an image)
     *    Those are handled by the ExternalRequestHandler
     * 2. requests from worker devices. Those are handled by the DeviceListener
     *
     * Both components have their own asynchronous communication with their peers, and they
     * communicate via the static TaskQueue. ExternalRequestsHandler will put requests
     * on the taskQueue, and wait until they are answered.
     * DeviceListener will listen for devices that are waiting for work, and hand them a task.
     * When a device is ready, it will again contact the DeviceListener and provide the answer.
     * The DeviceListener will then mark the task as answered, and the ExternalRequestHandler
     * will answer the original requester.
     * TODO: the answer needs to go to not only the original requester, but also to the videoclient
     */
    public static void main(String[] args) throws Exception {
        System.err.println("Starting main server");
        DeviceListener dl = new DeviceListener();
        dl.startListening();

        AutonomousDatabaseWriter autonomousDatabaseWriter = null;
        if (args.length >= 2) {
            autonomousDatabaseWriter = new AutonomousDatabaseWriter(args[0], args[1]);
            try {
                autonomousDatabaseWriter.setupConnectionPool();
            } catch (SQLException sql) {
                // too bad if the connection pool to the oracle Autonomous Database could not be setup
                logger.log(Level.SEVERE, "Failed to setup connection pool to oracle autonomous database.", sql);
            }
        }

        ExternalRequestHandler ext = new ExternalRequestHandler(autonomousDatabaseWriter);
        ext.startListening();
    }


}
