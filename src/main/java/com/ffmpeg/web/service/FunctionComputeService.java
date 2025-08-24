package com.ffmpeg.web.service;

import com.alibaba.fastjson2.JSON;
import com.ffmpeg.web.config.AliyunConfig;
import com.ffmpeg.web.model.AudioConvertRequest;
import com.ffmpeg.web.model.BaseRequest;
import com.ffmpeg.web.model.GetSpritesRequest;
import com.ffmpeg.web.model.OutputRequest;
import com.ffmpeg.web.model.VideoGifRequest;
import com.ffmpeg.web.model.VideoWatermarkRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 阿里云函数计算服务
 */
@Service
public class FunctionComputeService {
    @Autowired
    private AliyunConfig aliyunConfig;
    
    private com.aliyun.teaopenapi.Client fcClient;

    // 函数名映射表
    private static final Map<String, String> functionMap = new HashMap<String, String>() {{
        put("getMediaMeta", "GetMediaMeta-82d0");
        put("getSprites", "GetSprites-82d0");
        put("videoToMp4", "video_to_mp4");
        put("videoToGif", "video_to_gif");
        put("audioConvert", "audio_convert");
        put("videoWatermark", "video_watermark");
        put("getDuration", "GetDuration-82d0");
    }};

    @PostConstruct
    public void init() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
        config.setAccessKeyId(aliyunConfig.getAccessKey());
        config.setAccessKeySecret(aliyunConfig.getSecretKey());
        config.setEndpoint(aliyunConfig.getFcEndpoint());
        fcClient = new com.aliyun.teaopenapi.Client(config);
        System.out.println("阿里云函数计算客户端初始化完成，区域：" + aliyunConfig.getRegion());
        System.out.println("使用端点: " + config.getEndpoint());
    }

    /**
     * 获取媒体文件元数据
     * @param request 请求参数
     * @return 媒体元数据
     */
    public String getMediaMeta(BaseRequest request) throws IOException {
        return invokeFunction("getMediaMeta", request);
    }
    
    /**
     * 视频转换为MP4格式
     * @param request 请求参数
     * @return 转换结果
     */
    public String videoToMp4(BaseRequest request) throws IOException {
        return invokeFunction("videoToMp4", request);
    }
    
    /**
     * 获取媒体时长
     * @param request 请求参数
     * @return 媒体时长信息，结构化JSON格式
     */
    public String getDuration(BaseRequest request) throws IOException {
        // 调用函数计算，获取原始结果
        String rawResult = invokeFunction("getDuration", request);
        System.out.println("获取媒体时长原始结果: " + rawResult);
        
        // 尝试查找标记
        String marker = "DURATION_RESULT:";
        int index = rawResult.indexOf(marker);
        if (index >= 0) {
            try {
                // 从日志中提取标记后的JSON
                String jsonContent = rawResult.substring(index + marker.length()).trim();
                int jsonStart = jsonContent.indexOf("{");
                int jsonEnd = jsonContent.lastIndexOf("}") + 1;
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    String jsonStr = jsonContent.substring(jsonStart, jsonEnd);
                    System.out.println("使用标记提取到JSON: " + jsonStr);
                    // 验证是否为有效JSON（只验证格式，不实际使用对象）
                    com.alibaba.fastjson2.JSON.parseObject(jsonStr); // 如果格式无效会抛出异常
                    return jsonStr; // 直接返回已经是JSON格式的字符串
                }
            } catch (Exception e) {
                System.out.println("从标记提取JSON失败: " + e.getMessage());
            }
        }
        
        try {
            // 尝试直接解析结果
            if (rawResult.trim().startsWith("{") && rawResult.trim().endsWith("}")) {
                // 看起来已经是JSON格式，验证一下
                com.alibaba.fastjson2.JSONObject jsonObj = com.alibaba.fastjson2.JSON.parseObject(rawResult);
                if (jsonObj.containsKey("duration")) {
                    // 已经是正确的JSON格式，直接返回
                    System.out.println("直接返回JSON格式结果");
                    return rawResult;
                }
            }
            
            // 尝试解析为数值
            double duration = 0;
            try {
                duration = Double.parseDouble(rawResult.trim());
            } catch (NumberFormatException nfe) {
                // 如果既不是JSON也不是纯数值，尝试进一步处理
                // 查找任何看起来像数字的内容
                String pattern = "[0-9]+\\.[0-9]+";
                java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = r.matcher(rawResult);
                if (m.find()) {
                    try {
                        duration = Double.parseDouble(m.group());
                    } catch (Exception ex) {
                        System.out.println("无法从正则匹配中提取数值");
                    }
                }
            }
            
            // 格式化时长
            String formattedDuration = formatDuration(duration);
            
            // 返回结构化JSON
            return String.format("{\"duration\":%.3f,\"formatted_duration\":\"%s\"}", 
                               duration, formattedDuration);
        } catch (Exception e) {
            System.out.println("解析时长数据失败: " + e.getMessage());
            e.printStackTrace();
            // 创建错误响应
            return String.format("{\"error\":\"解析失败\",\"message\":\"%s\"}", e.getMessage());
        }
    }
    
    /**
     * 格式化时长为小时:分钟:秒格式
     * @param durationSeconds 秒数
     * @return 格式化的时长字符串
     */
    private String formatDuration(double durationSeconds) {
        int totalSeconds = (int)Math.floor(durationSeconds);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        int milliseconds = (int)((durationSeconds - totalSeconds) * 1000);
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
        } else {
            return String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);
        }
    }
    
    /**
     * 生成视频雪碎图
     * @param request 请求参数
     * @return 雪碎图生成结果
     */
    public String getSprites(GetSpritesRequest request) throws IOException {
        return invokeFunction("getSprites", request);
    }
    
    /**
     * 视频转GIF动图
     * @param request 请求参数
     * @return 转换结果
     */
    public String videoGif(VideoGifRequest request) throws IOException {
        return invokeFunction("videoToGif", request);
    }
    
    /**
     * 音频格式转换
     * @param request 请求参数
     * @return 转换结果
     */
    public String audioConvert(AudioConvertRequest request) throws IOException {
        return invokeFunction("audioConvert", request);
    }
    
    /**
     * 视频添加水印
     * @param request 请求参数
     * @return 处理结果
     */
    public String videoWatermark(VideoWatermarkRequest request) throws IOException {
        return invokeFunction("videoWatermark", request);
    }

    /**
     * 调用函数计算服务
     * 
     * @param functionName 函数名
     * @param requestObj 请求参数
     * @return 函数执行结果
     */
    private String invokeFunction(String functionName, Object requestObj) throws IOException {
        // 获取函数名的映射
        String fcFunctionName = functionMap.getOrDefault(functionName, functionName);
        
        // 准备请求数据
        Map<String, Object> requestMap = new HashMap<>();
        if (requestObj instanceof BaseRequest) {
            BaseRequest request = (BaseRequest) requestObj;
            
            // 基本参数（所有请求都需要）
            requestMap.put("bucket_name", request.getBucketName());
            requestMap.put("object_key", request.getObjectKey());
            
            // 处理OutputRequest的参数
            if (requestObj instanceof OutputRequest) {
                OutputRequest outputRequest = (OutputRequest) requestObj;
                if (outputRequest.getOutputDir() != null) {
                    requestMap.put("output_dir", outputRequest.getOutputDir());
                }
            }
            
            // 处理GetSpritesRequest的特殊参数
            if (requestObj instanceof GetSpritesRequest) {
                GetSpritesRequest spritesRequest = (GetSpritesRequest) requestObj;
                
                // 云函数要求的tile参数，必需
                if (spritesRequest.getTile() != null) {
                    requestMap.put("tile", spritesRequest.getTile());
                }
                
                // 其他参数
                if (spritesRequest.getStart() != null) {
                    requestMap.put("start", spritesRequest.getStart());
                }
                if (spritesRequest.getDuration() != null) {
                    requestMap.put("duration", spritesRequest.getDuration());
                }
                if (spritesRequest.getItsoffset() != null) {
                    requestMap.put("itsoffset", spritesRequest.getItsoffset());
                }
                if (spritesRequest.getScale() != null) {
                    requestMap.put("scale", spritesRequest.getScale());
                }
                if (spritesRequest.getInterval() != null) {
                    requestMap.put("interval", spritesRequest.getInterval());
                }
                if (spritesRequest.getPadding() != null) {
                    requestMap.put("padding", spritesRequest.getPadding());
                }
                if (spritesRequest.getColor() != null) {
                    requestMap.put("color", spritesRequest.getColor());
                }
                if (spritesRequest.getDstType() != null) {
                    requestMap.put("dst_type", spritesRequest.getDstType());
                }
            }
        } else if (requestObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) requestObj;
            requestMap.putAll(map);
        } else {
            throw new IllegalArgumentException("不支持的请求对象类型");
        }
        
        // 序列化为JSON字符串
        String jsonBody = JSON.toJSONString(requestMap);
        System.out.println("原始函数名: " + functionName);
        System.out.println("映射后函数名: " + fcFunctionName);
        System.out.println("请求体JSON: " + jsonBody);
        
        try {
            // 参考阿里云官方示例，使用SDK调用函数计算
            // 1. 准备API参数
            com.aliyun.teaopenapi.models.Params params = new com.aliyun.teaopenapi.models.Params()
                .setAction("InvokeFunction")
                .setVersion("2023-03-30")
                .setProtocol("HTTPS")
                .setMethod("POST")
                .setAuthType("AK")
                .setStyle("FC")
                .setPathname("/2023-03-30/functions/" + fcFunctionName + "/invocations")
                .setReqBodyType("json")
                .setBodyType("binary"); // 恢复为binary类型，保证签名正确
                
            // 2. 准备查询参数
            Map<String, Object> queries = new HashMap<>();
            queries.put("qualifier", "LATEST");
            
            // 3. 修复请求体内容
            // 添加JSON格式化，确保参数正确传递
            System.out.println("请求体JSON的结构: " + JSON.parse(jsonBody));
            InputStream body = com.aliyun.darabonba.stream.Client.readFromString(jsonBody);
            
            // 4. 准备请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("x-fc-invocation-type", "Sync");
            headers.put("x-fc-log-type", "Tail"); // 保留Tail以便查看日志
            
            // 5. 准备运行时参数
            com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
            
            // 6. 构造完整请求
            com.aliyun.teaopenapi.models.OpenApiRequest request = new com.aliyun.teaopenapi.models.OpenApiRequest()
                .setHeaders(headers)
                .setQuery(com.aliyun.openapiutil.Client.query(queries))
                .setStream(body); // 恢复使用流作为请求体
                
            System.out.println("调用函数: " + fcFunctionName);
            
            try {
                // 7. 发送请求并获取响应
                Object resp = fcClient.callApi(params, request, runtime);
                String respString = com.aliyun.teautil.Common.toJSONString(resp);
                System.out.println("函数调用原始响应: " + respString);
                
                // 直接返回从函数计算返回的响应中提取出的body内容
                com.alibaba.fastjson2.JSONObject jsonResp = JSON.parseObject(respString);
                
                // 获取状态码
                int statusCode = jsonResp.getIntValue("statusCode");
                System.out.println("响应状态码: " + statusCode);
                
                if (statusCode >= 200 && statusCode < 300) {
                    // 检查是否有错误类型标识，即使状态码是200
                    if (jsonResp.containsKey("headers") && jsonResp.getJSONObject("headers").containsKey("x-fc-error-type")) {
                        String errorType = jsonResp.getJSONObject("headers").getString("x-fc-error-type");
                        System.out.println("函数错误类型: " + errorType);
                        
                        // 从日志中提取错误详细信息
                        if (jsonResp.getJSONObject("headers").containsKey("x-fc-log-result")) {
                            String logResult = jsonResp.getJSONObject("headers").getString("x-fc-log-result");
                            // 解码Base64日志
                            try {
                                byte[] decodedLogBytes = java.util.Base64.getDecoder().decode(logResult);
                                String decodedLog = new String(decodedLogBytes, StandardCharsets.UTF_8);
                                System.out.println("解码后的日志: " + decodedLog);
                                
                                // 从日志中提取错误信息JSON
                                if (decodedLog.contains("\"errorMessage\":")) {
                                    int errorStart = decodedLog.indexOf("{\"errorMessage\"");
                                    if (errorStart >= 0) {
                                        int errorEnd = decodedLog.indexOf("}", errorStart);
                                        while (decodedLog.indexOf("}", errorEnd + 1) > errorEnd && errorEnd < decodedLog.length()) {
                                            errorEnd = decodedLog.indexOf("}", errorEnd + 1);
                                        }
                                        errorEnd++;
                                        if (errorEnd > errorStart) {
                                            String errorJson = decodedLog.substring(errorStart, errorEnd);
                                            System.out.println("提取的错误信息: " + errorJson);
                                            return "{\"success\":false,\"error\":\"" + errorType + "\",\"details\":" + errorJson + "}";
                                        }
                                    }
                                }
                                return "{\"success\":false,\"error\":\"" + errorType + "\",\"message\":\"检查函数日志以获取详细信息\"}";
                            } catch (IllegalArgumentException e) {
                                System.err.println("日志解码失败: " + e.getMessage());
                                return "{\"success\":false,\"error\":\"" + errorType + "\",\"message\":\"无法解码日志\"}";
                            }
                        }
                        return "{\"success\":false,\"error\":\"" + errorType + "\",\"message\":\"未提供详细错误日志\"}";
                    }
                    
                    // 尝试从响应头中获取日志结果，可能包含输出
                    if (jsonResp.containsKey("headers") && jsonResp.getJSONObject("headers").containsKey("x-fc-log-result")) {
                        String logResult = jsonResp.getJSONObject("headers").getString("x-fc-log-result");
                        System.out.println("日志结果 (Base64): " + logResult);
                        
                        // 解码Base64日志
                        try {
                            byte[] decodedLogBytes = java.util.Base64.getDecoder().decode(logResult);
                            String decodedLog = new String(decodedLogBytes, StandardCharsets.UTF_8);
                            System.out.println("解码后的日志: " + decodedLog);
                            
                            // 从日志中精确查找媒体元数据标记
                            String marker = "MEDIA_METADATA:";
                            int index = decodedLog.indexOf(marker);
                            if (index >= 0) {
                                // 标记位置之后就是JSON开始位置
                                int startIndex = index + marker.length();
                                // 检查JSON是否是完整的
                                if (startIndex < decodedLog.length()) {
                                    // 确保开始位置不是空格
                                    while (startIndex < decodedLog.length() && 
                                           Character.isWhitespace(decodedLog.charAt(startIndex))) {
                                        startIndex++;
                                    }
                                    
                                    if (startIndex < decodedLog.length() && decodedLog.charAt(startIndex) == '{') {
                                        // 找到开始大括号，现在需要找到对应的结束大括号
                                        // 使用括号匹配算法
                                        int braceCount = 1;
                                        int position = startIndex + 1;
                                        
                                        while (position < decodedLog.length() && braceCount > 0) {
                                            char currentChar = decodedLog.charAt(position);
                                            if (currentChar == '{') {
                                                braceCount++;
                                            } else if (currentChar == '}') {
                                                braceCount--;
                                            }
                                            position++;
                                        }
                                        
                                        if (braceCount == 0) {
                                            // 成功找到匹配的结束括号
                                            String jsonContent = decodedLog.substring(startIndex, position);
                                            System.out.println("成功提取完整JSON: " + jsonContent);
                                            
                                            // 验证是合法的JSON
                                            try {
                                                com.alibaba.fastjson2.JSON.parseObject(jsonContent);
                                                return jsonContent;
                                            } catch (Exception e) {
                                                System.err.println("提取的JSON无法解析，错误: " + e.getMessage());
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // 如果没有找到MEDIA_METADATA标记，尝试其他标记
                            String[] otherMarkers = {"OUTPUT:", "RESULT:", "RETURN:"};
                            for (String otherMarker : otherMarkers) {
                                index = decodedLog.indexOf(otherMarker);
                                if (index >= 0) {
                                    int startIndex = index + otherMarker.length();
                                    while (startIndex < decodedLog.length() && 
                                           Character.isWhitespace(decodedLog.charAt(startIndex))) {
                                        startIndex++;
                                    }
                                    
                                    if (startIndex < decodedLog.length() && decodedLog.charAt(startIndex) == '{') {
                                        int braceCount = 1;
                                        int position = startIndex + 1;
                                        
                                        while (position < decodedLog.length() && braceCount > 0) {
                                            char currentChar = decodedLog.charAt(position);
                                            if (currentChar == '{') {
                                                braceCount++;
                                            } else if (currentChar == '}') {
                                                braceCount--;
                                            }
                                            position++;
                                        }
                                        
                                        if (braceCount == 0) {
                                            String jsonContent = decodedLog.substring(startIndex, position);
                                            System.out.println("使用标记 '" + otherMarker + "' 提取到JSON: " + jsonContent);
                                            try {
                                                com.alibaba.fastjson2.JSON.parseObject(jsonContent);
                                                return jsonContent;
                                            } catch (Exception e) {
                                                System.err.println("提取的JSON无法解析，尝试其他方法");
                                            }
                                        }
                                    }
                                }
                            }

                            // 如果没有找到标记，尝试在日志中查找JSON形式内容
                            String[] logLines = decodedLog.split("\n");
                            for (int i = logLines.length - 1; i >= 0; i--) {
                                String line = logLines[i].trim();
                                if (line.startsWith("{") && line.endsWith("}")) {
                                    System.out.println("从日志提取的JSON结果: " + line);
                                    return line;
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("解析日志结果失败: " + e.getMessage());
                        }
                    }
                    
                    // 尝试解码响应体
                    if (jsonResp.containsKey("body")) {
                        Object bodyObj = jsonResp.get("body");
                        String bodyJson;
                        
                        if (bodyObj instanceof com.alibaba.fastjson2.JSONObject) {
                            // 如果body是JSON对象，直接转成字符串
                            bodyJson = ((com.alibaba.fastjson2.JSONObject) bodyObj).toString();
                        } else if (bodyObj instanceof String) {
                            // 如果是字符串，可能是Base64编码，尝试解码
                            String bodyStr = (String) bodyObj;
                            try {
                                byte[] decodedBytes = java.util.Base64.getDecoder().decode(bodyStr);
                                bodyJson = new String(decodedBytes, StandardCharsets.UTF_8);
                                System.out.println("Base64解码后的响应体: " + bodyJson);
                            } catch (IllegalArgumentException e) {
                                // 如果不是Base64编码，直接使用
                                bodyJson = bodyStr;
                            }
                        } else {
                            // 其他情况，尝试直接序列化
                            bodyJson = JSON.toJSONString(bodyObj);
                        }
                        
                        System.out.println("处理后的响应体: " + bodyJson);
                        if (bodyJson.equals("{}") || bodyJson.isEmpty()) {
                            // 如果body是空对象，可能实际内容在bodyBytes中
                            if (jsonResp.containsKey("bodyBytes")) {
                                Object bytesObj = jsonResp.get("bodyBytes");
                                if (bytesObj != null) {
                                    try {
                                        if (bytesObj instanceof byte[]) {
                                            String bytesContent = new String((byte[]) bytesObj, StandardCharsets.UTF_8);
                                            System.out.println("从bodyBytes解析的内容: " + bytesContent);
                                            return bytesContent;
                                        }
                                    } catch (Exception e) {
                                        System.err.println("处理bodyBytes异常: " + e.getMessage());
                                    }
                                }
                            }
                            
                            // 如果上述尝试都失败，返回原始响应
                            bodyJson = respString;
                            System.out.println("由于无法解析响应体，返回完整响应: " + bodyJson);
                        }
                        return bodyJson;
                    } else {
                        // 如果没有body字段，返回整个响应
                        System.out.println("响应中没有body字段，返回完整响应");
                        return respString;
                    }
                } else {
                    throw new IOException("函数调用失败，状态码: " + statusCode);
                }
            } catch (Exception e) {
                System.err.println("处理函数响应时发生异常: " + e.getMessage());
                e.printStackTrace();
                // 继续执行下面的代码，尝试使用原始方式获取响应
            }
            
            // 如果上面的方式失败，尝试使用原始的Map方式处理
            // 重新创建请求体，避免使用已关闭的流
            InputStream newBody = com.aliyun.darabonba.stream.Client.readFromString(jsonBody);
            request.setStream(newBody);
            
            Object resp = fcClient.callApi(params, request, runtime);
            System.out.println("重试获取响应");
            String respString = com.aliyun.teautil.Common.toJSONString(resp);
            System.out.println("重试原始响应: " + respString);
            
            // 备用处理方式
            if (resp instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> respMap = (Map<String, Object>) resp;
                
                // 获取状态码
                Object statusCode = respMap.get("statusCode");
                System.out.println("响应状态码(备用方式): " + statusCode);
                
                // 尝试提取响应体
                Object bodyObj = respMap.get("body");
                if (bodyObj != null) {
                    String bodyContent;
                    if (bodyObj instanceof String) {
                        bodyContent = (String) bodyObj;
                    } else {
                        bodyContent = com.aliyun.teautil.Common.toJSONString(bodyObj);
                    }
                    System.out.println("备用方式处理后的响应体: " + bodyContent);
                    return bodyContent;
                } else if (respMap.containsKey("bodyBytes")) {
                    // 如果有bodyBytes，尝试将其转换为字符串
                    try {
                        Object bytesObj = respMap.get("bodyBytes");
                        if (bytesObj instanceof byte[]) {
                            String bodyStr = new String((byte[]) bytesObj, StandardCharsets.UTF_8);
                            System.out.println("从bodyBytes提取的响应: " + bodyStr);
                            return bodyStr;
                        }
                    } catch (Exception e) {
                        System.err.println("从bodyBytes提取响应失败: " + e.getMessage());
                    }
                }
                
                // 如果无法提取响应体，返回整个响应对象的JSON表示
                return respString;
            } else {
                return com.aliyun.teautil.Common.toJSONString(resp);
            }
            
        } catch (Exception e) {
            System.err.println("调用函数失败: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("调用函数失败", e);
        }
    }
}