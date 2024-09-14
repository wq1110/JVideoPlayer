package com.jw.media.jvideoplayer.player.base;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.player.base.render.MediaRenderView;

import java.util.HashMap;
import java.util.Map;

import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Joyce.wang on 2024/4/2 18:47
 *
 * @Description 播放view（视频回调与状态处理等相关层）
 */
public class VideoPlayer extends MediaRenderView implements MediaPlayerListener {
    private static Logger logger = LoggerFactory.getLogger(VideoPlayer.class.getName());
    protected Context mContext;
    //音频焦点的监听
    protected AudioManager mAudioManager;
    protected int mMaxVolume;
    protected String mVideoUrl;
    protected Map<String, String> mHeaders;
    //是否是预告片
    protected boolean isTrailer;
    //是否循环播放
    protected boolean mLooping = false;
    //播放速度
    protected float mSpeed = 1;
    protected long mSeekWhenPrepared;//记录寻求位置而做准备
    //是否准备完成前调用了暂停
    protected boolean mPauseBeforePrepared = false;
    protected boolean isSeeking = false;//是否正在seek任务
    protected long preferSeekPos = -1;
    //播放缓冲监听
    protected int mCurrentBufferPercentage;
    //当前的播放状态
    protected PlayerStatus mCurrentState = PlayerStatus.STATE_IDLE;
    protected int mCurrentMode = VideoType.MODE_NORMAL;

    //是否纯音频播放
    protected boolean isAudioOnly = false;

    protected OnPlayerEventListener mOnPlayerEventListener;
    protected IMediaPlayer.OnPreparedListener mOnPreparedListener;
    protected IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    protected IMediaPlayer.OnErrorListener mOnErrorListener;
    protected IMediaPlayer.OnCompletionListener mOnCompletionListener;
    protected IMediaPlayer.OnInfoListener mOnInfoListener;
    protected IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    protected Handler mMainHandler = new Handler(Looper.getMainLooper());

