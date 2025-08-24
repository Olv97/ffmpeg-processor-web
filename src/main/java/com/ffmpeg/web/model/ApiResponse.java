package com.ffmpeg.web.model;

// import lombok.Data;

/**
 * API响应统一封装
 */
// @Data
public class ApiResponse<T> {
    /**
     * 状态码，0表示成功
     */
    private int code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    /**
     * 获取状态码
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 设置状态码
     */
    public void setCode(int code) {
        this.code = code;
    }
    
    /**
     * 获取消息
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 设置消息
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * 获取数据
     */
    public T getData() {
        return data;
    }
    
    /**
     * 设置数据
     */
    public void setData(T data) {
        this.data = data;
    }
    
    /**
     * 创建成功响应
     * 
     * @param data 响应数据
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setMessage("成功");
        response.setData(data);
        return response;
    }
    
    /**
     * 创建错误响应
     * 
     * @param code 错误码
     * @param message 错误消息
     * @return 错误响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
