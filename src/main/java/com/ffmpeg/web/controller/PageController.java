package com.ffmpeg.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面控制器，处理页面请求
 */
@Controller
public class PageController {
    
    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    /**
     * 首页别名
     */
    @GetMapping("/index")
    public String indexAlias() {
        return "index";
    }
}
