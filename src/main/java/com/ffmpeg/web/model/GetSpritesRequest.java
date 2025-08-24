package com.ffmpeg.web.model;

import com.alibaba.fastjson.annotation.JSONField;

// 移除Lombok依赖，手动添加getter/setter
// import lombok.Data;
// import lombok.EqualsAndHashCode;

/**
 * 雪碧图生成请求
 */
// @Data
// @EqualsAndHashCode(callSuper = true)
public class GetSpritesRequest extends OutputRequest {
    /**
     * 雪碧图的 rows * cols
     */
    @JSONField(name = "tile")
    private String tile;
    
    /**
     * 视频开始时间点（秒）
     */
    @JSONField(name = "start")
    private Integer start = 0;
    
    /**
     * 基于 start 之后的多长时间的视频内进行截图
     */
    @JSONField(name = "duration")
    private Integer duration;
    
    /**
     * delay 多少秒
     */
    @JSONField(name = "itsoffset")
    private Integer itsoffset = 0;
    
    /**
     * 图片缩放比例
     */
    @JSONField(name = "scale")
    private String scale = "-1:-1";
    
    /**
     * 截图间隔时间（秒）
     */
    @JSONField(name = "interval")
    private Integer interval = 5;
    
    /**
     * 图片之间的间隔
     */
    @JSONField(name = "padding")
    private Integer padding = 1;
    
    /**
     * 雪碧图背景颜色
     */
    @JSONField(name = "color")
    private String color = "black";
    
    /**
     * 生成的雪碧图图片格式
     */
    @JSONField(name = "dst_type")
    private String dstType = "jpg";
    
    public String getTile() {
        return tile;
    }
    
    public void setTile(String tile) {
        this.tile = tile;
    }
    
    public Integer getStart() {
        return start;
    }
    
    public void setStart(Integer start) {
        this.start = start;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public Integer getItsoffset() {
        return itsoffset;
    }
    
    public void setItsoffset(Integer itsoffset) {
        this.itsoffset = itsoffset;
    }
    
    public String getScale() {
        return scale;
    }
    
    public void setScale(String scale) {
        this.scale = scale;
    }
    
    public Integer getInterval() {
        return interval;
    }
    
    public void setInterval(Integer interval) {
        this.interval = interval;
    }
    
    public Integer getPadding() {
        return padding;
    }
    
    public void setPadding(Integer padding) {
        this.padding = padding;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getDstType() {
        return dstType;
    }
    
    public void setDstType(String dstType) {
        this.dstType = dstType;
    }
}
