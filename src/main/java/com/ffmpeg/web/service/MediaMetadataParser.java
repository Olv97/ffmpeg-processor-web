package com.ffmpeg.web.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ffmpeg.web.model.MediaMetadata;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * 媒体元数据解析服务
 * 将FFprobe返回的JSON数据转换为结构化的MediaMetadata对象
 */
@Service
public class MediaMetadataParser {

    /**
     * 解析媒体元数据
     * @param jsonMetadata FFprobe返回的JSON格式元数据
     * @return 结构化的媒体元数据对象
     */
    public MediaMetadata parseMetadata(String jsonMetadata) {
        JSONObject metadata = JSON.parseObject(jsonMetadata);
        MediaMetadata result = new MediaMetadata();
        
        // 解析格式信息
        if (metadata.containsKey("format")) {
            JSONObject format = metadata.getJSONObject("format");
            
            // 基本信息
            String filename = format.getString("filename");
            // 提取文件名（去除路径和参数）
            if (filename != null) {
                int lastSlash = filename.lastIndexOf('/');
                int queryStart = filename.indexOf('?');
                if (lastSlash >= 0 && queryStart > lastSlash) {
                    result.setFileName(filename.substring(lastSlash + 1, queryStart));
                } else if (lastSlash >= 0) {
                    result.setFileName(filename.substring(lastSlash + 1));
                } else if (queryStart > 0) {
                    result.setFileName(filename.substring(0, queryStart));
                } else {
                    result.setFileName(filename);
                }
            }
            
            result.setFormat(format.getString("format_long_name"));
            
            // 设置流计数
            Integer streamCount = format.getInteger("nb_streams");
            if (streamCount != null) {
                result.setStreamCount(streamCount);
            }
            
            // 格式化时长（转换为分:秒）
            String durationStr = format.getString("duration");
            if (durationStr != null) {
                double duration = Double.parseDouble(durationStr);
                int minutes = (int) (duration / 60);
                int seconds = (int) (duration % 60);
                result.setDuration(String.format("%d:%02d", minutes, seconds));
            }
            
            // 格式化文件大小（转换为MB或KB）
            String sizeStr = format.getString("size");
            if (sizeStr != null) {
                long size = Long.parseLong(sizeStr);
                if (size > 1024 * 1024) {
                    result.setSize(String.format("%.2f MB", size / (1024.0 * 1024.0)));
                } else if (size > 1024) {
                    result.setSize(String.format("%.2f KB", size / 1024.0));
                } else {
                    result.setSize(size + " bytes");
                }
            }
            
            // 格式化比特率（转换为Mbps或Kbps）
            String bitRateStr = format.getString("bit_rate");
            if (bitRateStr != null) {
                long bitRate = Long.parseLong(bitRateStr);
                if (bitRate > 1000000) {
                    result.setBitRate(String.format("%.2f Mbps", bitRate / 1000000.0));
                } else if (bitRate > 1000) {
                    result.setBitRate(String.format("%.2f Kbps", bitRate / 1000.0));
                } else {
                    result.setBitRate(bitRate + " bps");
                }
            }
            
            // 提取标签信息
            if (format.containsKey("tags")) {
                JSONObject tags = format.getJSONObject("tags");
                for (String key : tags.keySet()) {
                    String value = tags.getString(key);
                    result.getTags().put(key, value);
                }
            }
        }
        
        // 解析流信息
        if (metadata.containsKey("streams")) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            List<Map<String, Object>> streams = (List) metadata.getJSONArray("streams").toJavaList(Map.class);
            
            for (Map<String, Object> stream : streams) {
                String codecType = (String) stream.get("codec_type");
                
                if ("video".equalsIgnoreCase(codecType)) {
                    // 解析视频流
                    MediaMetadata.VideoStream videoStream = new MediaMetadata.VideoStream();
                    videoStream.setIndex(getIntValue(stream, "index"));
                    videoStream.setCodecName(getStringValue(stream, "codec_name"));
                    videoStream.setCodecType("视频");
                    videoStream.setWidth(getIntValue(stream, "width"));
                    videoStream.setHeight(getIntValue(stream, "height"));
                    
                    // 解析帧率
                    String frameRate = getStringValue(stream, "r_frame_rate");
                    if (frameRate != null && frameRate.contains("/")) {
                        String[] parts = frameRate.split("/");
                        if (parts.length == 2) {
                            try {
                                double rate = Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
                                videoStream.setFrameRate(new DecimalFormat("#.##").format(rate) + " fps");
                            } catch (NumberFormatException e) {
                                videoStream.setFrameRate(frameRate);
                            }
                        } else {
                            videoStream.setFrameRate(frameRate);
                        }
                    }
                    
                    // 解析比特率
                    String bitRate = getStringValue(stream, "bit_rate");
                    if (bitRate != null) {
                        try {
                            long bitRateValue = Long.parseLong(bitRate);
                            if (bitRateValue > 1000000) {
                                videoStream.setBitRate(String.format("%.2f Mbps", bitRateValue / 1000000.0));
                            } else if (bitRateValue > 1000) {
                                videoStream.setBitRate(String.format("%.2f Kbps", bitRateValue / 1000.0));
                            } else {
                                videoStream.setBitRate(bitRateValue + " bps");
                            }
                        } catch (NumberFormatException e) {
                            videoStream.setBitRate(bitRate);
                        }
                    }
                    
                    videoStream.setPixelFormat(getStringValue(stream, "pix_fmt"));
                    result.getVideoStreams().add(videoStream);
                    
                } else if ("audio".equalsIgnoreCase(codecType)) {
                    // 解析音频流
                    MediaMetadata.AudioStream audioStream = new MediaMetadata.AudioStream();
                    audioStream.setIndex(getIntValue(stream, "index"));
                    audioStream.setCodecName(getStringValue(stream, "codec_name"));
                    audioStream.setCodecType("音频");
                    audioStream.setChannels(getIntValue(stream, "channels"));
                    audioStream.setChannelLayout(getStringValue(stream, "channel_layout"));
                    
                    // 添加采样率单位
                    String sampleRate = getStringValue(stream, "sample_rate");
                    if (sampleRate != null) {
                        audioStream.setSampleRate(sampleRate + " Hz");
                    }
                    
                    // 解析比特率
                    String bitRate = getStringValue(stream, "bit_rate");
                    if (bitRate != null) {
                        try {
                            long bitRateValue = Long.parseLong(bitRate);
                            if (bitRateValue > 1000000) {
                                audioStream.setBitRate(String.format("%.2f Mbps", bitRateValue / 1000000.0));
                            } else if (bitRateValue > 1000) {
                                audioStream.setBitRate(String.format("%.2f Kbps", bitRateValue / 1000.0));
                            } else {
                                audioStream.setBitRate(bitRateValue + " bps");
                            }
                        } catch (NumberFormatException e) {
                            audioStream.setBitRate(bitRate);
                        }
                    }
                    
                    result.getAudioStreams().add(audioStream);
                }
            }
        }
        
