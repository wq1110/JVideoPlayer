package com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity;

import java.io.Serializable;
import java.util.List;

public class UrlSyntax implements Serializable {
    private String prefix;

    private List<MethodValue> methodInvokes;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<MethodValue> getMethodInvokes() {
        return methodInvokes;
    }

    public void setMethodInvokes(List<MethodValue> methodInvokes) {
        this.methodInvokes = methodInvokes;
    }
}
