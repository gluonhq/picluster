package com.gluonhq.picluster.server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ExternalRequestHandler {

    private final static boolean TEST_MODE = "test".equalsIgnoreCase(System.getenv("picluster_mode"));

    // We listen for incoming HTTP request in the form of
    // http://localhost:8080/foo?http://my.path.to/an/image.jpg
    static final int PORT = 8080;

    static final int POOLSIZE = 32;

    static final int TIMEOUT_SECONDS = 10;

    Logger logger = Logger.getLogger("ExternalRequest");

    private final AutonomousDatabaseWriter autonomousDatabaseWriter;

    public ExternalRequestHandler(AutonomousDatabaseWriter autonomousDatabaseWriter) {
        this.autonomousDatabaseWriter = autonomousDatabaseWriter;
    }

    public void startListening() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT),0);
        HttpContext context = server.createContext("/");
        Handler handler = new Handler();
        context.setHandler(handler);
        server.setExecutor(Executors.newFixedThreadPool(POOLSIZE));
        server.start();
    }

    class Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            URI uri = exchange.getRequestURI();
            String query;
            if (TEST_MODE) {
                query = uri.getQuery();
                logger.info("Got an external request with uri "+uri+", hence query = "+query);
            } else {
                logger.info("Got an external request with uri " + uri);
                final byte[] buffer = new byte[4096];
                try (final InputStream in = exchange.getRequestBody();
                     final ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.length)) {
                    int length;
                    while ((length = in.read(buffer, 0, buffer.length)) >= 0) {
                        baos.write(buffer, 0, length);
                    }
                    query = baos.toString();
                }
                logger.info("Got query = " + query);
            }
            Task task = new Task();
            task.url = query;
            TaskQueue.add(task);
            String finalAnswer = "TIMEOUT";
            try {
                if (task.latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    logger.info("Got answer: "+task.answer+"\n");
                    finalAnswer = task.answer;
                } else {
                    System.err.println("Got no answer");
                }
            } catch (InterruptedException e) {
                System.err.println("FAILED to get response in " + TIMEOUT_SECONDS + " seconds");
                finalAnswer = "INTERRUPT";
            }

            if (autonomousDatabaseWriter != null) {
                autonomousDatabaseWriter.logClientRequestAndAnswer(task.id, task.url, finalAnswer);
            }

            String response = "We're done, answer = " + finalAnswer + "\n";

            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
