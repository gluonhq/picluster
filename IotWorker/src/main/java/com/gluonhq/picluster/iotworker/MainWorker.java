package com.gluonhq.picluster.iotworker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Random;

public class MainWorker {

    static final String ID = "ID"; // won't use
    static final String SEP = ";";

    static final String SERVER_IP = "192.168.68.107";
    // static final String SERVER_IP = "127.0.0.1";
    static final int SERVER_PORT = 39265;

    static final String DISPLAY_IP = "192.168.68.112";
    // static final String DISPLAY_IP = "127.0.0.1";
    static final int DISPLAY_PORT = 6666;

    static boolean go = true;

    public static void main(String[] args) {
        System.err.println("Hi, I'm an IoT worker");
        try {
            if (args.length == 2) {
                runJobs(args[0], args[1]);
            } else {
                runJobs(SERVER_IP, DISPLAY_IP);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println("SHOULD NOT REACH HERE, best to reboot!");
    }

    static void runJobs(String serverIP, String displayIP) throws IOException {
        while (go) {
            System.err.println("Create socket...");
            String msg = ID+SEP+"ask"+SEP+"\n";
            String job = sendSingleMessage(serverIP, msg);


            int idx = job.indexOf(SEP);
            String taskId = job.substring(0, idx);
            String url = job.substring(idx+1);
            System.err.println("Need to process "+url);
            int answer = processURL(taskId, url);

            msg = ID+SEP+"answer"+SEP+taskId+SEP+answer+"\n";
            sendResultToDisplayApp(displayIP, answer);
            sendSingleMessage(serverIP, msg);

        }
    }

    static int processURL (String taskId, String url) {
        System.err.println("Processing image at "+url+" with taskId "+taskId);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int answer = (int)(Math.random() * 25);
        System.err.println("Processing image at "+url+" with taskId "+taskId+", result = "+answer);
        return answer;
    }

    static String sendSingleMessage (String serverIP, String msg) throws IOException {
        Socket s = new Socket(serverIP, SERVER_PORT);
        System.err.println("Need to send "+msg+", Socket created");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

        System.err.println("Write message: "+msg);
        bw.write(msg);
        bw.flush();
        System.err.println("Message written");
        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        System.err.println("Waiting for answer on "+msg);
        String job = br.readLine();
        // send ACK so server knows we'll do this job
        bw.write(1);
        bw.flush();
        br.close();
        bw.close();
        s.close();
        System.err.println("SingleMessage will return "+job);
        return job;
    }

    private static final int PORT = 6666;
    private static final int SIZE_X = 32;
    private static final int SIZE_Y = 32;
    private static final int SIZE = 1024;


    static void sendResultToDisplayApp(String displayIP, int answer) {
        try {
            Socket clientSocket = new Socket(displayIP, DISPLAY_PORT);
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            int chunkId_X = new Random().nextInt(SIZE_X);
            int chunkId_Y = new Random().nextInt(SIZE_Y);
            double opacity = (double) answer / 100d;
            String msg = String.format("%d,%d,%s", chunkId_X, chunkId_Y,
                    String.format("%.2f", opacity).replace(",", "."));
            out.println(msg);
            String resp = null;
            try {
                System.err.println("Waiting for answer from displayApp");
                resp = in.readLine();
                System.err.println("Got answer from displayApp: "+resp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (ConnectException c) {
            System.out.println("Error connection: " + c);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
