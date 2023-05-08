package com.autolink.app.downloadmanager;


public abstract class NamedRunnable implements Runnable {

    private String name;

    public NamedRunnable(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName(name);
        try {
            execute();
        } finally {
            Thread.currentThread().setName(oldName);
        }
    }

    abstract void execute();
}
