package com.gluonhq.picluster.mobile.model;

import java.util.ArrayList;
import java.util.List;

public class Model {

    private List<Block> blocks;

    public Model() {
        blocks = new ArrayList<>();
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    @Override
    public String toString() {
        return "Model{" +
                "blocks=" + blocks +
                '}';
    }
}
