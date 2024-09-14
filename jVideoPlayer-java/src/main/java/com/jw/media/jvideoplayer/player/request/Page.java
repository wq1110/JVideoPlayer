package com.jw.media.jvideoplayer.player.request;

/**
 * Created by Joyce.wang on 2024/9/13 9:26
 *
 * @Description 网络接口的分页信息
 */
public class Page extends AbstractPrintable {
    /**
     * 分页id
     */
    private Integer pageId;
    /**
     * 分页大小
     */
    private Integer pageSize;
    /**
     * 总数量
     */
    private Long count;
    /**
     * 总页数
     */
    private Integer pages;
    /**
     * 是否有下一页
     */
    private boolean hasNext;

    public Page() {
    }

    public Page(Integer pageId, Integer pageSize) {
        this.pageId = pageId;
        this.pageSize = pageSize;
    }

    public Integer getPageId() {
        return pageId;
    }

    public void setPageId(Integer pageId) {
        this.pageId = pageId;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
}
