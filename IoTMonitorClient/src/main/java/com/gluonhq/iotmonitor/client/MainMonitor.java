package com.gluonhq.iotmonitor.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Run:
 *
 * java -XX:-UsePerfData MainMonitor.java
 */
public class MainMonitor {

    private final static boolean TEST_MODE = false;

    private final static String HOST = TEST_MODE ? "localhost" : "192.168.68.112";
    private final static int PORT = 31415;
    private final static String SEP = ";";
    private final static ProcessBuilder pbCpu;
    private final static ProcessBuilder pbMem;

    static {
        pbCpu = new ProcessBuilder( "/bin/sh", "-c");
        pbCpu.command().add("vmstat | awk '(NR==2){for(i=1;i<=NF;i++)if($i==\"id\"){getline; print $i}}'");
        pbCpu.redirectErrorStream(true);
        pbMem = new ProcessBuilder( "/bin/sh", "-c");
        pbMem.command().add("vmstat -s | awk  ' $0 ~ /total memory/ {total=$1 } $0 ~/free memory/ {free=$1} $0 ~/buffer memory/ {buffer=$1} $0 ~/cache/ {cache=$1} END{print (total-free-buffer-cache)/total*100}'");
        pbMem.redirectErrorStream(true);
    }

    public static void main(String[] args) {
        try {
            if (args.length == 1) {
                talk(args[0]);
            } else {
                talk(HOST);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void talk (String host) throws IOException, InterruptedException {
        Socket s = new  Socket(host, PORT);

        OutputStream os = s.getOutputStream();
        InputStream is = s.getInputStream();
        Thread outThread = new Thread(() -> {
            boolean go = true;
            BufferedWriter br = new BufferedWriter(new OutputStreamWriter(os));
            // handshake
            try {
                br.write("ID\n");
                br.flush();
            } catch (IOException e) {
                e.printStackTrace();
                go = false;
            }
            while (go) {
                String msg = "ID" + SEP + "cpu" + SEP + getCpuUsage() + SEP + getMemUsage() + "\n";
                try {
                    br.write(msg);
                    br.flush();
                    os.flush();
                    Thread.sleep(TEST_MODE ? 1_000 : 10_000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    go = false;
                }
            }
        });
        outThread.start();
        Thread inThread = new Thread(() -> {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            boolean go = true;
            while (go) {
                try {
                    String cmd = br.readLine();
                    if (cmd == null) {
                        System.err.println("Got disconnected from server!");
                        go = false;
                    } else {
                        if (cmd.equals("REBOOT")) {
                            go = false;
                            System.err.println("I have to reboot!!");
                            ProcessBuilder pb = new ProcessBuilder("sudo", "reboot");
                            pb.redirectErrorStream(true);
                            pb.start();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    go = false;
                }
            }
        });
        inThread.start();

    }

    private static double getCpuUsage() {
        String answer;
        try {
            Process p = pbCpu.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                answer = reader.readLine();
            }
            if (p.waitFor() == 0) {
                return 100 - Double.valueOf(answer);
            }
            System.out.println("Error, answer: " + answer);
        } catch (IOException | InterruptedException | NumberFormatException ex) {
            System.err.println("Error processing " + ex.getMessage());
        }
        return 0d;
    }

    private static double getMemUsage() {
        String answer;
        try {
            Process p = pbMem.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                answer = reader.readLine();
            }
            if (p.waitFor() == 0) {
                return Double.valueOf(answer);
            }
            System.out.println("Error, answer: " + answer);
        } catch (IOException | InterruptedException | NumberFormatException ex) {
            System.err.println("Error processing " + ex.getMessage());
        }
        return 0d;
    }

}
