package com.jw.media.jvideoplayer.player.request;

import android.text.TextUtils;

/**
 * Created by Joyce.wang on 2024/9/13 9:21
 *
 * @Description TODO
 */
public class ResponseStatus extends AbstractPrintable {
    private static final int SUCCESS_CODE = 200;

    /**
     * 请求返回码
     */
    protected int code = -1;

    /**
     * 请求返回信息
     */
    protected String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return SUCCESS_CODE == code;
    }
}