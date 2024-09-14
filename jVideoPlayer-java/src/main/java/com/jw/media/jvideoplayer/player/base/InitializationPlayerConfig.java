package com.jw.media.jvideoplayer.player.base;

import java.util.Map;

/**
 * Created by Joyce.wang on 2024/7/1 15:30
 *
 * @Description Initialization configurations for the player.
 */
public class InitializationPlayerConfig {
    private String mUrl;

    private Map<String, String> mHeaders;

    private float speed = 1;

    private boolean looping;

    private boolean isAudioOnly;

    public InitializationPlayerConfig(String url,
                                      Map<String, String> headers,
                                      boolean loop,
                                      float speed,
                                      boolean isAudioOnly) {
        this.mUrl = url;
        this.mHeaders = headers;
        this.looping = loop;
        this.speed = speed;
        this.isAudioOnly = isAudioOnly;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    public void setHeaders(Map<String, String> headers) {
        this.mHeaders = headers;
    }

    public boolean isLooping() {
        return looping;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isAudioOnly() {
        return isAudioOnly;
    }

    public void setAudioOnly(boolean audioOnly) {
        isAudioOnly = audioOnly;
    }
}
