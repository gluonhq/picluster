package com.gluonhq.iotmonitor.monitor;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import static com.gluonhq.iotmonitor.monitor.Main.TEST_MODE;

public class DataReader {

    private static final int PORT = 31415;
    private boolean accepting;
    private String SEP = ";";

    public void startReading()  {
        accepting = true;
        Thread serverThread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(PORT);
                while (accepting) {
                    Socket socket = serverSocket.accept();
                    processSocket(socket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void processSocket(final Socket s) {
        Thread t = new Thread(() -> {
            NodeProxy proxy = new NodeProxy(s);
            try {
                String IP = s.getInetAddress().toString().substring(1);
                proxy.establish(IP);
                proxy.processIncomingMessages(IP);
            } catch (IOException e) {
                try {
                    s.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void stopReading() {
        accepting = false;
    }

    class NodeProxy {

        private String id;

        private Socket socket;
        InputStream is;
        OutputStream os;
        BufferedReader br;
        BufferedWriter bw;

        NodeProxy(Socket s) {
            this.socket = s;
        }

        void establish(String id) throws IOException {
            this.is = socket.getInputStream();
            this.os = socket.getOutputStream();
            String ip = socket.getInetAddress().toString();
            br = new BufferedReader(new InputStreamReader(this.is));
            bw = new BufferedWriter(new OutputStreamWriter(this.os));
            this.id = id;
            br.readLine();

            System.err.println(("id = " + id));
            Platform.runLater(() -> {
                Node node = Model.getNodeById(id);
                node.setProxy(this);
                node.lastKnownIp().set(ip);
                node.lastPing().set(System.nanoTime());
            });
        }

        void processIncomingMessages(String id) throws IOException {
            System.err.println("Got connection from " + socket);
            boolean go = true;
            while (go) {
                String status = this.readStatus();
//                System.err.println("DID READ " + status);
                if (status == null) {
                    System.err.println("Lost connection to " + socket);
                    go = false;
                } else {
                    processMessage (id, status);
                }
            }
        }

        void processMessage(String id, String status) {
            // first part is ID
            String[] split = status.split(SEP);
            if (split.length != 4) {
                return;
            }
            String cmd = split[1];
//            System.err.println("Message for " + id + ", cmd = " + cmd);
            // CHECK if this ID matches the proxy ID and fail big time if not

            if (cmd.equals("cpu")) {
                double v1 = Double.valueOf(split[2]);
                double v2 = Double.valueOf(split[3]);
//                System.err.println("Values = " + v1 + " " + v2);
                Platform.runLater(() -> {
                    Node node = Model.getNodeById(id);
                    if (TEST_MODE) {
                        node.getStat().cpu.set(new Random().nextDouble() * 100);
                        node.getStat().mem.set(new Random().nextDouble() * 100);
                    } else {
                        node.getStat().cpu.set(v1);
                        node.getStat().mem.set(v2);
                    }
                    node.lastPing().set(System.nanoTime());
                });
            }
        }

        // reads a status message, blocks until it gets one.
        String readStatus() throws IOException {
            String s = br.readLine();
            return s;
        }

        boolean requestReboot() {
            try {
                bw.write("REBOOT\n");
                bw.flush();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
