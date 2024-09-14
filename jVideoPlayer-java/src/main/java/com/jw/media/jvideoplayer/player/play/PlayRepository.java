package com.jw.media.jvideoplayer.player.play;

import android.app.Activity;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.jw.media.jvideoplayer.cache.utils.ThreadUtils;
import com.jw.media.jvideoplayer.java.R;
import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.lib.provider.ContextProvider;
import com.jw.media.jvideoplayer.lib.utils.NetworkUtils;
import com.jw.media.jvideoplayer.lib.utils.ToastUtils;
import com.jw.media.jvideoplayer.mvx.mvp.APIDataListener;
import com.jw.media.jvideoplayer.player.base.PlayerStatus;
import com.jw.media.jvideoplayer.player.base.VideoPlayer;
import com.jw.media.jvideoplayer.player.base.VideoType;
import com.jw.media.jvideoplayer.player.controller.IMediaController;
import com.jw.media.jvideoplayer.player.listener.Action;
import com.jw.media.jvideoplayer.player.listener.OnConnectionChangeListener;
import com.jw.media.jvideoplayer.player.listener.OnMediaControllerListener;
import com.jw.media.jvideoplayer.player.model.PlayParam;
import com.jw.media.jvideoplayer.player.youtube.YoutubeRemoteDataSource;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Joyce.wang on 2024/9/13 9:56
 *
 * @Description TODO
 */
public class PlayRepository extends BaseDataModel {
    private static Logger logger = LoggerFactory.getLogger(PlayRepository.class.getSimpleName());

    protected VideoPlayer mVideoPlayer;
    protected IMediaController mMediaController;
    protected PlayProxyDataModel mPlayProxyDataModel;
    protected PlayOperateDataModel mPlayOperateDataModel;
    protected PlayTrailerDataModel mPlayTrailerDataModel;
    protected YoutubeRemoteDataSource mYoutubeRemoteDataSource;

    protected boolean isLandscape;//是否横屏播放
    protected PlayLiveData mPlayLiveData;

    protected WeakReference<IMediaPlayer.OnPreparedListener> onPreparedListener;
    protected WeakReference<IMediaPlayer.OnBufferingUpdateListener> onBufferingUpdateListener;
    protected WeakReference<IMediaPlayer.OnErrorListener> onErrorListener;
    protected WeakReference<IMediaPlayer.OnCompletionListener> onCompletionListener;
    protected WeakReference<IMediaPlayer.OnInfoListener> onInfoListener;
    protected WeakReference<IMediaPlayer.OnSeekCompleteListener> onSeekCompleteListener;
    private volatile static PlayRepository instance;

    private PlayRepository() {
        mPlayProxyDataModel = new PlayProxyDataModel(this);
        mPlayOperateDataModel = new PlayOperateDataModel(this);
        mPlayTrailerDataModel = new PlayTrailerDataModel(this);

        mYoutubeRemoteDataSource = new YoutubeRemoteDataSource();
        mPlayLiveData = new PlayLiveData();
    }

    public static PlayRepository getInstance() {
        if (instance == null) {
            synchronized (PlayRepository.class) {
                if (instance == null) {
                    instance = new PlayRepository();
                }
            }
        }
        return instance;
    }

    public void setVideoView(@NonNull VideoPlayer videoPlayer) {
        clean(true);
        this.mVideoPlayer = videoPlayer;
        initPlayerListener();
    }

    public void setMediaController(@NonNull IMediaController mediaController) {
        this.mMediaController = mediaController;
        this.mMediaController.setOnMediaControllerListener(onMediaControllerListener);
    }

    private void initPlayerListener() {
        if (mVideoPlayer != null) {
            setListener(mVideoPlayer, IMediaPlayer.OnPreparedListener.class, this::onPrepared);
            setListener(mVideoPlayer, IMediaPlayer.OnCompletionListener.class, this::onCompletion);
            setListener(mVideoPlayer, IMediaPlayer.OnInfoListener.class, this::onInfo);
            setListener(mVideoPlayer, IMediaPlayer.OnErrorListener.class, this::onError);
            setListener(mVideoPlayer, IMediaPlayer.OnBufferingUpdateListener.class, this::onBufferingUpdate);
            setListener(mVideoPlayer, IMediaPlayer.OnSeekCompleteListener.class, this::onSeekComplete);
        }
    }

    // 设置播放器监听器的通用方法
    private <T> void setListener(VideoPlayer player, Class<T> listenerClass, T listener) {
        player.setPlayerListener(listenerClass.cast(listener));
    }

