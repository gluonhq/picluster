package com.gluonhq.picluster.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class DeviceListener {

    static Logger logger = Logger.getLogger("DeviceListener");

    private static final int PORT = 39265;
    boolean accepting = true;
    String SEP = ";";

    public DeviceListener() {
    }

    public void startListening()  {
        Thread serverThread = new Thread() {
            @Override
            public void run() {
                ServerSocket serverSocket = null;
                try {
                    serverSocket = new ServerSocket(PORT);
                    while (accepting) {
                        logger.fine("Server is listening for devices that want some work or have work");
                        Socket socket = serverSocket.accept();
                        logger.info("Server got a connection from a device that wants to work or has done work");
                        processSocket(socket);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        serverThread.start();
    }

    private void processSocket(final Socket s) {
        Thread t = new Thread() {
            @Override
            public void run() {
                NodeProxy proxy = new NodeProxy(s);
                try {
                    proxy.establish();
                    proxy.processIncomingMessages();
                } catch (IOException e) {
                    try {
                        s.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }

    public class NodeProxy {
        private String id;

        private Socket socket;
        InputStream is;
        OutputStream os;
        BufferedReader br;
        BufferedWriter bw;

        NodeProxy(Socket s) {
            this.socket = s;
        }

        void establish() throws IOException {
            logger.fine("Establish socket, get is");
            this.is = socket.getInputStream();
            logger.finer("got is, now get os");
            this.os = socket.getOutputStream();
            logger.finer("got os, now get IP");
            String ip = socket.getInetAddress().toString();
            logger.finer("Got ip: " + ip);
            br = new BufferedReader(new InputStreamReader(this.is));
            logger.finer("got br = " + br);
            bw = new BufferedWriter(new OutputStreamWriter(this.os));
            logger.finer("got bw");
        }


        void processIncomingMessages() throws IOException {
            logger.fine("Got connection from " + socket);
            String request = br.readLine();
            logger.fine("Got request from worker: " + request);

            processMessage(request);

        }


        void processMessage(String status) throws IOException {
            // first part is ID
            int idx = status.indexOf(SEP);
            String id = status.substring(0, idx);
            int idx2 = status.indexOf(SEP, idx + 1);
            String cmd = status.substring(idx + 1, idx2);
            logger.info("Message for " + id + ", cmd = " + cmd + " and proxy id = " + this.id);
            // CHECK if this ID matches the proxy ID and fail big time if not

            if (cmd.equals("ask")) {
                logger.fine("Wait for available task");
                Task task = TaskQueue.getAvailableTask(true);
                logger.fine("Got available task");
                String answer = task.id + SEP + task.url + "\n";
                bw.write(answer);
                bw.flush();
                logger.finer("Wrote id + url to worker: " + answer);
            }
            if (cmd.equals("answer")) {
                int idx3 = status.indexOf(SEP, idx2 + 1);
                String taskId = status.substring(idx2 + 1, idx3);
                String result = status.substring(idx3 + 1);
                logger.fine("got answer, statyus = "+status+" and idx2 = "+idx2+" and idx3 = "+idx3);
                Task task = TaskQueue.getTaskById(taskId);
                logger.info("Task with id " + taskId + " is done and has answer " + result);
                task.answer = result;
                task.latch.countDown();
            }
        }
    }


}
