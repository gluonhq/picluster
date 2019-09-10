package com.gluonhq.picluster.server;

import com.gluonhq.picluster.mobile.model.Wrapper;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ExternalRequestHandler {

    private static final String BLOCKS = "blocks-v1";
    private static final long DELAY =  10_000;

    private static final int TIMEOUT_SECONDS = 10;

    private Logger logger = Logger.getLogger("ExternalRequest");

    private final AutonomousDatabaseWriter autonomousDatabaseWriter;
    private final String GLUON_SERVER_KEY;

    public ExternalRequestHandler(AutonomousDatabaseWriter autonomousDatabaseWriter, String gluonServerKey) {
        this.autonomousDatabaseWriter = autonomousDatabaseWriter;

        GLUON_SERVER_KEY = gluonServerKey;
    }

    public void startListening() throws IOException {
        while (true) {
            List<Wrapper> wrappers = listRequest();
            wrappers.forEach(wrapper -> {
                String uid = wrapper.getUid();

                Task task = new Task();
                task.url = wrapper.getPayload();
                TaskQueue.add(task);

                String finalAnswer = "TIMEOUT";
                try {
                    if (task.latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                        logger.info("Got answer: "+task.answer+"\n");
                        removeRequest(uid);
                        finalAnswer = task.answer;
                    } else {
                        logger.warning("Got no answer");
                    }
                } catch (InterruptedException e) {
                    logger.severe("FAILED to get response in " + TIMEOUT_SECONDS + " seconds");
                    finalAnswer = "INTERRUPT";
                }

                if (autonomousDatabaseWriter != null) {
                    autonomousDatabaseWriter.logClientRequestAndAnswer(task.id, task.url, finalAnswer);
                }

                String response = "We're done, answer = " + finalAnswer + "\n";
                logger.info(response);
            });

            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {}
        }
    }

    private List<Wrapper> listRequest() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://cloud.gluonhq.com/3/data/enterprise/list/" + BLOCKS))
                .header("Authorization", "Gluon " + GLUON_SERVER_KEY)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            Jsonb jsonb = JsonbBuilder.create();
            return Arrays.asList(jsonb.fromJson(response.body(), Wrapper[].class));
        } catch (IOException | InterruptedException e) {
            logger.severe("Error processing list request");
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private void removeRequest(String uid) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://cloud.gluonhq.com/3/data/enterprise/list/" + BLOCKS + "/remove/" + uid))
                .header("Authorization", "Gluon " + GLUON_SERVER_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        try {
            HttpResponse<?> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());
            logger.info("uid: " + uid + " removed with response: " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            logger.severe("Error processing remove request");
            e.printStackTrace();
        }
    }

}