    private void onPrepared(IMediaPlayer mp) {
        if (onPreparedListener != null && onPreparedListener.get() != null) {
            onPreparedListener.get().onPrepared(mp);
        }
    }

    private void onCompletion(IMediaPlayer mp) {
        if (onCompletionListener != null && onCompletionListener.get() != null) {
            onCompletionListener.get().onCompletion(mp);
        }
        clean(false);
    }

    private boolean onInfo(IMediaPlayer mp, int what, int extra) {
        if (onInfoListener != null && onInfoListener.get() != null) {
            onInfoListener.get().onInfo(mp, what, extra);
        }
        return true;
    }

    private boolean onError(IMediaPlayer mp, int what, int extra) {
        if (onErrorListener != null && onErrorListener.get() != null) {
            onErrorListener.get().onError(mp, what, extra);
        }
        clean(false);
        return true;
    }

    private void onBufferingUpdate(IMediaPlayer mp, int percent) {
        if (onBufferingUpdateListener != null && onBufferingUpdateListener.get() != null) {
            onBufferingUpdateListener.get().onBufferingUpdate(mp, percent);
        }
    }

    private void onSeekComplete(IMediaPlayer mp) {
        if (onSeekCompleteListener != null && onInfoListener.get() != null) {
            onSeekCompleteListener.get().onSeekComplete(mp);
        }
    }

    public void startPlay(@NonNull PlayParam playparam, APIDataListener<Boolean> listener) {
        if (mVideoPlayer == null || mMediaController == null) {
            logger.e("video player or media controller not exist, videoPlayer: %s, mediaController: %s", mVideoPlayer, mMediaController);
            ExceptionUtil.handleError(listener, PlayConstant.Error.ERROR_CODE_NO_READY, null);
            return;
        }

        if (playparam instanceof PlayParam.PlayTrailerParam) {
            mPlayTrailerDataModel.mLocalProxyVideoControl.setVideoView(mVideoPlayer);
        }

        if (playparam.isLandscape) {
            mMediaController.enterFullScreen();
        } else {
            mMediaController.exitFullScreen();
        }

        mMediaController.showLoadingBox();
        mMediaController.hideMediaPlay();
        mMediaController.setIsTrailer(playparam instanceof PlayParam.PlayTrailerParam);
        mMediaController.setAllowDisplayNextEpisode(false);

        ThreadUtils.submitRunnableTask(() -> {
            if (playparam instanceof PlayParam.PlayTrailerParam) {
                //播放预告片
                mPlayTrailerDataModel.playTrailer((PlayParam.PlayTrailerParam) playparam, listener);
            }
        });
    }

    public void nextEpisode(@NonNull Activity activity, APIDataListener<Void> listener) {
        // TODO: NOT SUPPORT YET
    }

    public void resumePlay() {
        if (mVideoPlayer != null) {
            mVideoPlayer.resumePlay();
        }
    }

    public void pause() {
        if (mVideoPlayer != null) {
            mVideoPlayer.pause();
        }
    }

    public void seekTo(int position) {
        if (mVideoPlayer != null) {
            mVideoPlayer.seekTo(position);
        }
    }

    public void stop() {
        clean(false);
    }

    public void setNeedMute(boolean needMute) {
        if (mVideoPlayer != null) {
            mVideoPlayer.setNeedMute(needMute);
        }
    }

    public void setAspectRatioMode(int aspectRatioMode) {
        if (mVideoPlayer != null) {
            mVideoPlayer.setAspectRatioMode(aspectRatioMode);
        }
    }

    public boolean isPlaying() {
        if (mVideoPlayer != null) {
            return mVideoPlayer.isPlaying();
        }
        return false;
    }

    public long getDuration() {
        if (mVideoPlayer != null) {
            return mVideoPlayer.getDuration();
        }
        return -1;
    }

    public long getCurrentPosition() {
        if (mVideoPlayer != null) {
            return mVideoPlayer.getCurrentPosition();
        }
        return 0;
    }

    public PlayerStatus getCurrentPlayState() {
        if (mVideoPlayer != null) {
            mVideoPlayer.getCurrentPlayState();
        }
        return PlayerStatus.STATE_IDLE;
    }

    public int getBufferPercentage() {
        if (mVideoPlayer != null) {
            return mVideoPlayer.getCurrentBufferPercentage();
        }
        return 0;
    }

