package com.gluonhq.picluster.server;

import com.gluonhq.connect.provider.ListDataReader;
import com.gluonhq.connect.provider.RestClient;
import com.gluonhq.picluster.mobile.model.Model;
import com.gluonhq.picluster.mobile.model.Wrapper;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ExternalRequestHandler {

    private static final String BLOCKS = "blocks-v1";
    private static final long DELAY =  10_000;

    static final int TIMEOUT_SECONDS = 10;

    Logger logger = Logger.getLogger("ExternalRequest");

    private final AutonomousDatabaseWriter autonomousDatabaseWriter;
    private final String GLUON_SERVER_KEY;

    public ExternalRequestHandler(AutonomousDatabaseWriter autonomousDatabaseWriter, String gluonServerKey) {
        this.autonomousDatabaseWriter = autonomousDatabaseWriter;

        GLUON_SERVER_KEY = gluonServerKey;
    }

    public void startListening() throws IOException {
        RestClient restList = RestClient.create()
                .method("GET")
                .host("https://cloud.gluonhq.com")
                .header("Authorization", "Gluon " + GLUON_SERVER_KEY)
                .path("/3/data/enterprise/list/" + BLOCKS);

        Jsonb jsonb = JsonbBuilder.create();
        while (true) {
            ListDataReader<Wrapper> listDataReader = restList.createListDataReader(Wrapper.class);
            Iterator<Wrapper> it = listDataReader.iterator();
            while (it.hasNext()) {
                Wrapper wrapper = it.next();
                String uid = wrapper.getUid();
                Model model = jsonb.fromJson(wrapper.getPayload(), Model.class);

                // TODO
                // removeRequest(uid);

                Task task = new Task();
                task.url = wrapper.getPayload();
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
                logger.info(response);
            }

            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {}
        }
    }

    private void removeRequest(String uid) {
        RestClient restRemove = RestClient.create()
                .method("POST")
                .host("https://cloud.gluonhq.com")
                .header("Authorization", "Gluon " + GLUON_SERVER_KEY)
                .path("/3/data/enterprise/list/" + BLOCKS +"/remove/" + uid);
        // TODO
    }

}
