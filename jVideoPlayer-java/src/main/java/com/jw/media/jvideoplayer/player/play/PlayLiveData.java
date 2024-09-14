package com.jw.media.jvideoplayer.player.play;

import androidx.lifecycle.MutableLiveData;

/**
 * Created by Joyce.wang on 2024/9/13 11:20
 *
 * @Description 当前播放数据中心
 */
public class PlayLiveData extends MutableLiveData<Integer> {
    private String url;
    private boolean isLandscape;
    private boolean isTrailer;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isLandscape() {
        return isLandscape;
    }

    public void setLandscape(boolean landscape) {
        isLandscape = landscape;
    }

    public boolean isTrailer() {
        return isTrailer;
    }

    public void setTrailer(boolean trailer) {
        this.isTrailer = trailer;
    }
}
