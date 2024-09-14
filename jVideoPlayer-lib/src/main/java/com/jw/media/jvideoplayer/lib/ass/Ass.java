package com.jw.media.jvideoplayer.lib.ass;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.Keep;

import com.jw.media.jvideoplayer.lib.provider.ContextProvider;
import com.jw.media.jvideoplayer.lib.utils.FileUtils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Ass implements View.OnLayoutChangeListener {
    static {
        try {
            System.loadLibrary("assAndroid");
        } catch (Exception e) {
            e.toString();
        }
    }

    private static final String TAG = "libAss";
    private static final String FONTCONFIG_ENV_VARIABLE = "FONTCONFIG_PATH";
    private AssBuilder mBuilder;
    private AssTrack mTrack;
    private final IAssView mView;
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());
    private final Handler mWorkHandler;
    private AtomicBoolean isFontReady = new AtomicBoolean(false);
    private AtomicBoolean isSubtitleReady = new AtomicBoolean(false);

    private int[] rect = new int[2];

    private Ass(AssBuilder builder) {
        mBuilder = builder;
        mView = mBuilder.view;
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        mWorkHandler = new Handler(thread.getLooper());
    }

    public void prepare() {
        mWorkHandler.post(() -> {
            if (!isFontReady.get()) fontsWork();
            if (!isSubtitleReady.get()) subtitlesWork();
        });
    }

    public void destory() {
        if (mTrack != null) mTrack.destory();
        if (mView != null && mView instanceof View)
            ((View) mView).removeOnLayoutChangeListener(this);
        AssBitmapPool.clear();
    }

    private void fontsWork() {
        HashMap<String, String> fontNameMapping = new LinkedHashMap<>();
        final File fontsDataDir = new File(ContextProvider.getContext().getCacheDir(), TAG);
        FileUtils.deleteDirectory(fontsDataDir.getPath());
        fontsDataDir.mkdir();

        if (mBuilder.fonts.isEmpty()) {
            try {
                mBuilder.fonts.put("default", IOUtils.toByteArray(ContextProvider.getContext().getResources().getAssets().open("lib_ass_defaut_font.ttf")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (Map.Entry<String, byte[]> item : mBuilder.fonts.entrySet()) {
            fontNameMapping.put(item.getKey(), item.getKey());
            Utils.storeToCache(fontsDataDir, item.getValue(), item.getKey() + ".ttf"); //Todo
        }
        setFontDirectory(fontsDataDir, fontNameMapping);
        isFontReady.set(true);
    }

    private void subtitlesWork() {
        if (mView.getWidth() > 0 || mView.getHeight() > 0) {
            mTrack = convert(mBuilder.content, mBuilder.content.length, mBuilder.view.getWidth(), mBuilder.view.getHeight());
            if (mTrack == null) {
                Log.w(TAG, "Ass create track fail !!!! ");
                return;
            }
            isSubtitleReady.set(true);
            rect[0] = mView.getWidth();
            rect[1] = mView.getHeight();
            if (mView instanceof View)
                ((View) mView).addOnLayoutChangeListener(this);
        } else if (mView instanceof View) {
            ((View)mView).getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    ((View) mView).getViewTreeObserver().removeOnPreDrawListener(this);
                    mWorkHandler.post(Ass.this::subtitlesWork);
                    return false;
                }
            });
        } else {
            throw new RuntimeException("can not get valid resolution");
        }
    }

    public void onTimeStamp(long ms) {
        if (!isFontReady.get() || !isSubtitleReady.get()) {
            prepare();
            Log.w(TAG, "Ass not ready !!!! ");
            return;
        }

        mWorkHandler.post(() -> {
            AssImage[] images = getImage(mTrack, ms);
            mUiHandler.post(() -> {
                mView.render(images);
            });
        });
    }

    @Keep
    private static void tear(Object obj) {
        obj.toString();
    }


    private void setFontDirectory(final File fontDirectoryPath, final Map<String, String> fontNameMapping) {
        int validFontNameMappingCount = 0;

        final File fontConfigurationPath = new File(fontDirectoryPath, "fonts.conf");
        if (fontConfigurationPath.exists()) {
            boolean fontConfigurationDeleted = fontConfigurationPath.delete();
            Log.d(TAG, String.format("Deleted old temporary font configuration: %s.", fontConfigurationDeleted));
        }

        /* PROCESS MAPPINGS FIRST */
        final StringBuilder fontNameMappingBlock = new StringBuilder("");
        if (fontNameMapping != null && (fontNameMapping.size() > 0)) {
            fontNameMapping.entrySet();
            for (Map.Entry<String, String> mapping : fontNameMapping.entrySet()) {
                String fontName = mapping.getKey();
                String mappedFontName = mapping.getValue();

                if ((fontName != null) && (mappedFontName != null) && (fontName.trim().length() > 0) && (mappedFontName.trim().length() > 0)) {
                    fontNameMappingBlock.append("        <match target=\"pattern\">\n");
                    fontNameMappingBlock.append("                <test qual=\"any\" name=\"family\">\n");
                    fontNameMappingBlock.append(String.format("                        <string>%s</string>\n", fontName));
                    fontNameMappingBlock.append("                </test>\n");
                    fontNameMappingBlock.append("                <edit name=\"family\" mode=\"assign\" binding=\"same\">\n");
                    fontNameMappingBlock.append(String.format("                        <string>%s</string>\n", mappedFontName));
                    fontNameMappingBlock.append("                </edit>\n");
                    fontNameMappingBlock.append("        </match>\n");

                    validFontNameMappingCount++;
                }
            }
        }

        final String fontConfig = "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE fontconfig SYSTEM \"fonts.dtd\">\n" +
                "<fontconfig>\n" +
                "    <dir>.</dir>\n" +
                "    <dir>" + fontDirectoryPath + "</dir>\n" +
                fontNameMappingBlock +
                "</fontconfig>";

        final AtomicReference<FileOutputStream> reference = new AtomicReference<>();
        try {
            final FileOutputStream outputStream = new FileOutputStream(fontConfigurationPath);
            reference.set(outputStream);

            outputStream.write(fontConfig.getBytes());
            outputStream.flush();

            Log.d(TAG, String.format("Saved new temporary font configuration with %d font name mappings.", validFontNameMappingCount));

            setFontconfigConfigurationPath(fontDirectoryPath.getAbsolutePath());

            Log.d(TAG, String.format("Font directory %s registered successfully.", fontDirectoryPath));

        } catch (final IOException e) {
            Log.e(TAG, String.format("Failed to set font directory: %s.", fontDirectoryPath), e);
        } finally {
            if (reference.get() != null) {
                try {
                    reference.get().close();
                } catch (IOException e) {
                    // DO NOT PRINT THIS ERROR
                }
            }
        }
    }

    public static int setFontconfigConfigurationPath(final String path) {
        return setNativeEnvironmentVariable(FONTCONFIG_ENV_VARIABLE, path);
    }

    private native static int setNativeEnvironmentVariable(String variableName, String variableValue);

    private static native AssTrack convert(byte[] buffer, int length, int resolutionX, int resolutionY);

    private static native AssImage[] getImage(AssTrack track, long ms);

    private static native void addFont(String name, byte[] buffer, int length);

    private static native void setNativeDebug(boolean ref);

    public static void setDebug(boolean ref) {
        setNativeDebug(ref);
        AssBitmapPool.getInstance().lbp.isDebug = ref;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (rect[0] != mView.getWidth() || rect[1] != mView.getHeight()) {
            isSubtitleReady.set(false);
            if (mTrack != null) mTrack.destory();
            mTrack = null;
            prepare();
        }
    }

    public static class AssBuilder {
        private IAssView view;
        private byte[] content;
        private HashMap<String, byte[]> fonts = new LinkedHashMap<>();

        public AssBuilder setTarget(IAssView view) {
            this.view = view;
            return this;
        }

        public AssBuilder setAssContent(byte[] content) {
            this.content = content;
            return this;
        }

        public AssBuilder addTTF(byte[] font, String fontName) {
            fonts.put(fontName, font);
            return this;
        }

        public Ass build() {
            return new Ass(this);
        }
    }
}
