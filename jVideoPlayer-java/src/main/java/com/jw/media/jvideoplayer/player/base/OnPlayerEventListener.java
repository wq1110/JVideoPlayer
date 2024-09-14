package com.jw.media.jvideoplayer.player.base;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Joyce.wang on 2024/4/19 14:37
 *
 * @Description TODO
 */
public interface OnPlayerEventListener {
    void setStateAndUi(PlayerStatus state, boolean isResetLastPlayTime);
    void onPreparedEvent(IMediaPlayer mp);
    void onCompletionEvent(IMediaPlayer mp);
    void onSeekCompleteEvent(IMediaPlayer mp);
    void onErrorEvent(IMediaPlayer mp, int what, int extra);
    void onInfoEvent(IMediaPlayer mp, int what, int extra);
    void onBufferingUpdateEvent(IMediaPlayer mp, int percent);
    int getPlayerType();
}
