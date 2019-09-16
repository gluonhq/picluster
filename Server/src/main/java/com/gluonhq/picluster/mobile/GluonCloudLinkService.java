package com.gluonhq.picluster.mobile;

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
import java.util.logging.Logger;

import com.gluonhq.picluster.mobile.model.GluonObject;

public class GluonCloudLinkService {

    private static final Logger logger = Logger.getLogger(GluonCloudLinkService.class.getName());

    private String cloudlinkServerKey;

    public GluonCloudLinkService(String cloudlinkServerKey) {
        this.cloudlinkServerKey = cloudlinkServerKey;
    }

    public List<GluonObject> getList(String listIdentifier) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://cloud.gluonhq.com/3/data/enterprise/list/" + listIdentifier))
                .header("Authorization", "Gluon " + cloudlinkServerKey)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            logger.finer("Got response from Gluon CloudLink with status code " + response.statusCode() + " and body: " + response.body());
            Jsonb jsonb = JsonbBuilder.create();
            return Arrays.asList(jsonb.fromJson(response.body(), GluonObject[].class));
        } catch (IOException | InterruptedException e) {
            logger.severe("Error processing list request");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public void removeObjectFromList(String listIdentifier, String objectIdentifier) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://cloud.gluonhq.com/3/data/enterprise/list/" + listIdentifier + "/remove/" + objectIdentifier))
                .header("Authorization", "Gluon " + cloudlinkServerKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        try {
            HttpResponse<?> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());
            logger.finer("Got response from Gluon CloudLink with status code " + response.statusCode() + " and body: " + response.body());
        } catch (IOException | InterruptedException e) {
            logger.severe("Error processing remove request");
            e.printStackTrace();
        }
    }
}