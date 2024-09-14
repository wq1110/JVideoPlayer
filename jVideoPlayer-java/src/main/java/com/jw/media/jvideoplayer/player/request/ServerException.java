package com.jw.media.jvideoplayer.player.request;

/**
 * Created by Joyce.wang on 2024/9/13 9:29
 *
 * @Description TODO
 */
public class ServerException extends RuntimeException {
    private int httpCode;
    ResponseStatus responseStatus;

    public ServerException() {
    }

    public ServerException(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public ServerException(int httpCode, ResponseStatus responseStatus) {
        this.httpCode = httpCode;
        this.responseStatus = responseStatus;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }
}