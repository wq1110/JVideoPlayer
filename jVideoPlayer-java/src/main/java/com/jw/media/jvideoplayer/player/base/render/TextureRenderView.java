package com.jw.media.jvideoplayer.player.base.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.player.base.VideoType;
import com.jw.media.jvideoplayer.player.base.VideoMeasureHelper;

/**
 * Created by Joyce.wang on 2024/3/11 18:02
 *
 * @Description Renders video onto a TextureView.
 */
public class TextureRenderView  extends TextureView implements TextureView.SurfaceTextureListener, IRenderView, VideoSizeChangeListener {
    private static Logger logger = LoggerFactory.getLogger(TextureRenderView.class.getName());
    private VideoMeasureHelper videoMeasureHelper;
    private VideoSizeChangeListener videoSizeChangeListener;
    private IRenderCallback renderCallback;
    private SurfaceTexture saveTexture;
    private Surface surface;
    private boolean usingMediaCodec = true;//是否使用硬解渲染
    public TextureRenderView(Context context) {
        super(context);
        init();
    }

    public TextureRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextureRenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TextureRenderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        videoMeasureHelper = new VideoMeasureHelper(this, this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        videoMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(videoMeasureHelper.getMeasuredWidth(), videoMeasureHelper.getMeasuredHeight());
    }

    @Override
    public boolean shouldWaitForResize() {
        return false;
    }

    @Override
    public void setRenderCallback(IRenderCallback callback) {
        setSurfaceTextureListener(this);
        this.renderCallback = callback;
    }

    @Override
    public View getRenderView() {
        return this;
    }

    @Override
    public void setVideoSizeChangeListener(VideoSizeChangeListener listener) {
        this.videoSizeChangeListener = listener;
    }

    @Override
    public int getCurrentVideoWidth() {
        return videoSizeChangeListener != null ? videoSizeChangeListener.getCurrentVideoWidth() : 0;
    }

    @Override
    public int getCurrentVideoHeight() {
        return videoSizeChangeListener != null ? videoSizeChangeListener.getCurrentVideoHeight() : 0;
    }

    @Override
    public int getVideoSarNum() {
        return videoSizeChangeListener != null ? videoSizeChangeListener.getVideoSarNum() : 0;
    }

    @Override
    public int getVideoSarDen() {
        return videoSizeChangeListener != null ? videoSizeChangeListener.getVideoSarDen() : 0;
    }

    @Override
    public int getAspectRatioMode() {
        return videoSizeChangeListener != null ? videoSizeChangeListener.getAspectRatioMode() : VideoType.AR_ASPECT_FIT_PARENT;
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        if (usingMediaCodec) {
            if (saveTexture == null) {
                saveTexture = surfaceTexture;
                surface = new Surface(surfaceTexture);
            } else {
                setSurfaceTexture(saveTexture);
            }
            if (renderCallback != null) {
                renderCallback.onSurfaceCreated(surface);
            }
        } else {
            surface = new Surface(surfaceTexture);
            if (renderCallback != null) {
                renderCallback.onSurfaceCreated(surface);
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        if (renderCallback != null) {
            renderCallback.onSurfaceSizeChanged(surface, width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        //清空释放
        if (renderCallback != null) {
            renderCallback.onSurfaceDestroyed(surface);
        }
        if (usingMediaCodec) {
            return (surfaceTexture == null);
        } else {
            return true;
        }
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        //如果播放的是暂停全屏了
        if (renderCallback != null) {
            renderCallback.onSurfaceUpdated(surface);
        }
    }

    /**
     * Creates and adds a TextureRenderView to the given ViewGroup.
     *
     * @param context                The context.
     * @param renderViewContainer    The ViewGroup to add the TextureRenderView to.
     * @param rotate                 The rotation angle for the TextureRenderView.
     * @param renderCallback         The IRenderCallback for the TextureRenderView.
     * @param videoSizeChangeListener The VideoSizeChangeListener for the TextureRenderView.
     * @return The created TextureRenderView.
     */
    public static TextureRenderView addTextureView(Context context, ViewGroup renderViewContainer, final int rotate,
                                                   final IRenderView.IRenderCallback renderCallback,
                                                   final VideoSizeChangeListener videoSizeChangeListener) {
        TextureRenderView mediaTextureView = new TextureRenderView(context);
        mediaTextureView.setRenderCallback(renderCallback);
        mediaTextureView.setVideoSizeChangeListener(videoSizeChangeListener);
        mediaTextureView.setRotation(rotate);
        RenderView.addToParent(renderViewContainer, mediaTextureView);
        return mediaTextureView;
    }
}
