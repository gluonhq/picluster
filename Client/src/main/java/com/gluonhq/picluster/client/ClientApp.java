package com.gluonhq.picluster.client;

import java.util.Random;

public class ClientApp {
    private final static boolean TEST_MODE = "test".equalsIgnoreCase(System.getenv("picluster_mode"));
    static final String SERVER_IP = TEST_MODE ? "127.0.0.1" : "192.168.68.107";

    static volatile boolean run = true;

    public void startConnection(String ip, int port) {
        while(run) {
            try {
                ProcessBuilder pb = new ProcessBuilder("curl", "http://" + ip + ":" + port + "/ID?text");
                Process p = pb.start();
                Thread.sleep(1000 + new Random().nextInt(100) * 10);
            } catch (Exception e) {
                run = false;
                e.printStackTrace();
            }
        }
    }

    public void stopConnection() {
        run = false;
    }

    public static void main(String[] args) {
        ClientApp client = new ClientApp();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.stopConnection();
            try {
                Thread.currentThread().join(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
        String ip = args.length == 1 ? args[0] : SERVER_IP;
        client.startConnection(ip, 8080);
    }
}
