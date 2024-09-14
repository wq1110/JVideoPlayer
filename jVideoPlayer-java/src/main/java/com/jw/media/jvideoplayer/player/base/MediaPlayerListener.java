package com.jw.media.jvideoplayer.player.base;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Joyce.wang on 2024/3/11 16:48
 *
 * @Description TODO
 */
public interface MediaPlayerListener {
    void onPrepared(IMediaPlayer mp);

    void onCompletion(IMediaPlayer mp);

    void onBufferingUpdate(IMediaPlayer mp, int percent);

    void onSeekComplete(IMediaPlayer mp);

    void onError(IMediaPlayer mp, int what, int extra);

    void onInfo(IMediaPlayer mp, int what, int extra);

    void onVideoSizeChanged();

    void pause();

    void resumePlay();

    boolean isPlaying();

    void setNeedMute(boolean needMute);
}
