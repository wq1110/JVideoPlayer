package com.jw.media.jvideoplayer.player.base.render;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.player.base.VideoType;

/**
 * Created by Joyce.wang on 2024/3/11 17:32
 *
 * @Description Manages the rendering of video content.
 */
public class RenderView {
    private static Logger logger = LoggerFactory.getLogger(RenderView.class.getName());
    protected IRenderView renderView;

    public void requestLayout() {
        if (renderView != null) {
            renderView.getRenderView().requestLayout();
        }
    }

    /**
     * Gets the rotation of the underlying render view.
     *
     * @return The rotation in degrees.
     */
    public float getRotation() {
        return renderView != null ? renderView.getRenderView().getRotation() : 0f;
    }

    /**
     * Sets the rotation of the underlying render view.
     *
     * @param rotation The rotation in degrees.
     */
    public void setRotation(float rotation) {
        if (renderView != null)
            renderView.getRenderView().setRotation(rotation);
    }

    /**
     * Invalidates the underlying render view, causing it to redraw.
     */
    public void invalidate() {
        if (renderView != null) {
            renderView.getRenderView().invalidate();
        }
    }

    /**
     * Gets the underlying render view.
     *
     * @return The underlying render view, or null if not initialized.
     */
    public View getRenderView() {
        return renderView != null ? renderView.getRenderView() : null;
    }

    public boolean shouldWaitForResize() {
        return renderView == null || renderView.shouldWaitForResize();
    }

    /**
     * Gets the layout parameters of the underlying render view.
     *
     * @return The layout parameters.
     */
    public ViewGroup.LayoutParams getLayoutParams() {
        return renderView != null ? renderView.getRenderView().getLayoutParams() : null;
    }

    /**
     * Sets the layout parameters of the underlying render view.
     *
     * @param layoutParams The layout parameters to set.
     */
    public void setLayoutParams(ViewGroup.LayoutParams layoutParams) {
        if (renderView != null) {
            renderView.getRenderView().setLayoutParams(layoutParams);
        }
    }

    /**
     * Adds a render view to the specified container.
     *
     * @param context The application context.
     * @param renderViewContainer The container to add the render view to.
     * @param renderType The type of render view to create.
     * @param rotate The initial rotation of the render view.
     * @param iRenderCallback A callback for render events.
     * @param videoSizeChangeListener A listener for video size changes.
     */
    public void addView(final Context context, final ViewGroup renderViewContainer, final int renderType, final int rotate,
                        final IRenderView.IRenderCallback iRenderCallback,
                        final VideoSizeChangeListener videoSizeChangeListener) {
        if (renderType == VideoType.RENDER_SURFACE_VIEW) {
            renderView = SurfaceRenderView.addSurfaceView(context, renderViewContainer, rotate, iRenderCallback, videoSizeChangeListener);
        } else {
            renderView = TextureRenderView.addTextureView(context, renderViewContainer, rotate, iRenderCallback, videoSizeChangeListener);
        }
    }

    public static void addToParent(ViewGroup renderViewContainer, View render) {
        int params = getRenderViewParams();
        if (renderViewContainer instanceof RelativeLayout) {
            renderViewContainer.removeView(render);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(params, params);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            renderViewContainer.addView(render, 0, layoutParams);
        } else if (renderViewContainer instanceof FrameLayout) {
            renderViewContainer.removeView(render);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(params, params);
            layoutParams.gravity = Gravity.CENTER;
            renderViewContainer.addView(render, 0, layoutParams);
        }
    }

    /**
     * 获取布局参数
     * @return
     */
    public static int getRenderViewParams() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }
}
