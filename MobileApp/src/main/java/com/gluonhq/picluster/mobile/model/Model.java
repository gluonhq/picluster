package com.gluonhq.picluster.mobile.model;

import java.util.ArrayList;
import java.util.List;

public class Model {

    private String expression;

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

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "Model{" +
                "blocks=" + blocks +
                "expression=" + expression +
                '}';
    }
}
