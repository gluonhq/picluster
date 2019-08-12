package com.gluonhq.picluster.server;

public class Main {

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
        ExternalRequestHandler ext = new ExternalRequestHandler();
        ext.startListening();
    }


}
