package com.gluonhq.picluster.client;

import java.util.Random;

public class ClientApp {
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
        String ip = args.length == 1 ? args[0] : "192.168.68.107";
        client.startConnection(ip, 8080);
    }
}
