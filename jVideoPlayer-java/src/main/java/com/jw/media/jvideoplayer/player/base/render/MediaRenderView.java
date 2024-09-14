package com.jw.media.jvideoplayer.player.base.render;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.player.base.VideoType;

/**
 * Created by Joyce.wang on 2024/3/11 19:34
 *
 * @Description A view responsible for rendering media.
 *              播放绘制view
 */
public abstract class MediaRenderView extends FrameLayout implements IRenderView.IRenderCallback, VideoSizeChangeListener {
    private static Logger logger = LoggerFactory.getLogger(MediaRenderView.class.getName());
    //native绘制
    protected Surface mSurface;
    //渲染控件
    protected RenderView mRenderView;
    //渲染控件父类
    protected ViewGroup mRenderContainer;
    //画面选择角度
    protected int mRotate;
    public MediaRenderView(@NonNull Context context) {
        super(context);
    }

    public MediaRenderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaRenderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MediaRenderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 添加播放的view
     * 继承后重载addRenderView，继承RenderView后实现自己的RenderView类
     */
    protected void addRenderView() {
        mRenderView = new RenderView();
        mRenderView.addView(getContext(), mRenderContainer, VideoType.RENDER_SURFACE_VIEW, mRotate, this, this);
    }

    @Override
    public void onSurfaceCreated(Surface surface) {
        mSurface = surface;
        setDisplay(surface);

        onSurfaceCreatedEvent(surface);
    }

    @Override
    public void onSurfaceSizeChanged(Surface surface, int width, int height) {
        onSurfaceChangedEvent(surface, width, height);
    }

    @Override
    public boolean onSurfaceDestroyed(Surface surface) {
        //清空释放
        setDisplay(null);
        //同一消息队列中去release
        onSurfaceDestroyedEvent(surface);
        return true;
    }

    @Override
    public void onSurfaceUpdated(Surface surface) {

    }

    //设置播放
    protected abstract void setDisplay(Surface surface);
    protected abstract void onSurfaceCreatedEvent(Surface surface);
    protected abstract void onSurfaceChangedEvent(Surface surface, int width, int height);
    protected abstract void onSurfaceDestroyedEvent(Surface surface);
}
