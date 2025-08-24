/**
 * 增强调试脚本 - 用于检测页面加载和模态框问题
 */
window.addEventListener('DOMContentLoaded', function() {
    console.log("===== 页面DOM已加载 =====");
    
    // 显示环境信息
    console.log("当前路径:", window.location.href);
    console.log("页面上下文路径:", window.location.pathname);
    
    // 检查jQuery是否加载
    if (typeof jQuery !== 'undefined') {
        console.log("jQuery版本:", jQuery.fn.jquery);
    } else {
        console.error("错误: jQuery未加载!");
    }
    
    // 检查Bootstrap是否正常工作
    if (typeof bootstrap !== 'undefined') {
        console.log("Bootstrap已加载");
        console.log("Modal类是否可用:", typeof bootstrap.Modal !== 'undefined');
    } else {
        console.error("错误: Bootstrap库未加载!");
    }
    
    // 检查是否所有模态框都正确定义
    console.log("检查模态框元素:");
    ['#mediaMetaModal', '#durationModal', '#spritesModal', '#watermarkModal', '#gifModal', '#audioModal'].forEach(function(id) {
        var modal = document.querySelector(id);
        console.log(id + " 是否存在:", modal !== null);
        if (modal) {
            console.log(id + " class属性:", modal.className);
        }
    });
    
    // 为所有模态框按钮添加点击事件监听器
    document.querySelectorAll('[data-bs-toggle="modal"]').forEach(function(button) {
        var target = button.getAttribute('data-bs-target');
        console.log("找到模态框按钮:", target);
        
        button.addEventListener('click', function(e) {
            e.preventDefault(); // 阻止默认行为
            console.log("点击了模态框按钮:", target);
            
            try {
                var modalElement = document.querySelector(target);
                if (modalElement) {
                    console.log("尝试打开模态框:", target);
                    // 两种方法尝试打开模态框
                    try {
                        var modal = new bootstrap.Modal(modalElement);
                        modal.show();
                        console.log("方法1: bootstrap.Modal成功调用");
                    } catch (innerErr) {
                        console.error("方法1失败:", innerErr);
                        
                        // 备用方法: jQuery
                        try {
                            if (typeof jQuery !== 'undefined') {
                                console.log("尝试方法2: jQuery modal");
                                $(target).modal('show');
                                console.log("方法2: jQuery modal 调用完成");
                            }
                        } catch (jqErr) {
                            console.error("方法2也失败:", jqErr);
                        }
                    }
                } else {
                    console.error("找不到模态框元素:", target);
                }
            } catch (err) {
                console.error("模态框操作出错:", err);
            }
        });
    });
    
    console.log("===== 调试脚本加载完成 =====");
});

// 全局错误处理
window.onerror = function(message, source, lineno, colno, error) {
    console.error("全局错误:", message, "于", source, "行:", lineno, "列:", colno);
    return false;
};

// 在页面完全加载后执行
window.addEventListener('load', function() {
    console.log("===== 页面完全加载完成 =====");
});

