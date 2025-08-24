/**
 * 模态框调试跟踪脚本
 */
(function() {
    // 页面加载完成后执行
    window.addEventListener('DOMContentLoaded', function() {
        console.log('★★★ 模态框调试脚本已运行 ★★★');
        
        // 监听所有按钮点击事件，用于调试
        document.querySelectorAll('button[data-toggle="modal"]').forEach(function(btn) {
            const targetId = btn.getAttribute('data-target');
            console.log('发现模态框按钮: ' + targetId);
            
            btn.addEventListener('click', function(e) {
                console.log('按钮点击事件已触发，目标：' + targetId);
            });
        });
        
        // 添加全局点击监听器来调试所有点击事件
        document.addEventListener('click', function(e) {
            console.log('点击元素：', e.target.tagName, e.target.className);
        }, true);
        
        // 监听模态框事件
        $(document).on('show.bs.modal', function(e) {
            console.log('模态框正在显示：', e.target.id);
        });
        $(document).on('shown.bs.modal', function(e) {
            console.log('模态框已显示：', e.target.id);
        });
        $(document).on('hide.bs.modal', function(e) {
            console.log('模态框正在隐藏：', e.target.id);
        });
    });
})();
