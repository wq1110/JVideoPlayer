package com.jw.media.jvideoplayer.player.youtube.callback;

/**
 * Created by Joyce.wang on 2024/9/13 13:57
 *
 * @Description 接口式函数
 */
public interface BiFunction<T, U, R> {
    R apply(T var1, U var2);

    default <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
        throw new RuntimeException("Stub!");
    }
}