package com.jw.media.jvideoplayer.player.base;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.lib.provider.ContextProvider;

import java.lang.ref.WeakReference;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Joyce.wang on 2024/3/11 15:51
 *
 * @Description Manages player using an underlying IPlayerManager.
 *              播放器和基础播放View（VideoPlayer）对接交互层
 */
class PlayerManager implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnInfoListener, VideoPlayerBridge {
    private static Logger logger = LoggerFactory.getLogger(PlayerManager.class.getName());
    
    protected static final int MSG_PREPARE = 0;
    protected static final int MSG_SET_DISPLAY = 1;
    protected static final int MSG_RELEASE = 2;
    protected static final int MSG_RELEASE_SURFACE = 3;

    protected int currentVideoWidth = 0;//当前播放的视频宽的高
    protected int currentVideoHeight = 0;//当前播放的视屏的高
    protected int currentVideoSarNum = 0;
    protected int currentVideoSarDen = 0;
    protected int currentAspectRatioMode = VideoType.AR_ASPECT_FIT_PARENT;

    protected IPlayerManager playerManager; //播放器内核管理
    protected boolean needMute = false;//是否需要静音
    protected WeakReference<MediaPlayerListener> mediaPlayerListenerRef;
    private volatile boolean selfVideoSizeFlag;
    private static volatile PlayerManager instance;
    private PlayerManager() {
    }
    public static PlayerManager getInstance() {
        if (instance == null) {
            synchronized (PlayerManager.class) {
                if (instance == null) {
                    instance = new PlayerManager();
                }
            }
        }
        return instance;
    }
    private final Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PREPARE:
                    initVideo((InitializationPlayerConfig) msg.obj);
                    break;
                case MSG_SET_DISPLAY:
                    handleSetDisplay(msg.obj);
                    break;
                case MSG_RELEASE:
                    selfVideoSizeFlag = false;
                    releaseMediaPlayerInternal();
                    break;
                case MSG_RELEASE_SURFACE:
                    releaseSurfaceInternal(msg.obj);
                    break;
            }
        }
    };

    // Extract setDisplay logic into a separate method
    private void handleSetDisplay(Object displayObj) {
        if (displayObj instanceof Surface && playerManager != null) {
            playerManager.setDisplay((Surface) displayObj);
        } else {
            // Handle invalid display object, log an error, etc.
        }
    }

    private void releaseMediaPlayerInternal() {
        if (playerManager != null) {
            playerManager.release();
            playerManager = null;
        }
        setNeedMute(false);
    }

    private void releaseSurfaceInternal(Object obj) {
        if (obj != null && playerManager != null) {
            playerManager.releaseSurface();
        }
    }

    protected IPlayerManager getPlayManager() {
        return PlayerFactory.getPlayManager();
    }

    private void initVideo(InitializationPlayerConfig config) {
        try {
            currentVideoWidth = 0;
            currentVideoHeight = 0;
            currentVideoSarNum = 0;
            currentVideoSarDen = 0;
            selfVideoSizeFlag = false;

            if (playerManager != null) {
                playerManager.release();
            }
            playerManager = getPlayManager();

            playerManager.initVideoPlayer(ContextProvider.getContext(), config);

            if (playerManager != null) {
                playerManager.setNeedMute(needMute);
            }
            IMediaPlayer mediaPlayer = playerManager.getMediaPlayer();

            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            logger.e("Error initializing video player.");
            e.printStackTrace();
        }
    }

    @Override
    public void prepare(String url, Map<String, String> headers,
                        boolean loop, float speed, boolean isAudioOnly) {
        if (url == null || url.isEmpty()) {
            return;
        }

        Message msg = mainHandler.obtainMessage(MSG_PREPARE,
                new InitializationPlayerConfig(url, headers, loop, speed, isAudioOnly));
        mainHandler.sendMessage(msg);
    }

    @Override
    public IPlayerManager getPlayer() {
        return playerManager;
    }

    @Override
    public void start() {
        if (playerManager != null) {
            playerManager.start();
        }
    }

    @Override
    public void stop() {
        if (playerManager != null) {
            playerManager.stop();
        }
    }

    @Override
    public void pause() {
        if (playerManager != null) {
            playerManager.pause();
        }
    }

    @Override
    public int getVideoWidth() {
        if (playerManager != null) {
            return playerManager.getVideoWidth();
        }
        return 0;
    }

    @Override
    public int getVideoHeight() {
        if (playerManager != null) {
            return playerManager.getVideoHeight();
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        if (playerManager != null) {
            return playerManager.isPlaying();
        }
        return false;
    }

    @Override
    public void seekTo(long position) {
        if (playerManager != null) {
            playerManager.seekTo(position);
        }
    }

    @Override
    public long getCurrentPosition() {
        if (playerManager != null) {
            return playerManager.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (playerManager != null) {
            return playerManager.getDuration();
        }
        return -1;
    }

    @Override
    public int getVideoSarNum() {
        if (playerManager != null) {
            return playerManager.getVideoSarNum();
        }
        return 0;
    }

    @Override
    public int getVideoSarDen() {
        if (playerManager != null) {
            return playerManager.getVideoSarDen();
        }
        return 0;
    }

    @Override
    public void setAspectRatioMode(int aspectRatioMode) {
        currentAspectRatioMode = aspectRatioMode;
    }

    @Override
    public int getAspectRatioMode() {
        return currentAspectRatioMode;
    }

    @Override
    public void releaseMediaPlayer() {
        Message msg = new Message();
        msg.what = MSG_RELEASE;
        sendMessage(msg);
    }

    @Override
    public void setSelfVideoSizeFlag(boolean selfVideoSizeFlag) {
        this.selfVideoSizeFlag = selfVideoSizeFlag;
    }

    @Override
    public void setCurrentVideoSize(int videoWidth, int videoHeight) {
        this.currentVideoWidth = videoWidth;
        this.currentVideoHeight = videoHeight;
    }

    @Override
    public void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen) {
        this.currentVideoSarNum = videoSarNum;
        this.currentVideoSarDen = videoSarDen;
    }

    @Override
    public int getCurrentVideoWidth() {
        return currentVideoWidth;
    }

    @Override
    public int getCurrentVideoHeight() {
        return currentVideoHeight;
    }

    @Override
    public int getCurrentVideoSarNum() {
        return currentVideoSarNum;
    }

    @Override
    public int getCurrentVideoSarDen() {
        return currentVideoSarDen;
    }

    @Override
    public void setNeedMute(boolean needMute) {
        this.needMute = needMute;
        if (playerManager != null) {
            playerManager.setNeedMute(needMute);
        }
    }

    @Override
    public void setDisplay(Surface holder) {
        Message msg = mainHandler.obtainMessage(MSG_SET_DISPLAY, holder);
        mainHandler.sendMessage(msg);
    }

    @Override
    public void releaseSurface(Surface surface) {
        Message msg = mainHandler.obtainMessage(MSG_RELEASE_SURFACE, surface);
        mainHandler.sendMessage(msg);
    }

    @Override
    public MediaPlayerListener getMediaPlayerListener() {
        return mediaPlayerListenerRef != null ? mediaPlayerListenerRef.get() : null;
    }

    @Override
    public void setMediaPlayerListener(MediaPlayerListener listener) {
        mediaPlayerListenerRef = listener != null ? new WeakReference<>(listener) : null;
    }

    protected void sendMessage(Message message) {
        if (mainHandler != null) {
            mainHandler.sendMessage(message);
        }
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        if (getMediaPlayerListener() != null) {
            getMediaPlayerListener().onPrepared(mp);
        }
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {
        if (getMediaPlayerListener() != null) {
            getMediaPlayerListener().onBufferingUpdate(mp, percent);
        }
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        if (getMediaPlayerListener() != null) {
            getMediaPlayerListener().onCompletion(mp);
        }
    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        if (getMediaPlayerListener() != null) {
            getMediaPlayerListener().onError(mp, what, extra);
        }
        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        if (getMediaPlayerListener() != null) {
            getMediaPlayerListener().onInfo(mp, what, extra);
        }
        return true;
    }

    @Override
    public void onSeekComplete(IMediaPlayer mp) {
        if (getMediaPlayerListener() != null) {
            getMediaPlayerListener().onSeekComplete(mp);
        }
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
        if (selfVideoSizeFlag) {
            return;
        } else {
            currentVideoWidth = mp.getVideoWidth();
            currentVideoHeight = mp.getVideoHeight();
            currentVideoSarNum = mp.getVideoSarNum();
            currentVideoSarDen = mp.getVideoSarDen();
        }

        MediaPlayerListener listener = getMediaPlayerListener();
        if (listener != null) {
            listener.onVideoSizeChanged();
        }
    }
}
