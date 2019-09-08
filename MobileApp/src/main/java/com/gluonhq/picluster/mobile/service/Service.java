package com.gluonhq.picluster.mobile.service;

import com.gluonhq.cloudlink.client.data.DataClient;
import com.gluonhq.cloudlink.client.data.DataClientBuilder;
import com.gluonhq.cloudlink.client.data.OperationMode;
import com.gluonhq.cloudlink.client.usage.UsageClient;
import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.provider.DataProvider;
import com.gluonhq.picluster.mobile.model.Model;

import javax.annotation.PostConstruct;

public class Service {

    private static final String BLOCKS = "blocks-v1";

    private GluonObservableList<Model> blocks;

    private DataClient dataClient;
    private final UsageClient usageClient;

    public Service() {
        usageClient = new UsageClient();
        usageClient.enable();
        dataClient = DataClientBuilder.create()
                .operationMode(OperationMode.CLOUD_FIRST)
                .build();

        blocks = DataProvider.retrieveList(dataClient.createListDataReader(BLOCKS, Model.class));
    }

    public void addBlock(Model block) {
        blocks.add(block);
    }

    @PostConstruct
    public void postConstruct() {
        dataClient = DataClientBuilder.create()
                .operationMode(OperationMode.CLOUD_FIRST)
                .build();
    }

}
