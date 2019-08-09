package com.modular.booking.base;


import com.modular.booking.utils.ViewEventListener;

import java.util.List;

public interface IAdapter<T> {

    void setItems(List<T> items);

    void addItem(T item);

    void delItem(T item);

    void addItems(List<T> items);

    void clearItems();

    T getItem(int position);

    ViewEventListener<T> getViewEventListener();

    void setViewEventListener(ViewEventListener<T> viewEventListener);
}