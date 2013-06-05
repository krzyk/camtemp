package com.kirela.android.camtemp;

public class Camera {
    private final int id;
    private final String name;

    public Camera(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return String.format("%d, %s", id, name);
    }
}
