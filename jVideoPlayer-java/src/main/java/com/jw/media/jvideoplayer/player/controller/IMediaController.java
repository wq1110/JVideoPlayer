package com.jw.media.jvideoplayer.player.controller;

import android.text.Spanned;

import com.jw.media.jvideoplayer.lib.ass.AssView;
import com.jw.media.jvideoplayer.player.base.PlayerStatus;
import com.jw.media.jvideoplayer.player.listener.OnMediaControllerListener;

/**
 * Created by Joyce.wang on 2024/4/16 19:10
 *
 * @Description An interface for media controller
 */
public interface IMediaController {

    //Device State(区分手机还是盒子)
    boolean isMobileDevice();

    // Playback State
    boolean isPlaying();
    boolean isInPlaybackState();
    long getDuration();
    long getCurrentPosition();
    PlayerStatus getCurrentPlayState();
    int getCurrentBufferPercentage();

    //UI Control
    void hideLoadingBox();
    void showLoadingBox();
    void hideMediaPlay();
    void showMediaPlay();
    void setMediaTitle(String title);

    //Content Meta data
    void setIsSeries(boolean isSeries);
    void setIsTrailer(boolean isTrailer);
    void setIsCachedFilm(boolean isCacheFinishFilm);
    void setIsFromCachePage(boolean isFromCachePage);
    void setAllowDisplayNextEpisode(boolean isAllowDisplayNextEpisode);

    //Loading Information
    void setLoadingPercent(String loadingPercent);
    void setLoadingSpeed(String loadingSpeed);
    void setLoadingExtraInfo(String txt);

    AssView getMediaAssView();
    void setSubtitleTextSize(float size);
    void setSubtitleTextColor(int textColor);
    void setSubtitleBackgroundColor(int backColor);
    void toggleSubtitleView(boolean show);
    void updateSubtitleText(boolean subtitleInShifting, Spanned spannedText);

    //Time Tracking
    long getActualPlaybackTime();
    void resetPlaybackTime();
    void setStatisticSub(boolean isStatisticSub);
    long getActualSubtitleTime();
    void resetSubtitleTime(boolean isResetLastTime);

    //Player Control
    void resetPlayer();
    void enterFullScreen();
    void exitFullScreen();
    void setOnMediaControllerListener(OnMediaControllerListener onPlayerListener);
}