    public int getCurrentAspectRatioMode() {
        if (mVideoPlayer != null) {
            return mVideoPlayer.getAspectRatioMode();
        }
        return VideoType.AR_ASPECT_FIT_PARENT;
    }
    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {
        this.onPreparedListener = new WeakReference<>(l);
    }

    public void setOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener l) {
        this.onBufferingUpdateListener = new WeakReference<>(l);
    }

    public void setOnErrorListener(IMediaPlayer.OnErrorListener l) {
        this.onErrorListener = new WeakReference<>(l);
    }

    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener l) {
        this.onCompletionListener = new WeakReference<>(l);
    }

    public void setOnInfoListener(IMediaPlayer.OnInfoListener l) {
        this.onInfoListener = new WeakReference<>(l);
    }

    public void setOnSeekCompleteListener(IMediaPlayer.OnSeekCompleteListener l) {
        this.onSeekCompleteListener = new WeakReference<>(l);
    }

    public void clean(boolean isClearPlayerView) {
        mPlayProxyDataModel.clean();
        if (mMediaController != null) {
            mMediaController.resetPlayer();
            if (isClearPlayerView) {
                mMediaController.setOnMediaControllerListener(null);
                mMediaController = null;
            }
        }

        if (isClearPlayerView) {
            mVideoPlayer = null;
            mPlayLiveData = new PlayLiveData();
        }
    }

    private OnMediaControllerListener onMediaControllerListener = new OnMediaControllerListener() {

        @Override
        public void onNetworkChange(Activity activity, Action<Boolean> action) {
            if (activity == null || activity.isFinishing() || action == null) {
                return;
            }
            mPlayOperateDataModel.showNetworkChooseDialog(activity, new APIDataListener<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    action.call(result);
                }

                @Override
                public void onError(Throwable exception) {
                    ToastUtils.showToast(ContextProvider.getContext(), exception.getMessage());
                }
            });
        }

        @Override
        public void onNetworkChange(OnConnectionChangeListener.ConnectionType type) {
            if (OnConnectionChangeListener.ConnectionType.NONE == type) {
                Observable.timer(2, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) throws Exception {
                                if (!NetworkUtils.isConnectingToInternet()) {
                                    clean(false);
                                    ToastUtils.showToast(ContextProvider.getContext(), R.string.sdk_vod_c_play_str_network_is_disconnected);
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                            }
                        });
            }
        }

        @Override
        public void onNetworkDisconnected(Activity activity, Action<Boolean> action) {
            if (activity == null|| activity.isFinishing() || action == null) {
                return;
            }

            // TODO: Add some handling
            if (action != null) {
                action.call(true);
            }
        }

        @Override
        public void onBufferingStart(IMediaPlayer mp) {

        }

        @Override
        public void onBufferingEnd(IMediaPlayer mp, boolean isPrepare, long bufferingStartTime, long bufferingTime, long prepareFirstBufferingTime, long seekFirstBuffingTime, long startBufferingPosition) {

        }

        @Override
        public boolean isMobileTraffic() {
            return true;
        }

        @Override
        public void showSubtitleDialog(FragmentManager fragmentManager) {

        }

        @Override
        public void showEpisodesDialog(Activity activity, FragmentManager fragmentManager) {

        }

        @Override
        public void showResourceDialog(Activity activity, FragmentManager fragmentManager) {

        }

        @Override
        public void showResolutionDialog(Activity activity, FragmentManager fragmentManager) {

        }

        @Override
        public void showMoreDialog(Activity activity, FragmentManager fragmentManager) {

        }

        @Override
        public void onPreparePlay(Activity activity) {
            if (activity == null || activity.isFinishing() || mPlayLiveData == null) {
                return;
            }

            startPlay(PlayParam.toPlayTrailer(mPlayLiveData.getUrl(), mPlayLiveData.isLandscape()), new APIDataListener<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    logger.i("onPreparePlay success, result: %s", result);
                    if (!result) {
                        clean(false);
                    }
                }

                @Override
                public void onError(Throwable exception) {
                    ToastUtils.showToast(ContextProvider.getContext(), exception.getMessage());
                    exception.printStackTrace();
                    clean(false);
                }
            });
        }

        @Override
        public void onResetPlayer() {
            clean(true);
        }

        @Override
        public void onNextEpisode(Activity activity) {

        }

        @Override
        public void onToggleRatio() {

        }

        @Override
        public void onPlayerType(int playerType) {

        }

        @Override
        public void onFromBeginning() {

        }

        @Override
        public void onBack() {

        }

        @Override
        public void finish() {

        }
    };
}
