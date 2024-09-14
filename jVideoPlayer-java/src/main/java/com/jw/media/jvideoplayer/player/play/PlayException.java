package com.jw.media.jvideoplayer.player.play;

/**
 * Created by Joyce.wang on 2024/9/13 10:39
 *
 * @Description TODO
 */
public class PlayException extends RuntimeException {
    public int code;
    public PlayException() {
        super();

    }
    public PlayException(int code) {
        super();
        this.code = code;
    }

    public PlayException(String message) {
        super(message);
    }

    public PlayException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}