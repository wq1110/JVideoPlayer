package com.jw.media.jvideoplayer.lib.ass;

import android.graphics.Bitmap;

import androidx.annotation.Keep;

import com.jw.media.jvideoplayer.lib.provider.ContextProvider;
import com.jw.media.jvideoplayer.lib.utils.Singleton;

@Keep
class AssBitmapPool {
    static Singleton<AssBitmapPool> singleton = new Singleton<AssBitmapPool>() {
        @Override
        protected AssBitmapPool create() {
            return new AssBitmapPool();
        }
    };

    LruBitmapPool lbp;

    public AssBitmapPool() {
        lbp = new LruBitmapPool((long) ContextProvider.getContext().getResources().getDisplayMetrics().widthPixels * ContextProvider.getContext().getResources().getDisplayMetrics().heightPixels * 4);
    }

    public static AssBitmapPool getInstance() {
        return singleton.get();
    }

    public static void recycle(Bitmap bitmap) {
        getInstance().lbp.put(bitmap);
    }


    public static Bitmap get(int width, int height, Bitmap.Config config) {
        return getInstance().lbp.get(width, height, config);
    }

    public static void clear() {
        getInstance().lbp.clearMemory();
    }

}
