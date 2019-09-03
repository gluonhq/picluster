package com.gluonhq.picluster.mobile.service;

import com.gluonhq.cloudlink.client.data.DataClient;
import com.gluonhq.cloudlink.client.data.DataClientBuilder;
import com.gluonhq.cloudlink.client.data.OperationMode;
import com.gluonhq.cloudlink.client.data.RemoteFunctionBuilder;
import com.gluonhq.cloudlink.client.data.RemoteFunctionObject;
import com.gluonhq.cloudlink.client.usage.UsageClient;
import com.gluonhq.connect.GluonObservableObject;
import com.gluonhq.connect.converter.JsonConverter;
import com.gluonhq.picluster.mobile.model.Model;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;

public class Service {

    private DataClient dataClient;
    private final UsageClient usageClient;

    public Service() {
        usageClient = new UsageClient();
        usageClient.enable();
    }

    @PostConstruct
    public void postConstruct() {
        dataClient = DataClientBuilder.create()
                .operationMode(OperationMode.CLOUD_FIRST)
                .build();
    }

    public GluonObservableObject<String> sendBlocks(Model model) {
        JsonConverter<Model> converter = new JsonConverter<>(Model.class);
        String jsonAnswer = converter.writeToJson(model).toString();
        RemoteFunctionObject sendBlocks = null;
        try {
            sendBlocks = RemoteFunctionBuilder.create("sendBlocks")
                    .rawBody(jsonAnswer.getBytes("UTF-8"))
                    .cachingEnabled(false)
                    .object();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sendBlocks.call(String.class);
    }
}
