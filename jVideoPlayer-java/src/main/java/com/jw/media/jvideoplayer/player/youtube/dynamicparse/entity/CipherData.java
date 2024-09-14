package com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity;

import java.io.Serializable;

public class CipherData implements Serializable {
    private String regex;

    private SignSyntax signSyntax;

    private UrlSyntax urlSyntax;

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public SignSyntax getSignSyntax() {
        return signSyntax;
    }

    public void setSignSyntax(SignSyntax signSyntax) {
        this.signSyntax = signSyntax;
    }

    public UrlSyntax getUrlSyntax() {
        return urlSyntax;
    }

    public void setUrlSyntax(UrlSyntax urlSyntax) {
        this.urlSyntax = urlSyntax;
    }
}
