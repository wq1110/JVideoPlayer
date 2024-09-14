package com.jw.media.jvideoplayer.player.listener;

/**
 * Created by Joyce.wang on 2024/3/19 16:41
 *
 * @Description TODO
 */
public interface OnConnectionChangeListener {
    public void onLocaleChange();

    public void onConnectionChange(ConnectionType type);

    public enum ConnectionType {
        WIFI, MOBILE, ETHERNET, NONE, UNKNOWN
    }
}
