package com.gluonhq.picluster.mobile.views.helper;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class FunctionValue {

    public enum FUNCTION {
        SIN("sin()"),
        COS("cos()"),
        TAN("tan()"),
        SQRT("sqrt()");

        private final String function;

        FUNCTION(String operation) {
            this.function = operation;
        }

        public String getFunction() {
            return function;
        }

        @Override
        public String toString() {
            return function.trim();
        }
    }

    private String title;
    private ObjectProperty<FUNCTION> value = new SimpleObjectProperty<>(FUNCTION.SIN);

    public FunctionValue(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public final FUNCTION getValue() {
        return value.get();
    }

    public final ObjectProperty<FUNCTION> valueProperty() {
        return value;
    }

    public final void setValue(FUNCTION value) {
        this.value.set(value);
    }
}
