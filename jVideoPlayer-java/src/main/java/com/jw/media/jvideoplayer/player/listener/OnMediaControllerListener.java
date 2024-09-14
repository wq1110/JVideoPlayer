package com.jw.media.jvideoplayer.player.listener;

import android.app.Activity;

import androidx.fragment.app.FragmentManager;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Joyce.wang on 2024/3/20 11:15
 *
 * @Description TODO
 */
public interface OnMediaControllerListener {
    void onNetworkChange(Activity activity, Action<Boolean> action);
    void onNetworkChange(OnConnectionChangeListener.ConnectionType type);
    void onNetworkDisconnected(Activity activity, Action<Boolean> action);

    void onBufferingStart(IMediaPlayer mp);
    void onBufferingEnd(IMediaPlayer mp, boolean isPrepare, long bufferingStartTime,
                        long bufferingTime, long prepareFirstBufferingTime,
                        long seekFirstBuffingTime, long startBufferingPosition);

    boolean isMobileTraffic();
    void showSubtitleDialog(FragmentManager fragmentManager);
    void showEpisodesDialog(Activity activity, FragmentManager fragmentManager);
    void showResourceDialog(Activity activity, FragmentManager fragmentManager);
    void showResolutionDialog(Activity activity, FragmentManager fragmentManager);
    void showMoreDialog(Activity activity, FragmentManager fragmentManager);
    void onPreparePlay(Activity activity);
    void onResetPlayer();
    void onNextEpisode(Activity activity);
    void onToggleRatio();
    void onPlayerType(int playerType);
    void onFromBeginning();
    void onBack();
    void finish();
}
