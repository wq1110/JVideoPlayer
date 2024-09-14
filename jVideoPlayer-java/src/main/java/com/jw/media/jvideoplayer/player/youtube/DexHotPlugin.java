package com.jw.media.jvideoplayer.player.youtube;

import android.content.Context;
import java.io.File;
import dalvik.system.DexClassLoader;

/**
 * Created by Joyce.wang on 2024/9/13 14:11
 *
 * @Description TODO
 */
public class DexHotPlugin {
    private static ClassLoader sClassLoader;

    public DexHotPlugin() {
    }

    public static void init(Context context, String dexFilePath) {
        try {
            File dexFile = new File(dexFilePath);
            if (!dexFile.exists()) {
                return;
            }

            ClassLoader classLoader = new DexClassLoader(dexFilePath, context.getDir("dex", 0).getAbsolutePath(), (String)null, context.getClassLoader());
            sClassLoader = classLoader;
        } catch (Exception var4) {
        }

    }

    public static <T> T create(Class<T> exceptClass, String className) {
        if (sClassLoader == null) {
            return null;
        } else {
            try {
                Class newClass = sClassLoader.loadClass(className);
                T t = (T) newClass.newInstance();
                return t;
            } catch (Exception var4) {
                Exception e = var4;
                e.printStackTrace();
                return null;
            }
        }
    }
}
