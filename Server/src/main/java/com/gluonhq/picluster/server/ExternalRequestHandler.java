package com.gluonhq.picluster.server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ExternalRequestHandler {

    // We listen for incoming HTTP request in the form of
    // http://localhost:8080/foo?http://my.path.to/an/image.jpg
    static final int PORT = 8080;

    static final int POOLSIZE = 32;

    Logger logger = Logger.getLogger("ExternalRequest");

    public ExternalRequestHandler() {
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
            String query = uri.getQuery();
            logger.info("Got an external request with uri "+uri+", hence query = "+query);
            Task task = new Task();
            task.url = query;
            TaskQueue.add(task);
            try {
                task.latch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("FAILED to get response in 30 seconds");
            }
            String response = "We're done, answer = ";
            if (task.latch.getCount() == 0) {
                logger.info("Got answer: "+task.answer+"\n");
                response = response + task.answer;
            } else {
                System.err.println("Got no answer");
                response = response + "TIMEOUT\n";
            }
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
