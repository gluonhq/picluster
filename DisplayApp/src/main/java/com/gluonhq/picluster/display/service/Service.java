package com.gluonhq.picluster.display.service;

import com.gluonhq.picluster.display.model.Chunk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;

public class Service {

    private static final int PORT = 6666;

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean run;

    public void start(Consumer<Chunk> consumer) {
        Thread thread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                if (serverSocket.isClosed()) {
                    return;
                }
                run = true;
                while (run) {
                    clientSocket = serverSocket.accept();
                    Thread clientThread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                out = new PrintWriter(clientSocket.getOutputStream(), true);
                                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                                String message;
                                while ((message = in.readLine()) != null) {
                                    out.println("Server: " + message);
//                        System.out.println("chunk = " + message);
                                    Chunk chunk = Chunk.parseChunk(message);
                                    if (chunk != null) {
                                        consumer.accept(chunk);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    clientThread.start();
                }
            } catch (SocketException s) {
                System.out.println("Error: " + s);
                run = false;
            } catch (IOException e) {
                e.printStackTrace();
                run = false;
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        run = false;
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
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
