package com.autolink.app.downloadmanager;

public interface Callback {

    default void onStart(Task task){}

    void onSuccess(Task task);

    default void onProgressChanged(Task task) {
    }

    void onFailure(Task task, Exception e);

}
