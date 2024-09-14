package com.jw.media.jvideoplayer.cache.socket.request;

/**
 * Created by Joyce.wang on 2024/9/11 8:58
 *
 * @Description TODO
 */
public enum Method {
    GET,
    PUT,
    POST,
    DELETE,
    HEAD,
    OPTIONS,
    TRACE,
    CONNECT,
    PATCH,
    PROPFIND,
    PROPPATCH,
    MKCOL,
    MOVE,
    COPY,
    LOCK,
    UNLOCK;

    public static Method lookup(String method) {
        if (method == null)
            return null;
        try {
            return valueOf(method);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
