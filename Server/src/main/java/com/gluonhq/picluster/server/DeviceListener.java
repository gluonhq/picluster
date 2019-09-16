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

import com.gluonhq.picluster.mobile.GluonCloudLinkService;

public class DeviceListener {

    static Logger logger = Logger.getLogger(DeviceListener.class.getName());

    private static final int PORT = 39265;
    boolean accepting = true;
    String SEP = ";";

    private final GluonCloudLinkService gluonCloudLinkService;

    public DeviceListener(GluonCloudLinkService gluonCloudLinkService) {
        this.gluonCloudLinkService = gluonCloudLinkService;
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
                    String IP = s.getInetAddress().toString().substring(1);
                    proxy.establish(IP);
                    proxy.processIncomingMessages(IP);
                } catch (IOException e) {
                    e.printStackTrace();
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

        void establish(String id) throws IOException {
            logger.fine("Establish id");
            this.id = id;
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


        void processIncomingMessages(String id) throws IOException {
            logger.fine("Got connection from " + socket);
            String request = br.readLine();
            logger.fine("Got request from worker: " + request);

            processMessage(id, request);
            System.err.println("DONE processing incoming messages");
        }


        void processMessage(String id, String status) throws IOException {
            // first part is ID
            int idx = status.indexOf(SEP);
            int idx2 = status.indexOf(SEP, idx + 1);
            String cmd = status.substring(idx + 1, idx2);
            logger.info("Message for " + id + ", cmd = " + cmd + " and proxy id = " + this.id);
            // CHECK if this ID matches the proxy ID and fail big time if not

            if (cmd.equals("ask")) {
                logger.info("Wait for available task: "+id);
                Task task = TaskQueue.getAvailableTask(true);
                logger.info("Got available task: "+id);
                String answer = task.id + SEP + task.url + "\n";
                bw.write(answer);
                bw.flush();
                os.flush();
                int ack = br.read();
                System.err.println("got ack: "+ack);
                if (ack == -1) {
                    TaskQueue.pushBack(task);
                    System.err.println("Pushback, return");
                    return;
                }
                logger.info("Wrote id + url to worker: " + answer);
            }
            if (cmd.equals("answer")) {
                int idx3 = status.indexOf(SEP, idx2 + 1);
                String taskId = status.substring(idx2 + 1, idx3);
                String result = status.substring(idx3 + 1);
                logger.fine("got answer, status = "+status+" and idx2 = "+idx2+" and idx3 = "+idx3);
                Task task = TaskQueue.getTaskById(taskId);
                logger.info("Task with id " + taskId + " is done and has answer " + result);
                bw.write("thanks\n");
                bw.flush();
                task.answer = result;
                gluonCloudLinkService.removeObjectFromList(ExternalRequestHandler.GLUONLIST_BLOCKS, task.id);
            }
        }
    }


}
