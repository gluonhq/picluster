package com.gluonhq.picluster.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Random;

public class ClientApp {
    private static final int PORT = 6666;
    private static final int SIZE_X = 32;
    private static final int SIZE_Y = 32;
    private static final int SIZE = 1024;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    static volatile boolean run;


    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            run = true;
        } catch (ConnectException c) {
            System.out.println("Error connection: " + c);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateChunks() {
        while (run) {
            int chunkId_X = new Random().nextInt(SIZE_X);
            int chunkId_Y = new Random().nextInt(SIZE_Y);
            double opacity = 0.5 + (double) new Random().nextInt(SIZE) / (2d * SIZE);
            sendMessage(String.format("%d,%d,%s", chunkId_X, chunkId_Y,
                    String.format("%.2f", opacity).replace(",", ".")));
            try {
                Thread.sleep(10 + new Random().nextInt(20));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String sendMessage(String msg) {
        if (out == null) {
            return "No response";
        }
        out.println(msg);
        String resp = null;
        try {
            resp = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resp;
    }

    public void stopConnection() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ClientApp client = new ClientApp();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            run = false;
            client.stopConnection();
            try {
                Thread.currentThread().join(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
        client.startConnection("127.0.0.1", PORT);
        client.updateChunks();
    }
}