    public VideoPlayer(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    protected void init(Context context) {
        mContext = context;
        mRenderContainer = new FrameLayout(mContext);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mRenderContainer, params);
        if (isInEditMode())
            return;
        mAudioManager = (AudioManager) mContext.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public void attachMediaController(View mediaController) {
        mRenderContainer.removeAllViews();
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mRenderContainer.addView(mediaController, params);
    }

    /**
     *@description Enters full-screen mode， 将mRenderContainer(内部包含mTextureView（或者mSurfaceView）和mController)从当前容器中移除，并添加到android.R.content中
     *
     *@param activity The activity to enter full-screen in.
     */
    public void enterFullScreen(Activity activity) {
        if (activity == null || mCurrentMode == VideoType.MODE_FULL_SCREEN) {
            return;
        }

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ViewGroup contentView = activity.findViewById(android.R.id.content);
        if (mCurrentMode == VideoType.MODE_SMALL_WINDOW) {
            contentView.removeView(mRenderContainer);
        } else {
            this.removeView(mRenderContainer);
        }

        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        contentView.addView(mRenderContainer, params);
        mCurrentMode = VideoType.MODE_FULL_SCREEN;
    }

    /**
     *@description Exits full-screen mode. 移除mTextureView（或者mSurfaceView）和mController，并添加到非全屏的容器中
     *
     *@param activity The activity to exit full-screen from.
     *@return True if successful, false otherwise.
     */
    public boolean exitFullScreen(Activity activity) {
        if (activity == null || mCurrentMode != VideoType.MODE_FULL_SCREEN) {
            return false;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ViewGroup contentView = activity.findViewById(android.R.id.content);
        contentView.removeView(mRenderContainer);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mRenderContainer, params);

        mCurrentMode = VideoType.MODE_NORMAL;
        return true;
    }

    /**
     *@description Sets the video URL to play.
     *
     *@param url The URL of the video.
     */
    public void setVideoUrl(String url) {
        setVideoUrl(url, null, false);
    }

    /**
     *@description Sets the video URL to play with custom headers.
     *
     *@param url     The URL of the video.
     *@param headers HTTP headers to use for the request.
     */
    public void setVideoUrl(String url, Map<String, String> headers) {
        setVideoUrl(url, headers, false);
    }

    /**
     *@description Sets the video URL to play with options.
     *
     *@param url         The URL of the video.
     *@param headers     HTTP headers to use for the request.
     *@param isAudioOnly True if only audio should be played, false otherwise.
     */
    public void setVideoUrl(String url, Map<String, String> headers, boolean isAudioOnly) {
        this.mVideoUrl = url;
        this.mHeaders = headers;
        this.isAudioOnly = isAudioOnly;
        mCurrentState = PlayerStatus.STATE_PREPARING;
        mSeekWhenPrepared = 0;
        mCurrentBufferPercentage = 0;
        prepareVideo();
    }

    /**
     * @Description Starts video preparation.
     */
    private void prepareVideo() {
        startPrepare();
    }

    private void startPrepare() {
        getVideoManager().setMediaPlayerListener(this);
        setPlayerType();
        mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        getVideoManager().prepare(mVideoUrl, (mHeaders == null) ? new HashMap<String, String>() : mHeaders, mLooping, mSpeed, isAudioOnly);
        updatePlayerStatus(PlayerStatus.STATE_PREPARING);
    }

    private void setPlayerType() {
        int playerType = mOnPlayerEventListener != null ? mOnPlayerEventListener.getPlayerType() : VideoType.PV_PLAYER__IjkMediaPlayer;
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener.onInfoEvent(null, VideoType.MEDIA_INFO_PLAYER_TYPE, playerType);
        }
        switch (playerType) {
            case VideoType.PV_PLAYER__IjkExoMediaPlayer:
                PlayerFactory.setPlayManager(ExoPlayerManager.class);
                break;
            case VideoType.PV_PLAYER__AndroidMediaPlayer:
                PlayerFactory.setPlayManager(SystemPlayerManager.class);
                break;
            case VideoType.PV_PLAYER__IjkMediaPlayer:
                PlayerFactory.setPlayManager(IjkPlayerManager.class);
                break;
            default:
                PlayerFactory.setPlayManager(IjkPlayerManager.class);
                break;
        }
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        if (mCurrentState != PlayerStatus.STATE_PREPARING) {
            return;
        }
        updatePlayerStatus(PlayerStatus.STATE_PREPARED);

        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener.onPreparedEvent(mp);
        }
        startAfterPrepared();
        if (mOnPreparedListener != null) mOnPreparedListener.onPrepared(mp);
    }

    /**
     *@description prepared成功之后会开始播放
     *@return
     */
    private void startAfterPrepared() {
        if (mCurrentState != PlayerStatus.STATE_PREPARED) {
            prepareVideo();
        }

        try {
            getVideoManager().start();
            updatePlayerStatus(PlayerStatus.STATE_PLAYING);

            final long seekToPosition = mSeekWhenPrepared;

            if (seekToPosition > 0) {
                mSeekWhenPrepared = 0;
                getVideoManager().seekTo(seekToPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        addRenderView();

        if (mPauseBeforePrepared) {
            pause();
            mPauseBeforePrepared = false;
        }
    }
    /**
     *@description 暂停播放
     *@return
     */
    @Override
    public void pause() {
        if (mCurrentState == PlayerStatus.STATE_PREPARING) {
            mPauseBeforePrepared = true;
        }
        try {
            if (isInPlaybackState()) {
                if (getVideoManager().isPlaying()) {
                    getVideoManager().pause();

                    updatePlayerStatus(PlayerStatus.STATE_PAUSED);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *@description 恢复播放
     *@return
     */
    @Override
    public void resumePlay() {
        resumePlay(true);
    }

    private void resumePlay(boolean isResetLastPlayTime) {
        mPauseBeforePrepared = false;
        if (isInPlaybackState()) {
            try {
                getVideoManager().start();
                updatePlayerStatus(PlayerStatus.STATE_PLAYING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     *@description 释放播放
     *@return
     */
    public void release() {
        mMainHandler.post(() -> {
            if (mRenderView !=  null && mRenderView.getRenderView() !=  null) {
                mRenderView.getRenderView().setVisibility(GONE);//解决release后，最后一帧画面依然展示问题
            }
        });
        getVideoManager().releaseMediaPlayer();
        isSeeking = false;
        preferSeekPos = -1;
        updatePlayerStatus(PlayerStatus.STATE_IDLE);
    }

    /**
     *@description 正常播放完成回调
     *@return
     */
    @Override
    public void onCompletion(IMediaPlayer mp) {
        updatePlayerStatus(PlayerStatus.STATE_COMPLETED);

        if (mRenderView !=  null && mRenderView.getRenderView() !=  null) {
            mRenderContainer.removeView(mRenderView.getRenderView());
        }

        mAudioManager.abandonAudioFocus(null);

        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener.onCompletionEvent(mp);
        }
        if (mOnCompletionListener != null) mOnCompletionListener.onCompletion(mp);
    }

    @Override
    public void onSeekComplete(IMediaPlayer mp) {
        isSeeking = false;
        if (preferSeekPos >= 0) {
            final long pos = preferSeekPos;
            preferSeekPos = -1;
            mMainHandler.post(() -> {
               seekTo(pos);
            });
        }

        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener.onSeekCompleteEvent(mp);
        }
        if (mOnSeekCompleteListener != null) mOnSeekCompleteListener.onSeekComplete(mp);
    }

    @Override
    public void onError(IMediaPlayer mp, int what, int extra) {
        logger.e("onError what: %s, extra: %s", what, extra);
        if (VideoType.FFP_MSG_ERROR_997 != extra && VideoType.FFP_MSG_ERROR_998 != extra) {
            updatePlayerStatus(PlayerStatus.STATE_ERROR);
        }
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener.onErrorEvent(mp, what, extra);
        }
        if (mOnErrorListener != null) mOnErrorListener.onError(mp, what, extra);
    }

    @Override
    public void onInfo(IMediaPlayer mp, int what, int extra) {
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener.onInfoEvent(mp, what, extra);
        }
        if (mOnInfoListener != null) mOnInfoListener.onInfo(mp, what, extra);
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {
        mCurrentBufferPercentage = percent;
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener.onBufferingUpdateEvent(mp, percent);
        }
        if (mOnBufferingUpdateListener != null) mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
    }

    /**
     *@description Update the player's status and notify listeners.
     *
     *@param newState The new player status.
     */
    private void updatePlayerStatus(PlayerStatus newState) {
        if (mCurrentState != newState) {
            mCurrentState = newState;
            if (mOnPlayerEventListener != null) {
                mOnPlayerEventListener.setStateAndUi(mCurrentState, true);
            }
        }
    }

    /**
     *@description Get the current buffer percentage
     *
     *@return The current buffer percentage.
     */
    public int getCurrentBufferPercentage() {
        return mCurrentBufferPercentage;
    }

    /**
     *@description Get the total duration of the video.
     *
     *@return The duration in milliseconds, or -1 if an error occurs.
     */
    public long getDuration() {
        try {
            if (isInPlaybackState()) {
                return getVideoManager().getDuration();
            }
        } catch (Exception e) {
            logger.e("Error getting duration: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
    /**
     *@description Check if the video is currently playing. (Pause will return false)
     *
     *@return True if playing, false otherwise.
     */
    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && getVideoManager().isPlaying();
    }

    @Override
    public int getCurrentVideoWidth() {
        return getVideoManager().getCurrentVideoWidth();
    }

    @Override
    public int getCurrentVideoHeight() {
        return getVideoManager().getCurrentVideoHeight();
    }

    @Override
    public int getVideoSarNum() {
        return getVideoManager().getVideoSarNum();
    }

    @Override
    public int getVideoSarDen() {
        return getVideoManager().getVideoSarDen();
    }

    @Override
    public int getAspectRatioMode() {
        return getVideoManager().getAspectRatioMode();
    }

    /**
     *@description Set the aspect ratio mode for video playback.
     *
     *@param aspectRatioMode The desired aspect ratio mode.
     */
    public void setAspectRatioMode(int aspectRatioMode) {
        mMainHandler.post(() -> {
            if (aspectRatioMode >= 0) {
                getVideoManager().setAspectRatioMode(aspectRatioMode);
                if (mRenderView !=null) {
                    mRenderView.requestLayout();
                }
            }
        });
    }

    /**
     *@description Set the display surface for video rendering.
     *
     *@param surface The surface to render the video on.
     */
    @Override
    protected void setDisplay(Surface surface) {
        getVideoManager().setDisplay(surface);
    }

    /**
     *@description Set the video aspect ratio and dimensions.
     *
     *@param aspectRatio The desired aspect ratio.
     *@param width       The width of the video.
     *@param height      The height of the video.
     */
    public void setVideoAspectRatio(final double aspectRatio, final int width, final int height) {
        mMainHandler.post(() -> {
            if (width > 0 && height > 0) {
                getVideoManager().setSelfVideoSizeFlag(true);
                getVideoManager().setCurrentVideoSize(width, height);
                if (mRenderView != null) {
                    mRenderView.requestLayout();
                }
            }
        });
    }

    public void setSelfVideoSizeFlag(boolean selfVideoSizeFlag) {
        getVideoManager().setSelfVideoSizeFlag(selfVideoSizeFlag);
    }

    @Override
    public void setNeedMute(boolean needMute) {
        getVideoManager().setNeedMute(needMute);
    }

    public void setTrailer(boolean isTrailer) {
        this.isTrailer = isTrailer;
    }

    /**
     *@description Get the estimated time of cached video ahead of the current position.
     *
     *@return The cache time in seconds.
     */
    public int getPlayerCacheTime() {
        try {
            if (isInPlaybackState()) {
                int currentPosition = (int) getCurrentPosition();
                int cachePosition = (int) (mCurrentBufferPercentage * getDuration() / 100);
                int playerCacheTime = (cachePosition - currentPosition) / 1000;
                return Math.max(playerCacheTime, 0);
            }
        } catch (Throwable e) {
            logger.e("Error getting player cache time: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    /**
     *@description Get the current playback position.
     *
     *@return The current position in milliseconds.
     */
    public long getCurrentPosition() {
        try {
            if (isInPlaybackState()) {
                if (preferSeekPos >= 0) {
                    return preferSeekPos;
                }
                return getVideoManager().getCurrentPosition();
            }
        } catch (Exception e) {
            logger.e("Error getting current position: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     *@description Seek to a specific position in the video.
     *
     *@param position position The position to seek to in milliseconds.
     */
    public void seekTo(long position) {
        try {
            if (position < 0) {
                return;
            }
            if (isInPlaybackState()) {
                if (isSeeking) {
                    //防止播放中多次点击连续seek导致回跳
                    preferSeekPos = position;
                } else {
                    if (getVideoManager().getPlayer() != null &&
                            getVideoManager().getPlayer().getMediaPlayer() != null
                            && !(getVideoManager().getPlayer().getMediaPlayer() instanceof IjkExoMediaPlayer)) {
                        //exo播放器seek完成后不会回调onSeekComplete接口，会导致seek到最后，结束播放会有点延后，这里只针对ijk和amp处理。
                        isSeeking = true;
                    }
                    getVideoManager().seekTo(position);
                    mSeekWhenPrepared = 0;
                }
            } else {
                mSeekWhenPrepared = position;
            }
        } catch (Exception e) {
            logger.e("Error seekTo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     *@description Check if the player is in a playback state (not idle, error, or preparing).
     *
     *@return True if in a playback state, false otherwise.
     */
    public boolean isInPlaybackState() {
        return (mCurrentState != PlayerStatus.STATE_ERROR &&
                mCurrentState != PlayerStatus.STATE_IDLE &&
                mCurrentState != PlayerStatus.STATE_PREPARING);
    }

    /**
     *@description Get the current playback state of the player.
     *
     *@return The current PlayerStatus.
     */
    public PlayerStatus getCurrentPlayState() {
        return mCurrentState;
    }

    @Override
    protected void onSurfaceCreatedEvent(Surface surface) {
        seekTo(getCurrentPosition());
    }

    @Override
    protected void onSurfaceChangedEvent(Surface surface, int width, int height) {
        boolean isValidState = mCurrentState == PlayerStatus.STATE_PLAYING;
        boolean hasValidSize = (mRenderView !=  null && !mRenderView.shouldWaitForResize()) || (getCurrentVideoWidth() == width && getCurrentVideoHeight() == height);
        if (isValidState && hasValidSize) {
            final long seekToPosition = mSeekWhenPrepared;
            if (seekToPosition > 0) {
                mSeekWhenPrepared = 0;
                seekTo((int) seekToPosition);
            }
            resumePlay(false);
        }
    }

    @Override
    protected void onSurfaceDestroyedEvent(Surface surface) {
        getVideoManager().releaseSurface(surface);
    }

    @Override
    public void onVideoSizeChanged() {
        int currentVideoWidth = getVideoManager().getCurrentVideoWidth();
        int currentVideoHeight = getVideoManager().getCurrentVideoHeight();
        if (currentVideoWidth != 0 && currentVideoHeight != 0 && mRenderView != null) {
            mRenderView.requestLayout();
        }
    }
    /**
     * 获取管理器桥接的实现
     */
    public VideoPlayerBridge getVideoManager() {
        return PlayerManager.getInstance();
    }

    public <T> void setPlayerListener(T listener) {
        if (listener instanceof IMediaPlayer.OnPreparedListener) {
            mOnPreparedListener = (IMediaPlayer.OnPreparedListener) listener;
        } else if (listener instanceof IMediaPlayer.OnInfoListener) {
            mOnInfoListener = (IMediaPlayer.OnInfoListener) listener;
        } else if (listener instanceof IMediaPlayer.OnCompletionListener) {
            mOnCompletionListener = (IMediaPlayer.OnCompletionListener) listener;
        } else if (listener instanceof IMediaPlayer.OnErrorListener) {
            mOnErrorListener = (IMediaPlayer.OnErrorListener) listener;
        } else if (listener instanceof IMediaPlayer.OnBufferingUpdateListener) {
            mOnBufferingUpdateListener = (IMediaPlayer.OnBufferingUpdateListener) listener;
        } else if (listener instanceof IMediaPlayer.OnSeekCompleteListener) {
            mOnSeekCompleteListener = (IMediaPlayer.OnSeekCompleteListener) listener;
        }
    }

    public void setOnPlayerEventListener(OnPlayerEventListener l) {
        this.mOnPlayerEventListener = l;
    }
    public void onDestroy() {
        release();
    }
}
