package com.modular.booking.utils;

/**
 * Created by Arison on 2018/2/1.
 */

public class EventMessage {
    Object object;

    public EventMessage(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
