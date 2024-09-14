package com.jw.media.jvideoplayer.player.request;

/**
 * Created by Joyce.wang on 2024/9/13 9:26
 *
 * @Description 服务后端返回的地区信息
 */
public class Region extends AbstractPrintable {
    /**
     * 登录地区，用于与前次比较
     */
    private String loginRegion;

    /**
     * 地区变化
     */
    private String changeRegion;

    /**
     * 实际应该使用的地区
     */
    private String actualRegion;

    public Region() {
    }

    public Region(String loginRegion, String changeRegion, String actualRegion) {
        this.loginRegion = loginRegion;
        this.changeRegion = changeRegion;
        this.actualRegion = actualRegion;
    }

    public String getLoginRegion () {
        return loginRegion;
    }

    public void setLoginRegion (String loginRegion){
        this.loginRegion = loginRegion;
    }

    public String getChangeRegion () {
        return changeRegion;
    }

    public void setChangeRegion (String changeRegion){
        this.changeRegion = changeRegion;
    }

    public String getActualRegion () {
        return actualRegion;
    }

    public void setActualRegion (String actualRegion){
        this.actualRegion = actualRegion;
    }
}
