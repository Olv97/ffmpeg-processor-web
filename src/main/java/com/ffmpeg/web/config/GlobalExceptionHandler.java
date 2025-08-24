package com.ffmpeg.web.config;

import com.ffmpeg.web.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理一般异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<String> handleException(Exception ex, HttpServletRequest request) {
        logger.error("请求处理出现异常: {}, 请求地址: {}", ex.getMessage(), request.getRequestURI(), ex);
        return ApiResponse.error(500, "服务器内部错误: " + ex.getMessage());
    }
    
    /**
     * 处理参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        logger.error("请求参数错误: {}, 请求地址: {}", ex.getMessage(), request.getRequestURI(), ex);
        return ApiResponse.error(400, "请求参数错误: " + ex.getMessage());
    }
}
