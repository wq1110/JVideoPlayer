package com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity;

import java.io.Serializable;

public class SignSyntax implements Serializable {
    private String target;

    private String replacement;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }
}
