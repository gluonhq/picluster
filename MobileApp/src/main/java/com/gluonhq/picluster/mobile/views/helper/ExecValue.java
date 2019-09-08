package com.gluonhq.picluster.mobile.views.helper;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ExecValue {

    private String title;
    private ObjectProperty<Runnable> value = new SimpleObjectProperty<>();

    public ExecValue(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public final Runnable getValue() {
        return value.get();
    }

    public final ObjectProperty<Runnable> valueProperty() {
        return value;
    }

    public final void setValue(Runnable value) {
        this.value.set(value);
    }
}
