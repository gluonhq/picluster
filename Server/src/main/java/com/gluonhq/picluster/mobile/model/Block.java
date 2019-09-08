package com.gluonhq.picluster.mobile.model;

import java.util.ArrayList;
import java.util.List;

public class Block {
    private String id;
    private String type;
    private String value;
    private List<String> inputs;
    private List<String> outputs;

    public Block() {
        this(null, null);
    }

    public Block(String id, String type) {
        this.id = id;
        this.type = type;
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<String> getInputs() {
        return inputs;
    }

    public void setInputs(List<String> inputs) {
        this.inputs = inputs;
    }

    public List<String> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<String> outputs) {
        this.outputs = outputs;
    }

    @Override
    public String toString() {
        return "Block{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", inputs=" + inputs +
                ", outputs=" + outputs +
                '}';
    }
}
