package com.jw.media.jvideoplayer.player.base.render;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Joyce.wang on 2024/3/11 17:33
 *
 * @Description Represents a view responsible for rendering video content.
 */
public interface IRenderView {

    boolean shouldWaitForResize();

    /**
     * Sets the callback to receive rendering events.
     *
     * @param callback The callback to be notified of rendering events.
     */
    void setRenderCallback(IRenderCallback callback);

    /**
     * Retrieves the underlying view responsible for rendering.
     *
     * @return The rendering view.
     */
    View getRenderView();

    /**
     * Sets the listener to be notified of video size changes.
     *
     * @param listener The listener to receive video size change events.
     */
    void setVideoSizeChangeListener(VideoSizeChangeListener listener);

    interface ISurfaceHolder {
        void bindToMediaPlayer(IMediaPlayer mp);

        @NonNull
        IRenderView getRenderView();

        @Nullable
        SurfaceHolder getSurfaceHolder();

        @Nullable
        Surface openSurface();

        @Nullable
        SurfaceTexture getSurfaceTexture();
    }

    interface IRenderCallback {

        /**
         * Called when a surface is created.
         *
         * @param surface The newly created surface.
         */
        void onSurfaceCreated(Surface surface);

        /**
         * Called when the size of a surface changes.
         *
         * @param surface The surface whose size has changed.
         * @param width   The new width of the surface.
         * @param height  The new height of the surface.
         */
        void onSurfaceSizeChanged(Surface surface, int width, int height);

        /**
         * Called when a surface is about to be destroyed.
         *
         * @param surface The surface being destroyed.
         */
        boolean onSurfaceDestroyed(Surface surface);

        /**
         * Called when a surface's contents are updated.
         *
         * @param surface The surface whose contents have been updated.
         */
        void onSurfaceUpdated(Surface surface);
    }
}