        // 测试数据为空的情况下，尝试从原始JSON中提取更多信息
        if (result.getVideoStreams().isEmpty() && result.getAudioStreams().isEmpty() && result.getTags().isEmpty()) {
            try {
                // 如果没有结构化数据，但原始JSON有效，尝试提取更多原始信息
                extractRawMetadata(metadata, result);
            } catch (Exception e) {
                // 忽略提取错误，保持已有数据
                System.err.println("Failed to extract additional raw metadata: " + e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * 从原始JSON中提取更多元数据
     * 当标准解析无法获取到完整信息时使用
     */
    private void extractRawMetadata(JSONObject metadata, MediaMetadata result) {
        // 提取标签
        if (metadata.containsKey("format") && metadata.getJSONObject("format").containsKey("tags")) {
            JSONObject tags = metadata.getJSONObject("format").getJSONObject("tags");
            for (String key : tags.keySet()) {
                result.getTags().put(key, tags.getString(key));
            }
        }
        
        // 如果没有从格式化结构获取到流信息，直接从原始JSON提取
        if (result.getVideoStreams().isEmpty() && result.getAudioStreams().isEmpty() && metadata.containsKey("streams")) {
            com.alibaba.fastjson2.JSONArray streams = metadata.getJSONArray("streams");
            for (int i = 0; i < streams.size(); i++) {
                JSONObject streamObj = streams.getJSONObject(i);
                String codecType = streamObj.getString("codec_type");
                
                if ("video".equalsIgnoreCase(codecType)) {
                    MediaMetadata.VideoStream videoStream = new MediaMetadata.VideoStream();
                    videoStream.setIndex(streamObj.getIntValue("index", i));
                    videoStream.setCodecName(streamObj.getString("codec_name"));
                    videoStream.setCodecType("视频");
                    videoStream.setWidth(streamObj.getIntValue("width", 0));
                    videoStream.setHeight(streamObj.getIntValue("height", 0));
                    
                    // 帧率
                    String frameRate = streamObj.getString("r_frame_rate");
                    if (frameRate != null && frameRate.contains("/")) {
                        try {
                            String[] parts = frameRate.split("/");
                            double rate = Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
                            videoStream.setFrameRate(String.format("%.2f fps", rate));
                        } catch (Exception e) {
                            videoStream.setFrameRate(frameRate);
                        }
                    }
                    
                    // 比特率
                    if (streamObj.containsKey("bit_rate")) {
                        try {
                            long bitRate = streamObj.getLong("bit_rate");
                            if (bitRate > 1000000) {
                                videoStream.setBitRate(String.format("%.2f Mbps", bitRate / 1000000.0));
                            } else {
                                videoStream.setBitRate(String.format("%.2f Kbps", bitRate / 1000.0));
                            }
                        } catch (Exception e) {
                            videoStream.setBitRate(streamObj.getString("bit_rate"));
                        }
                    }
                    
                    videoStream.setPixelFormat(streamObj.getString("pix_fmt"));
                    result.getVideoStreams().add(videoStream);
                    
                } else if ("audio".equalsIgnoreCase(codecType)) {
                    MediaMetadata.AudioStream audioStream = new MediaMetadata.AudioStream();
                    audioStream.setIndex(streamObj.getIntValue("index", i));
                    audioStream.setCodecName(streamObj.getString("codec_name"));
                    audioStream.setCodecType("音频");
                    audioStream.setChannels(streamObj.getIntValue("channels", 0));
                    audioStream.setChannelLayout(streamObj.getString("channel_layout"));
                    audioStream.setSampleRate(streamObj.getString("sample_rate") + " Hz");
                    
                    // 比特率
                    if (streamObj.containsKey("bit_rate")) {
                        try {
                            long bitRate = streamObj.getLong("bit_rate");
                            if (bitRate > 1000000) {
                                audioStream.setBitRate(String.format("%.2f Mbps", bitRate / 1000000.0));
                            } else {
                                audioStream.setBitRate(String.format("%.2f Kbps", bitRate / 1000.0));
                            }
                        } catch (Exception e) {
                            audioStream.setBitRate(streamObj.getString("bit_rate"));
                        }
                    }
                    
                    result.getAudioStreams().add(audioStream);
                }
            }
        }
        
        // 如果原始JSON中有更多格式信息，补充出来
        if (metadata.containsKey("format")) {
            JSONObject format = metadata.getJSONObject("format");
            
            if (result.getFormat() == null || result.getFormat().trim().isEmpty()) {
                result.setFormat(format.getString("format_long_name"));
            }
            
            if (result.getDuration() == null || result.getDuration().trim().isEmpty()) {
                String durationStr = format.getString("duration");
                if (durationStr != null) {
                    try {
                        double duration = Double.parseDouble(durationStr);
                        int minutes = (int) (duration / 60);
                        int seconds = (int) (duration % 60);
                        result.setDuration(String.format("%d:%02d", minutes, seconds));
                    } catch (NumberFormatException e) {
                        result.setDuration(durationStr);
                    }
                }
            }
            
            if (result.getSize() == null || result.getSize().trim().isEmpty()) {
                String sizeStr = format.getString("size");
                if (sizeStr != null) {
                    try {
                        long size = Long.parseLong(sizeStr);
                        if (size > 1024 * 1024) {
                            result.setSize(String.format("%.2f MB", size / (1024.0 * 1024.0)));
                        } else if (size > 1024) {
                            result.setSize(String.format("%.2f KB", size / 1024.0));
                        } else {
                            result.setSize(size + " bytes");
                        }
                    } catch (NumberFormatException e) {
                        result.setSize(sizeStr);
                    }
                }
            }
            
            if (result.getBitRate() == null || result.getBitRate().trim().isEmpty()) {
                String bitRateStr = format.getString("bit_rate");
                if (bitRateStr != null) {
                    try {
                        long bitRate = Long.parseLong(bitRateStr);
                        if (bitRate > 1000000) {
                            result.setBitRate(String.format("%.2f Mbps", bitRate / 1000000.0));
                        } else if (bitRate > 1000) {
                            result.setBitRate(String.format("%.2f Kbps", bitRate / 1000.0));
                        } else {
                            result.setBitRate(bitRate + " bps");
                        }
                    } catch (NumberFormatException e) {
                        result.setBitRate(bitRateStr);
                    }
                }
            }
        }
    }
    
    /**
     * 安全获取整型值
     */
    private int getIntValue(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            Object value = map.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }
    
    /**
     * 安全获取字符串值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            Object value = map.get(key);
            return value != null ? value.toString() : null;
        }
        return null;
    }
}
