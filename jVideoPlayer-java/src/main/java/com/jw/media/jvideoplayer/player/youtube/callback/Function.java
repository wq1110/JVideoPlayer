package com.jw.media.jvideoplayer.player.youtube.callback;

import java.util.Objects;

/**
 * Created by Joyce.wang on 2024/9/13 13:58
 *
 * @Description 接口式函数
 */
public interface Function<T, R> {
    R apply(T var1);

    default <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (v) -> {
            return this.apply(before.apply(v));
        };
    }

    default <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (t) -> {
            return after.apply(this.apply(t));
        };
    }

    static <T> Function<T, T> identity() {
        return (t) -> {
            return t;
        };
    }
}
