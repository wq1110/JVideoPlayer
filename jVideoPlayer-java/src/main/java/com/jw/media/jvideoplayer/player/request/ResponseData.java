package com.jw.media.jvideoplayer.player.request;

/**
 * Created by Joyce.wang on 2024/9/13 9:25
 *
 * @Description 接口返回具体结果
 * @param <T> 返回的实体类或集合
 */
public class ResponseData<T> extends ResponseStatus implements KeepClassMember {
    /**
     * 返回的数据
     */
    private T data;

    /**
     * 返回的分页信息
     */
    private Page page;

    /**
     * 用户的各种地区信息
     */
    private Region region;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Page getPage() {
        return page;
    }

    public Region getRegion() {
        return region;
    }
}