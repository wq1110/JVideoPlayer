package com.jw.media.jvideoplayer.player.base.render;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.player.base.VideoType;
import com.jw.media.jvideoplayer.player.base.VideoMeasureHelper;

/**
 * Created by Joyce.wang on 2024/3/11 17:46
 *
 * @Description Renders video onto a SurfaceView.
 */
public class SurfaceRenderView extends SurfaceView implements SurfaceHolder.Callback2, IRenderView, VideoSizeChangeListener {
    private static Logger logger = LoggerFactory.getLogger(SurfaceRenderView.class.getName());
    private VideoMeasureHelper videoMeasureHelper;
    private VideoSizeChangeListener videoSizeChangeListener;
    private IRenderCallback iRenderCallback ;

    public SurfaceRenderView(Context context) {
        super(context);
        init();
    }

    public SurfaceRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SurfaceRenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SurfaceRenderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        videoMeasureHelper = new VideoMeasureHelper(this, this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        videoMeasureHelper.prepareMeasure(widthMeasureSpec, heightMeasureSpec, (int) getRotation());
        setMeasuredDimension(videoMeasureHelper.getMeasuredWidth(), videoMeasureHelper.getMeasuredHeight());
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
        if (videoSizeChangeListener != null) {
            return videoSizeChangeListener.getVideoSarNum();
        }
        return 0;
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
    public boolean shouldWaitForResize() {
        return true;
    }

    @Override
    public void setRenderCallback(IRenderCallback callback) {
        getHolder().addCallback(this);
        this.iRenderCallback = callback;
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
    public void surfaceRedrawNeeded(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (iRenderCallback != null) {
            iRenderCallback.onSurfaceCreated(surfaceHolder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (iRenderCallback != null) {
            iRenderCallback.onSurfaceSizeChanged(surfaceHolder.getSurface(), width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //清空释放
        if (iRenderCallback != null) {
            iRenderCallback.onSurfaceDestroyed(surfaceHolder.getSurface());
        }
    }

    /**
     * Creates and adds a SurfaceRenderView to the given parent.
     *
     * @param context The context.
     * @param renderViewContainer The parent ViewGroup.
     * @param rotate The rotation angle.
     * @param renderCallback The render callback.
     * @param videoSizeChangeListener The video size change listener.
     * @return The created SurfaceRenderView.
     */
    public static SurfaceRenderView addSurfaceView(Context context,
                                                   ViewGroup renderViewContainer,
                                                   final int rotate,
                                                   final IRenderView.IRenderCallback renderCallback,
                                                   final VideoSizeChangeListener videoSizeChangeListener) {
        SurfaceRenderView surfaceRenderView = new SurfaceRenderView(context);
        surfaceRenderView.setRenderCallback(renderCallback);
        surfaceRenderView.setVideoSizeChangeListener(videoSizeChangeListener);
        surfaceRenderView.setRotation(rotate);
        RenderView.addToParent(renderViewContainer, surfaceRenderView);
        return surfaceRenderView;
    }
}
