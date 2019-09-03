package com.gluonhq.picluster.mobile.views.helper;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class CompareValue {

    public enum TEST {
        EQUAL_TO("  ==  "),
        NOT_EQUAL_TO("  !=  "),
        GREATER_THAN("  >  "),
        GREATER_THAN_OR_EQUAL_TO("  >=  "),
        LESS_THAN("  <  "),
        LESS_THAN_OR_EQUAL_TO("  <=  ");

        private final String test;

        TEST(String test) {
            this.test = test;
        }

        public String getTest() {
            return test;
        }

        @Override
        public String toString() {
            return test.trim();
        }
    }

    private String title;
    private ObjectProperty<TEST> value = new SimpleObjectProperty<>(TEST.EQUAL_TO);

    public CompareValue(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public final TEST getValue() {
        return value.get();
    }

    public final ObjectProperty<TEST> valueProperty() {
        return value;
    }

    public final void setValue(TEST value) {
        this.value.set(value);
    }
}
