package com.jw.media.jvideoplayer.player.model;

/**
 * Created by Joyce.wang on 2024/9/13 13:24
 *
 * @Description 播放功能对外传参类
 */
public class PlayParam {
    public String url;
    public boolean isLandscape;
    private PlayParam() {
    }

    public static PlayTrailerParam toPlayTrailer(String url) {
        return new PlayTrailerParam(url, false);
    }

    public static PlayTrailerParam toPlayTrailer(String url, boolean isLandscape) {
        return new PlayTrailerParam(url, isLandscape);
    }

    public static class PlayTrailerParam extends PlayParamBase<Void> {
        private PlayTrailerParam(String url) {
            super(url, false);
        }
        private PlayTrailerParam(String url, boolean isLandscape) {
            super(url, isLandscape);
        }
    }

    private static class PlayParamBase<T> extends PlayParam {
        private PlayParamBase(String url, boolean isLandscape) {
            this.url = url;
            this.isLandscape = isLandscape;
        }
    }
}
