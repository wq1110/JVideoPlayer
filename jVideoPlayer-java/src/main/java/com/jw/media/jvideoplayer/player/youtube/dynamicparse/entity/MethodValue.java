package com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity;

import java.io.Serializable;

public class MethodValue implements Serializable {
    private String methodName;

    private int value;

    public MethodValue() {
    }

    public MethodValue(String methodName, int value) {
        this.methodName = methodName;
        this.value = value;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
