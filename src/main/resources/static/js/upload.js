/**
 * 文件上传及处理功能相关JavaScript
 */
$(document).ready(function() {
    // 全局变量存储上传的文件信息
    let uploadedFiles = [];
    let selectedFileIndex = 0;
    
    // 文件选择按钮事件
    $('#selectFileButton').click(function() {
        $('#fileInput').click();
    });
    
    // 文件选择变更事件
    $('#fileInput').change(function(e) {
        if (this.files.length > 0) {
            handleFiles(this.files);
        }
    });
    
    // 拖放区域事件
    const dropzone = document.getElementById('dropzone');
    if (dropzone) {
        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            dropzone.addEventListener(eventName, preventDefaults, false);
        });
        
        ['dragenter', 'dragover'].forEach(eventName => {
            dropzone.addEventListener(eventName, function() {
                dropzone.classList.add('dropzone-highlight');
            }, false);
        });
        
        ['dragleave', 'drop'].forEach(eventName => {
            dropzone.addEventListener(eventName, function() {
                dropzone.classList.remove('dropzone-highlight');
            }, false);
        });
        
        // 处理文件拖放
        dropzone.addEventListener('drop', function(e) {
            const files = e.dataTransfer.files;
            if (files.length > 0) {
                handleFiles(files);
            }
        }, false);
    }
    
    // 添加更多文件按钮
    $('#addMoreFileBtn').click(function() {
        $('#fileInput').click();
    });
    
    // 处理文件上传
    function handleFiles(files) {
        // 显示上传进度
        $('#uploadProgress').removeClass('d-none');
        $('.progress-bar').width('0%').attr('aria-valuenow', 0);
        
        // 上传第一个文件
        uploadFile(files[0]);
    }
    
    // 上传单个文件
    function uploadFile(file) {
        // 检查文件大小
        if (file.size > 100 * 1024 * 1024) { // 100MB
            showToast('文件过大', '请选择小于100MB的文件', 'error');
            $('#uploadProgress').addClass('d-none');
            return;
        }
        
        const formData = new FormData();
        formData.append('file', file);
        
        $.ajax({
            url: '/ffmpeg-processor/api/upload',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            xhr: function() {
                const xhr = new window.XMLHttpRequest();
                xhr.upload.addEventListener('progress', function(e) {
                    if (e.lengthComputable) {
                        const percent = Math.round((e.loaded / e.total) * 100);
                        $('.progress-bar').width(percent + '%').attr('aria-valuenow', percent);
                    }
                }, false);
                return xhr;
            },
            success: function(response) {
                // 隐藏上传进度
                $('#uploadProgress').addClass('d-none');
                
                if (response.code === 0) {
                    // 添加到已上传文件列表
                    const fileObj = {
                        originalName: file.name,
                        size: file.size,
                        type: file.type,
                        bucketName: response.data.bucketName,
                        objectKey: response.data.objectKey
                    };
                    uploadedFiles.push(fileObj);
                    
                    // 更新UI
                    updateFileList();
                    
                    // 显示功能选择区域和文件列表
                    $('#uploadInitialArea').addClass('d-none');
                    $('#fileProcessArea').removeClass('d-none');
                } else {
                    showToast('上传失败', response.message || '文件上传失败', 'error');
                }
            },
            error: function(xhr, status, error) {
                $('#uploadProgress').addClass('d-none');
                showToast('上传失败', '请求错误: ' + error, 'error');
            }
        });
    }
    
    // 更新已上传文件列表
    function updateFileList() {
        const fileListEl = $('#fileList');
        fileListEl.empty();
        
        uploadedFiles.forEach((file, index) => {
            const fileSize = formatFileSize(file.size);
            const isSelected = index === selectedFileIndex ? 'selected' : '';
            
            // 显示文件图标
            let fileIcon = 'fa-file';
            if (file.type.includes('video')) {
                fileIcon = 'fa-file-video';
            } else if (file.type.includes('audio')) {
                fileIcon = 'fa-file-audio';
            }
            
            const fileItem = `
                <div class="file-item d-flex align-items-center p-2 border rounded mb-2 ${isSelected}" data-index="${index}">
                    <i class="fas ${fileIcon} mr-3 text-primary"></i>
                    <div class="flex-grow-1">
                        <div class="font-weight-bold">${file.originalName}</div>
                        <div class="text-muted small">${fileSize}</div>
                    </div>
                    <button class="btn btn-sm btn-danger remove-file-btn" data-index="${index}">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
            `;
            
            fileListEl.append(fileItem);
        });
        
        // 为文件项添加点击事件
        $('.file-item').click(function() {
            const index = $(this).data('index');
            selectedFileIndex = index;
            $('.file-item').removeClass('selected');
            $(this).addClass('selected');
        });
        
        // 为删除按钮添加事件
        $('.remove-file-btn').click(function(e) {
            e.stopPropagation();
            const index = $(this).data('index');
            uploadedFiles.splice(index, 1);
            
            if (uploadedFiles.length === 0) {
                // 没有文件了，返回初始上传界面
                $('#uploadInitialArea').removeClass('d-none');
                $('#fileProcessArea').addClass('d-none');
                $('#resultArea').addClass('d-none');
            } else {
                if (selectedFileIndex >= uploadedFiles.length) {
                    selectedFileIndex = uploadedFiles.length - 1;
                }
                updateFileList();
            }
        });
    }
    
    // 为功能按钮添加点击事件
    $(document).on('click', '.function-btn', function() {
        const functionName = $(this).data('function');
        if (uploadedFiles.length === 0) {
            showToast('请上传文件', '请先上传文件再执行操作', 'warning');
            return;
        }
        
        executeFunction(functionName, uploadedFiles[selectedFileIndex]);
    });
    
    // 执行功能操作
    function executeFunction(functionName, file) {
        // 显示结果区域
        $('#resultArea').removeClass('d-none');
        $('#resultArea').html(`
            <div class="card">
                <div class="card-header">
                    <h5 class="mb-0">处理中...</h5>
                </div>
                <div class="card-body text-center py-5">
                    <div class="spinner-border text-primary" role="status">
                        <span class="sr-only">处理中...</span>
                    </div>
                    <p class="mt-3">正在处理，请稍候...</p>
                </div>
            </div>
        `);
        
        // 滚动到结果区域
        $('html, body').animate({
            scrollTop: $("#resultArea").offset().top - 20
        }, 500);
        
        // 准备请求数据
        const requestData = {
            bucketName: file.bucketName,
            objectKey: file.objectKey
        };
        
        // 根据功能类型选择API
        let apiUrl = '';
        let apiTitle = '';
        
        switch(functionName) {
            case 'metadata':
                apiUrl = '/ffmpeg-processor/api/ffmpeg/getMediaMeta';
                apiTitle = '媒体元数据';
                break;
            case 'duration':
                apiUrl = '/ffmpeg-processor/api/ffmpeg/getDuration';
                apiTitle = '媒体时长';
                break;
            case 'sprites':
                // 雪碧图生成需要显示表单，供用户设置参数
                displaySpriteForm(file);
                // 不发送请求，由表单提交时处理
                return;
            case 'watermark':
                apiUrl = '/ffmpeg-processor/api/ffmpeg/watermark';
                apiTitle = '视频水印';
                break;
            default:
                showToast('未知功能', '未能识别的功能操作', 'error');
                $('#resultArea').addClass('d-none');
                return;
        }
        
        // 发送请求
        $.ajax({
            url: apiUrl,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(requestData),
            success: function(response) {
                if (response.code === 0) {
                    // 根据不同功能显示结果
                    switch(functionName) {
                        case 'metadata':
                            displayMediaMetadata(response.data, apiTitle);
                            break;
                        case 'duration':
                            displayMediaDuration(response.data, apiTitle);
                            break;
                        case 'sprites':
                            displaySpriteResult(response.data, apiTitle);
                            break;
                        case 'watermark':
                            // 水印结果展示
                            break;
                    }
                } else {
                    $('#resultArea').html(`
                        <div class="card">
                            <div class="card-header bg-danger text-white">
                                <h5 class="mb-0">处理出错</h5>
                            </div>
                            <div class="card-body">
                                <div class="alert alert-danger">
                                    <i class="fas fa-exclamation-circle mr-2"></i>
                                    ${response.message || '操作处理失败'}
                                </div>
                            </div>
                        </div>
                    `);
                }
            },
            error: function(xhr, status, error) {
                $('#resultArea').html(`
                    <div class="card">
                        <div class="card-header bg-danger text-white">
                            <h5 class="mb-0">请求失败</h5>
                        </div>
                        <div class="card-body">
                            <div class="alert alert-danger">
                                <i class="fas fa-exclamation-circle mr-2"></i>
                                请求失败: ${error}
                            </div>
                        </div>
                    </div>
                `);
            }
        });
    }
    
    // 显示媒体时长结果
    function displayMediaDuration(data, title) {
        console.log('原始媒体时长数据:', data);
        
        // 解析时长数据
        let seconds = 0;
        let formattedTime = '00:00.000';
        
        // 如果是JSON对象或JSON字符串
        if (typeof data === 'object' && data.duration) {
            // 已经是JSON对象
            seconds = parseFloat(data.duration);
            formattedTime = data.formatted_duration || formatDuration(seconds);
        } else if (typeof data === 'string') {
            try {
                // 尝试解析JSON字符串
                const parsedData = JSON.parse(data);
                if (parsedData.duration) {
                    seconds = parseFloat(parsedData.duration);
                    formattedTime = parsedData.formatted_duration || formatDuration(seconds);
                } else {
                    // 如果不是JSON格式，尝试直接解析为数字
                    seconds = parseFloat(data);
                    formattedTime = formatDuration(seconds);
                }
            } catch(e) {
                console.error('解析时长数据失败:', e);
                // 尝试作为纯数字处理
                seconds = parseFloat(data) || 0;
                formattedTime = formatDuration(seconds);
            }
        } else if (typeof data === 'number') {
            seconds = data;
            formattedTime = formatDuration(seconds);
        }
        
        // 显示结果
        $('#resultArea').html(`
            <div class="card">
                <div class="card-header bg-success text-white">
                    <h5 class="mb-0">${title}分析完成</h5>
                </div>
                <div class="card-body">
                    <table class="table table-bordered">
                        <thead class="thead-light">
                            <tr>
                                <th colspan="2" class="text-center">媒体时长信息</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <th style="width: 30%">精确时长（秒）</th>
                                <td>${seconds}</td>
                            </tr>
                            <tr>
                                <th>格式化时长</th>
                                <td>${formattedTime}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        `);
    }
    
    // 显示媒体元数据结果
    function displayMediaMetadata(data, title) {
        // 输出原始数据到控制台以便调试
        console.log('媒体元数据原始数据:', data);
        
        // 如果是字符串，尝试解析JSON
        let metaData = data;
        if (typeof data === 'string') {
            try {
                metaData = JSON.parse(data);
            } catch(e) {
                console.error('无法解析元数据:', e);
                $('#resultArea').html(`
                    <div class="alert alert-danger">
                        <i class="fas fa-exclamation-circle mr-2"></i>
                        无法解析元数据: ${e.message}
                    </div>
                `);
                return;
            }
        }
        
        // 创建基本信息HTML
        let resultHtml = `
            <div class="card">
                <div class="card-header bg-success text-white">
                    <h5 class="mb-0">${title || '媒体'}分析完成</h5>
                </div>
                <div class="card-body">
                    <h5 class="mb-3">基本信息</h5>
                    <table class="table table-bordered mb-4">
                        <thead class="thead-light">
                            <tr>
                                <th colspan="2" class="text-center">媒体元数据</th>
                            </tr>
                        </thead>
                        <tbody>`;
        
        // 处理后端返回的新格式数据 (format对象)
        if (metaData.format && typeof metaData.format === 'object') {
            const format = metaData.format;
            
            // 添加常见的重要字段
            resultHtml += `
                <tr>
                    <th style="width: 30%">文件名</th>
                    <td>${format.filename || '-'}</td>
                </tr>
                <tr>
                    <th>格式</th>
                    <td>${format.format_name || '-'} (${format.format_long_name || '-'})</td>
                </tr>
                <tr>
                    <th>时长(秒)</th>
                    <td>${format.duration || '-'}</td>
                </tr>
                <tr>
                    <th>大小(字节)</th>
                    <td>${format.size || '-'}</td>
                </tr>
                <tr>
                    <th>比特率</th>
                    <td>${format.bit_rate || '-'}</td>
                </tr>
                <tr>
                    <th>流数量</th>
                    <td>${format.nb_streams || '-'}</td>
                </tr>
                <tr>
                    <th>开始时间</th>
                    <td>${format.start_time || '-'}</td>
                </tr>
                <tr>
                    <th>探测得分</th>
                    <td>${format.probe_score || '-'}</td>
                </tr>`;
            
            // 遍历其他format字段，排除已显示的和tags字段(单独显示)
            const excludedKeys = ['filename', 'format_name', 'format_long_name', 'duration', 
                               'size', 'bit_rate', 'nb_streams', 'start_time', 'tags', 'probe_score'];
            
            for (const [key, value] of Object.entries(format)) {
                if (!excludedKeys.includes(key) && typeof value !== 'object') {
                    resultHtml += `
                    <tr>
                        <th>${key.replace(/_/g, ' ')}</th>
                        <td>${value}</td>
                    </tr>`;
                }
            }
            
            // 处理tags信息
            if (format.tags && Object.keys(format.tags).length > 0) {
                resultHtml += `
                <tr>
                    <th colspan="2" class="bg-light">媒体标签</th>
                </tr>`;
                
                for (const [key, value] of Object.entries(format.tags)) {
                    resultHtml += `
                    <tr>
                        <th>${key.replace(/_/g, ' ')}</th>
                        <td>${value}</td>
                    </tr>`;
                }
            }
        } else {
            // 兼容旧版接口格式
            resultHtml += `
                <tr>
                    <th style="width: 30%">格式</th>
                    <td>${metaData.format || '-'}</td>
                </tr>
                <tr>
                    <th>时长</th>
                    <td>${metaData.duration || '-'}</td>
                </tr>
                <tr>
                    <th>宽度</th>
                    <td>${metaData.width || '-'}</td>
                </tr>
                <tr>
                    <th>高度</th>
                    <td>${metaData.height || '-'}</td>
                </tr>
                <tr>
                    <th>比特率</th>
                    <td>${metaData.bitRate || '-'}</td>
                </tr>`;
        }
        
        resultHtml += `
                        </tbody>
                    </table>`;
        
        // 处理视频流信息
        if (metaData.videoStreams && metaData.videoStreams.length > 0) {
            resultHtml += `
                <h5 class="mb-3">视频流信息</h5>
                <table class="table table-bordered table-striped mb-4">
                    <thead class="thead-light">
                        <tr>
                            <th>索引</th>
                            <th>编码器</th>
                            <th>分辨率</th>
                            <th>帧率</th>
                            <th>像素格式</th>
                        </tr>
                    </thead>
                    <tbody>`;
            
            metaData.videoStreams.forEach(stream => {
                resultHtml += `
                    <tr>
                        <td>${stream.index || '-'}</td>
                        <td>${stream.codecName || '-'}</td>
                        <td>${stream.width || '-'}x${stream.height || '-'}</td>
                        <td>${stream.frameRate || '-'}</td>
                        <td>${stream.pixelFormat || '-'}</td>
                    </tr>`;
            });
            
            resultHtml += `
                    </tbody>
                </table>`;
        }
        
        // 处理音频流信息
        if (metaData.audioStreams && metaData.audioStreams.length > 0) {
            resultHtml += `
                <h5 class="mb-3">音频流信息</h5>
                <table class="table table-bordered table-striped mb-4">
                    <thead class="thead-light">
                        <tr>
                            <th>索引</th>
                            <th>编码器</th>
                            <th>采样率</th>
                            <th>声道数</th>
                            <th>比特率</th>
                        </tr>
                    </thead>
                    <tbody>`;
            
            metaData.audioStreams.forEach(stream => {
                resultHtml += `
                    <tr>
                        <td>${stream.index || '-'}</td>
                        <td>${stream.codecName || '-'}</td>
                        <td>${stream.sampleRate || '-'}</td>
                        <td>${stream.channels || '-'}</td>
                        <td>${stream.bitRate || '-'}</td>
                    </tr>`;
            });
            
            resultHtml += `
                    </tbody>
                </table>`;
        }
        
        // 显示最终结果
        $('#resultArea').html(resultHtml + '</div></div>');
    }
    
    // 显示雪碧图生成表单
    function displaySpriteForm(file) {
        console.log('生成雪碧图表单，使用文件:', file);
        if (!file || !file.bucketName || !file.objectKey) {
            showToast('错误', '无法获取文件信息，请重新上传', 'error');
            return;
        }
        
        $('#resultArea').html(`
            <div class="card">
                <div class="card-header bg-info text-white">
                    <h5 class="mb-0">雪碧图生成参数</h5>
                </div>
                <div class="card-body">
                    <form id="spriteForm" data-bucket="${file.bucketName}" data-object="${file.objectKey}">
                        <input type="hidden" name="bucket_name" value="${file.bucketName}">
                        <input type="hidden" name="object_key" value="${file.objectKey}">
                        <input type="hidden" name="output_dir" value="sprites/">
                        
                        <div class="form-group row">
                            <label class="col-md-3 col-form-label">视频文件</label>
                            <div class="col-md-9">
                                <p class="form-control-plaintext">${file.originalName}</p>
                            </div>
                        </div>
                        
                        <!-- 雪碧图布局 -->
                        <div class="form-group row">
                            <label class="col-md-3 col-form-label">雪碧图布局 *</label>
                            <div class="col-md-9">
                                <input type="text" class="form-control" name="tile" value="3*4" required>
                                <small class="form-text text-muted">格式为：行数*列数（例如：3*4）</small>
                            </div>
                        </div>
                        
                        <!-- 时间参数 -->
                        <div class="form-group row">
                            <label class="col-md-3 col-form-label">截图间隔（秒）</label>
                            <div class="col-md-9">
                                <input type="number" class="form-control" name="interval" value="10" min="1">
                                <small class="form-text text-muted">每隔多少秒截取一张图片</small>
                            </div>
                        </div>
                        
                        <div class="form-group row">
                            <label class="col-md-3 col-form-label">开始时间（秒）</label>
                            <div class="col-md-9">
                                <input type="number" class="form-control" name="start" value="0" min="0">
                                <small class="form-text text-muted">从视频的哪一秒开始截图</small>
                            </div>
                        </div>
                        
                        <div class="form-group row">
                            <label class="col-md-3 col-form-label">持续时间（秒）</label>
                            <div class="col-md-9">
                                <input type="number" class="form-control" name="duration" min="1" placeholder="可选">
                                <small class="form-text text-muted">截取多长时间的视频（留空表示截取到结束）</small>
                            </div>
                        </div>
                        
                        <!-- 图片参数 -->
                        <div class="form-group row">
                            <label class="col-md-3 col-form-label">图片缩放</label>
                            <div class="col-md-9">
                                <div class="input-group">
                                    <input type="text" class="form-control" name="scale" value="-1:-1" placeholder="例如：320:240">
                                </div>
                                <small class="form-text text-muted">格式为宽:高，-1表示保持原始比例</small>
                            </div>
                        </div>
                        
                        <div class="form-group row">
                            <label class="col-md-3 col-form-label">图片格式</label>
                            <div class="col-md-9">
                                <select class="form-control" name="dst_type">
                                    <option value="jpg">JPG</option>
                                    <option value="png">PNG</option>
                                </select>
                            </div>
                        </div>
                        
                        <div class="form-group row">
                            <div class="col-md-9 offset-md-3">
                                <button type="submit" class="btn btn-primary">
                                    <i class="fas fa-play mr-1"></i> 生成雪碧图
                                </button>
                                <button type="button" class="btn btn-secondary ml-2" id="cancelSpriteBtn">
                                    <i class="fas fa-times mr-1"></i> 取消
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        `);
        
        // 取消按钮事件
        $('#cancelSpriteBtn').click(function() {
            $('#resultArea').addClass('d-none');
        });
        
        // 表单提交事件
        $('#spriteForm').submit(function(e) {
            e.preventDefault();
            
            // 从表单data属性获取bucketName和objectKey
            const bucketName = $(this).data('bucket');
            const objectKey = $(this).data('object');
            
            console.log('提交雪碧图请求：', {
                bucketName: bucketName,
                objectKey: objectKey
            });
            
            if (!bucketName || !objectKey) {
                showToast('错误', '无法获取文件信息，请重新上传', 'error');
                return;
            }
            
            // 显示加载中
            $('#resultArea').html(`
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">雪碧图生成中...</h5>
                    </div>
                    <div class="card-body text-center py-5">
                        <div class="spinner-border text-primary" role="status">
                            <span class="sr-only">处理中...</span>
                        </div>
                        <p class="mt-3">正在生成雪碧图，可能需要几分钟，请耐心等待...</p>
                    </div>
                </div>
            `);
            
            // 获取所有表单数据，使用snake_case格式与后端API匹配
            const formData = {
                bucket_name: bucketName,
                object_key: objectKey,
                output_dir: $('input[name="output_dir"]').val() || 'sprites/',
                // 必须参数：雪碧图布局
                tile: $('input[name="tile"]').val() || '3*4',
                // 可选参数
                start: parseFloat($('input[name="start"]').val()) || 0,
                interval: parseFloat($('input[name="interval"]').val()) || 5,
                itsoffset: parseFloat($('input[name="itsoffset"]').val()) || 0,
                padding: parseInt($('input[name="padding"]').val()) || 1,
                color: $('input[name="color"]').val() || 'black',
                dst_type: $('select[name="dst_type"]').val() || 'jpg'
            };
            
            // 只有在用户填写了duration值时才添加它
            const duration = $('input[name="duration"]').val();
            if (duration && duration.trim() !== '') {
                formData.duration = parseFloat(duration);
            }
            
            // 只有在用户修改了scale默认值时才添加它
            const scale = $('input[name="scale"]').val();
            if (scale && scale.trim() !== '' && scale !== '-1:-1') {
                formData.scale = scale;
            }
            
            console.log('发送雪碧图请求数据:', formData);
            
            // 发送请求
            $.ajax({
                url: '/ffmpeg-processor/api/sprite/generate',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(formData),
                timeout: 120000, // 延长超时时间到2分钟
                success: function(response) {
                    if (response.code === 0) {
                        displaySpriteResult(response.data, '雪碧图生成');
                    } else {
                        $('#resultArea').html(`
                            <div class="card">
                                <div class="card-header bg-danger text-white">
                                    <h5 class="mb-0">处理出错</h5>
                                </div>
                                <div class="card-body">
                                    <div class="alert alert-danger">
                                        <i class="fas fa-exclamation-circle mr-2"></i>
                                        ${response.message || '雪碧图生成失败'}
                                    </div>
                                </div>
                            </div>
                        `);
                    }
                },
                error: function(xhr, status, error) {
                    console.error('雪碧图请求失败:', status, error, xhr.responseText);
                    let errorMsg = error;
                    try {
                        if (xhr.responseText) {
                            const response = JSON.parse(xhr.responseText);
                            if (response.message) {
                                errorMsg = response.message;
                            }
                        }
                    } catch (e) {
                        // 解析失败时使用默认错误
                    }
                    
                    $('#resultArea').html(`
                        <div class="card">
                            <div class="card-header bg-danger text-white">
                                <h5 class="mb-0">请求失败</h5>
                            </div>
                            <div class="card-body">
                                <div class="alert alert-danger">
                                    <i class="fas fa-exclamation-circle mr-2"></i>
                                    请求失败: ${errorMsg}
                                </div>
                            </div>
                        </div>
                    `);
                }
            });
        });
    }
    
    // 显示雪碧图生成结果
    function displaySpriteResult(data, title) {
        let spriteData = data;
        
        console.log('处理雪碧图结果数据:', data);
        
        // 如果是字符串，尝试解析JSON
        if (typeof data === 'string') {
            try {
                spriteData = JSON.parse(data);
            } catch(e) {
                console.error('无法解析雪碧图数据:', e);
                $('#resultArea').html(`
                    <div class="alert alert-danger">
                        <i class="fas fa-exclamation-circle mr-2"></i>
                        无法解析雪碧图数据: ${e.message}
                    </div>
                `);
                return;
            }
        }
        
        console.log('雪碧图数据:', spriteData);
        
        // 后端返回的是spriteUrls数组，我们需要处理这种情况
        if (!spriteData || (!spriteData.spriteUrl && !spriteData.url && (!spriteData.spriteUrls || !spriteData.spriteUrls.length))) {
            console.error('雪碧图数据格式错误:', spriteData);
            $('#resultArea').html(`
                <div class="alert alert-danger">
                    <i class="fas fa-exclamation-circle mr-2"></i>
                    雪碧图数据格式错误，无法显示结果
                </div>
            `);
            return;
        }
        
        // 如果是后端返回的spriteUrls数组，则取第一个URL作为雪碧图预览
        if (!spriteData.spriteUrl && spriteData.spriteUrls && spriteData.spriteUrls.length > 0) {
            spriteData.spriteUrl = spriteData.spriteUrls[0];
            console.log('使用spriteUrls中的第一个URL:', spriteData.spriteUrl);
        }
        // 兼容旧代码的url字段
        else if (!spriteData.spriteUrl && spriteData.url) {
            spriteData.spriteUrl = spriteData.url;
        }
        
        // 显示结果
        $('#resultArea').html(`
            <div class="card">
                <div class="card-header bg-success text-white">
                    <h5 class="mb-0">${title}完成</h5>
                </div>
                <div class="card-body">
                    <div class="row mb-4">
                        <div class="col-md-12">
                            <div class="card">
                                <div class="card-header bg-light">
                                    <h6 class="mb-0">雪碧图预览</h6>
                                </div>
                                <div class="card-body text-center">
                                    <img src="${spriteData.spriteUrl}" class="img-fluid border" alt="雪碧图预览">
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6">
                            <div class="card">
                                <div class="card-header bg-light">
                                    <h6 class="mb-0">雪碧图信息</h6>
                                </div>
                                <div class="card-body">
                                    <table class="table table-bordered">
                                        <tbody>
                                            <tr>
                                                <th style="width: 30%">总帧数</th>
                                                <td>${spriteData.framesCount || '-'}</td>
                                            </tr>
                                            <tr>
                                                <th>行数</th>
                                                <td>${spriteData.rows || '-'}</td>
                                            </tr>
                                            <tr>
                                                <th>列数</th>
                                                <td>${spriteData.columns || '-'}</td>
                                            </tr>
                                            <tr>
                                                <th>图片尺寸</th>
                                                <td>${spriteData.spriteWidth || '-'} x ${spriteData.spriteHeight || '-'}</td>
                                            </tr>
                                            <tr>
                                                <th>格式</th>
                                                <td>${spriteData.format || '-'}</td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="card">
                                <div class="card-header bg-light">
                                    <h6 class="mb-0">链接和操作</h6>
                                </div>
                                <div class="card-body">
                                    <div class="input-group mb-3">
                                        <input type="text" class="form-control" value="${spriteData.spriteUrl || ''}" id="spriteUrlInput" readonly>
                                        <div class="input-group-append">
                                            <button class="btn btn-outline-secondary copy-btn" type="button" data-clipboard-target="#spriteUrlInput">
                                                <i class="fas fa-copy"></i> 复制
                                            </button>
                                        </div>
                                    </div>
                                    <div class="btn-group w-100">
                                        <a href="${spriteData.spriteUrl || '#'}" class="btn btn-primary" target="_blank" download>
                                            <i class="fas fa-download mr-1"></i> 下载雪碧图
                                        </a>
                                        <a href="${spriteData.vttUrl || '#'}" class="btn btn-info" target="_blank" download>
                                            <i class="fas fa-file-alt mr-1"></i> 下载VTT文件
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `);
        
        // 初始化复制按钮
        if (typeof ClipboardJS !== 'undefined') {
            new ClipboardJS('.copy-btn');
            
            $('.copy-btn').click(function() {
                showToast('复制成功', '链接已复制到剪贴板', 'success');
            });
        }
    }
    
    // 防止默认事件
    function preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
    }
    
    // 格式化文件大小
    function formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }
    
    // 格式化时长 - 与后端格式兼容 (分:秒.毫秒)
    function formatDuration(seconds) {
        if (isNaN(seconds) || seconds < 0) {
            return '00:00.000';
        }
        
        // 提取小数部分作为毫秒
        const totalSeconds = Math.floor(seconds);
        const milliseconds = Math.floor((seconds - totalSeconds) * 1000);
        
        // 计算小时、分钟、秒
        const hours = Math.floor(totalSeconds / 3600);
        const minutes = Math.floor((totalSeconds % 3600) / 60);
        const secs = totalSeconds % 60;
        
        // 根据时长决定格式
        if (hours > 0) {
            // 如果有小时，使用 HH:MM:SS.mmm 格式
            return `${padZero(hours)}:${padZero(minutes)}:${padZero(secs)}.${padZeroMs(milliseconds)}`;
        } else {
            // 如果没有小时，使用 MM:SS.mmm 格式（与后端兼容）
            return `${padZero(minutes)}:${padZero(secs)}.${padZeroMs(milliseconds)}`;
        }
    }

    // 将数字填限0到两位
    function padZero(num) {
        return num < 10 ? '0' + num : num;
    }

    // 将毫秒填限0到3位
    function padZeroMs(ms) {
        if (ms < 10) return '00' + ms;
        if (ms < 100) return '0' + ms;
        return ms;
    }
    
    // 显示Toast消息
    function showToast(title, message, type) {
        if ($.toast) {
            $.toast({
                heading: title,
                text: message,
                showHideTransition: 'slide',
                icon: type || 'info',
                position: 'top-right',
                hideAfter: 3000
            });
        } else {
            alert(title + ': ' + message);
        }
    }
});