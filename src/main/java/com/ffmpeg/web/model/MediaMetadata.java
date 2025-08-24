package com.ffmpeg.web.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 媒体元数据结构化模型，用于前端表格显示
 */
public class MediaMetadata {
    
    // 基本信息
    private String fileName;
    private String format;
    private String duration;
    private String size;
    private String bitRate;
    private int streamCount;
    
    // 视频流信息
    private List<VideoStream> videoStreams = new ArrayList<>();
    
    // 音频流信息
    private List<AudioStream> audioStreams = new ArrayList<>();
    
    // 标签信息（如创建时间、编码器等）
    private Map<String, String> tags = new HashMap<>();
    
    /**
     * 视频流信息
     */
    public static class VideoStream {
        private int index;
        private String codecName;
        private String codecType;
        private int width;
        private int height;
        private String frameRate;
        private String bitRate;
        private String pixelFormat;
        
        public int getIndex() {
            return index;
        }
        
        public void setIndex(int index) {
            this.index = index;
        }
        
        public String getCodecName() {
            return codecName;
        }
        
        public void setCodecName(String codecName) {
            this.codecName = codecName;
        }
        
        public String getCodecType() {
            return codecType;
        }
        
        public void setCodecType(String codecType) {
            this.codecType = codecType;
        }
        
        public int getWidth() {
            return width;
        }
        
        public void setWidth(int width) {
            this.width = width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public void setHeight(int height) {
            this.height = height;
        }
        
        public String getFrameRate() {
            return frameRate;
        }
        
        public void setFrameRate(String frameRate) {
            this.frameRate = frameRate;
        }
        
        public String getBitRate() {
            return bitRate;
        }
        
        public void setBitRate(String bitRate) {
            this.bitRate = bitRate;
        }
        
        public String getPixelFormat() {
            return pixelFormat;
        }
        
        public void setPixelFormat(String pixelFormat) {
            this.pixelFormat = pixelFormat;
        }
    }
    
    /**
     * 音频流信息
     */
    public static class AudioStream {
        private int index;
        private String codecName;
        private String codecType;
        private int channels;
        private String channelLayout;
        private String sampleRate;
        private String bitRate;
        
        public int getIndex() {
            return index;
        }
        
        public void setIndex(int index) {
            this.index = index;
        }
        
        public String getCodecName() {
            return codecName;
        }
        
        public void setCodecName(String codecName) {
            this.codecName = codecName;
        }
        
        public String getCodecType() {
            return codecType;
        }
        
        public void setCodecType(String codecType) {
            this.codecType = codecType;
        }
        
        public int getChannels() {
            return channels;
        }
        
        public void setChannels(int channels) {
            this.channels = channels;
        }
        
        public String getChannelLayout() {
            return channelLayout;
        }
        
        public void setChannelLayout(String channelLayout) {
            this.channelLayout = channelLayout;
        }
        
        public String getSampleRate() {
            return sampleRate;
        }
        
        public void setSampleRate(String sampleRate) {
            this.sampleRate = sampleRate;
        }
        
        public String getBitRate() {
            return bitRate;
        }
        
        public void setBitRate(String bitRate) {
            this.bitRate = bitRate;
        }
    }
    
    // Getters and Setters
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public String getSize() {
        return size;
    }
    
    public void setSize(String size) {
        this.size = size;
    }
    
    public String getBitRate() {
        return bitRate;
    }
    
    public void setBitRate(String bitRate) {
        this.bitRate = bitRate;
    }
    
    public List<VideoStream> getVideoStreams() {
        return videoStreams;
    }
    
    public void setVideoStreams(List<VideoStream> videoStreams) {
        this.videoStreams = videoStreams;
    }
    
    public List<AudioStream> getAudioStreams() {
        return audioStreams;
    }
    
    public void setAudioStreams(List<AudioStream> audioStreams) {
        this.audioStreams = audioStreams;
    }
    
    public int getStreamCount() {
        return streamCount;
    }
    
    public void setStreamCount(int streamCount) {
        this.streamCount = streamCount;
    }
    
    public Map<String, String> getTags() {
        return tags;
    }
    
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
