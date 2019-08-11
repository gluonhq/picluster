package com.gluonhq.picluster.server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import java.util.concurrent.Executors

public class Main {

    static final int PORT = 8080;

    static final int POOLSIZE = 32;

    public static void main(String[] args) throws Exception {
        System.err.println("Starting main server");
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT),0);
        HttpContext context = server.createContext("/");
        Handler handler = new Handler();
        context.setHandler(handler);
        server.setExecutor(Executors.newFixedThreadPool(POOLSIZE));
        server.start();
    }

    static class Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.err.println("HANDLE!");
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();
            System.err.println("URI = "+ uri);
            System.err.println("Query = "+ query);
try {
Thread.sleep(5000);
} catch (Exception e) {
e.printStackTrace();
}
            String response = "We're done";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}
