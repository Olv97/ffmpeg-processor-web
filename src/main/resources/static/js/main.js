/**
 * FFmpeg音视频处理平台前端交互脚本
 */
$(document).ready(function() {
    // 通用API调用函数
    function callApi(endpoint, requestData, resultElement) {
        // 显示加载中状态
        $(resultElement).text("处理中，请稍候...");
        
        $.ajax({
            url: '/ffmpeg-processor/api/ffmpeg/' + endpoint,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(requestData),
            success: function(response) {
                if (response.code === 0) {
                    // 处理成功
                    if (typeof response.data === 'object') {
                        $(resultElement).text(JSON.stringify(response.data, null, 2));
                    } else {
                        $(resultElement).text(response.data);
                    }
                    showToast('操作成功', '处理已完成！', 'success');
                } else {
                    // 处理失败
                    $(resultElement).text('错误: ' + response.message);
                    showToast('操作失败', response.message, 'error');
                }
            },
            error: function(xhr, status, error) {
                $(resultElement).text('请求失败: ' + error);
                showToast('请求失败', '服务器连接错误', 'error');
            }
        });
    }
    
    // 显示提示信息
    function showToast(title, message, type) {
        // 简单的提示实现，如果需要可以更换成其他toast库
        alert(title + ': ' + message);
    }
    
    // 获取媒体元信息
    $('#getMediaMetaButton').click(function() {
        const bucketName = $('#metaBucketName').val();
        const objectKey = $('#metaObjectKey').val();
        
        if (!bucketName || !objectKey) {
            showToast('验证失败', '请填写所有必填字段', 'error');
            return;
        }
        
        // 显示加载状态，隐藏结果和错误
        $('#mediaMetaLoading').removeClass('d-none');
        $('#mediaMetaResultContainer').addClass('d-none');
        $('#mediaMetaError').addClass('d-none');
        
        const requestData = {
            bucketName: bucketName,
            objectKey: objectKey
        };
        
        // 使用AJAX直接处理响应，不使用通用callApi函数
        $.ajax({
            url: '/ffmpeg-processor/api/ffmpeg/getMediaMeta',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(requestData),
            success: function(response) {
                // 隐藏加载状态
                $('#mediaMetaLoading').addClass('d-none');
                
                if (response.code === 0) {
                    // 显示结果容器
                    $('#mediaMetaResultContainer').removeClass('d-none');
                    
                    // 解析媒体元数据并填充表格
                    displayMediaMetadata(response.data);
                } else {
                    // 显示错误信息
                    $('#mediaMetaError').removeClass('d-none');
                    $('#mediaMetaErrorText').text(response.message);
                }
            },
            error: function(xhr, status, error) {
                // 隐藏加载状态，显示错误
                $('#mediaMetaLoading').addClass('d-none');
                $('#mediaMetaError').removeClass('d-none');
                $('#mediaMetaErrorText').text('请求失败: ' + error);
            }
        });
    });
    
    // 显示媒体元数据函数
    function displayMediaMetadata(metadata) {
        // 填充基本信息
        $('#meta_fileName').text(metadata.fileName || '-');
        $('#meta_format').text(metadata.format || '-');
        $('#meta_duration').text(metadata.duration || '-');
        $('#meta_size').text(metadata.size || '-');
        $('#meta_bitRate').text(metadata.bitRate || '-');
        
        // 处理视频流信息
        if (metadata.videoStreams && metadata.videoStreams.length > 0) {
            let videoHtml = '<table class="table table-bordered table-striped"><thead><tr>' +
                '<th>序号</th>' +
                '<th>编解码器</th>' +
                '<th>分辨率</th>' +
                '<th>帧率</th>' +
                '<th>比特率</th>' +
                '<th>像素格式</th>' +
                '</tr></thead><tbody>';
                
            metadata.videoStreams.forEach(function(stream) {
                videoHtml += '<tr>' +
                    '<td>' + (stream.index + 1) + '</td>' +
                    '<td>' + (stream.codecName || '-') + '</td>' +
                    '<td>' + (stream.width ? (stream.width + 'x' + stream.height) : '-') + '</td>' +
                    '<td>' + (stream.frameRate || '-') + '</td>' +
                    '<td>' + (stream.bitRate || '-') + '</td>' +
                    '<td>' + (stream.pixelFormat || '-') + '</td>' +
                    '</tr>';
            });
            
            videoHtml += '</tbody></table>';
            $('#videoStreamsContainer').html(videoHtml);
        } else {
            $('#videoStreamsContainer').html('<div class="alert alert-info">无视频流信息</div>');
        }
        
        // 处理音频流信息
        if (metadata.audioStreams && metadata.audioStreams.length > 0) {
            let audioHtml = '<table class="table table-bordered table-striped"><thead><tr>' +
                '<th>序号</th>' +
                '<th>编解码器</th>' +
                '<th>声道数</th>' +
                '<th>声道布局</th>' +
                '<th>采样率</th>' +
                '<th>比特率</th>' +
                '</tr></thead><tbody>';
                
            metadata.audioStreams.forEach(function(stream) {
                audioHtml += '<tr>' +
                    '<td>' + (stream.index + 1) + '</td>' +
                    '<td>' + (stream.codecName || '-') + '</td>' +
                    '<td>' + (stream.channels || '-') + '</td>' +
                    '<td>' + (stream.channelLayout || '-') + '</td>' +
                    '<td>' + (stream.sampleRate || '-') + '</td>' +
                    '<td>' + (stream.bitRate || '-') + '</td>' +
                    '</tr>';
            });
            
            audioHtml += '</tbody></table>';
            $('#audioStreamsContainer').html(audioHtml);
        } else {
            $('#audioStreamsContainer').html('<div class="alert alert-info">无音频流信息</div>');
        }
        
        // 处理标签信息
        // 特殊处理：我们假设原始JSON包含了format.tags字段，这部分需要额外处理
        // 这需要修改MediaMetadataParser类来额外提取这些信息
        if (metadata.tags && Object.keys(metadata.tags).length > 0) {
            let tagsHtml = '<table class="table table-bordered table-striped"><thead><tr>' +
                '<th style="width: 30%">名称</th>' +
                '<th>值</th>' +
                '</tr></thead><tbody>';
                
            for (const [key, value] of Object.entries(metadata.tags)) {
                tagsHtml += '<tr>' +
                    '<td>' + getTagDisplayName(key) + '</td>' +
                    '<td>' + value + '</td>' +
                    '</tr>';
            }
            
            tagsHtml += '</tbody></table>';
            $('#tagsContainer').html(tagsHtml);
        } else {
            $('#tagsContainer').html('<div class="alert alert-info">无标签信息</div>');
        }
    }
    
    // 将标签键名转换为友好显示名称
    function getTagDisplayName(key) {
        const tagMapping = {
            'major_brand': '主品牌',
            'minor_version': '次版本',
            'compatible_brands': '兼容品牌',
            'creation_time': '创建时间',
            'artist': '艺术家',
            'title': '标题',
            'comment': '备注',
            'encoder': '编码器',
            'handler_name': '处理器名称',
            'vendor_id': '供应商ID',
            'language': '语言',
            'date': '日期',
            'album': '专辑',
            'composer': '作曲家',
            'genre': '流派',
            'publisher': '发行商',
            'track': '音轨',
            'year': '年份'
        };
        
        return tagMapping[key] || key;
    }
    
    // 显示媒体时长函数 - 类似元数据处理
    function displayMediaDuration(durationData) {
        if (typeof durationData === 'string') {
            try {
                // 解析字符串形式的JSON
                durationData = JSON.parse(durationData);
            } catch(e) {
                console.error('无法解析时长数据:', e);
                $('#duration_seconds').text('无法解析');
                $('#duration_formatted').text('无法解析');
                return;
            }
        }
        
        // 确保数据存在并格式化
        if (durationData && durationData.duration !== undefined) {
            const seconds = parseFloat(durationData.duration).toFixed(3);
            const formattedTime = durationData.formatted_duration || formatDurationForDisplay(parseFloat(durationData.duration));
            
            // 填充表格数据
            $('#duration_seconds').text(seconds);
            $('#duration_formatted').text(formattedTime);
        } else {
            $('#duration_seconds').text('无效数据');
            $('#duration_formatted').text('无效数据');
        }
    }
    
    // 获取媒体时长 - 完全参照元数据获取的实现方式
    $('#getDurationButton').click(function() {
        console.log('获取时长按钮被点击');
        
        // 调试DOM元素
        console.log('加载标识存在：', $('#durationLoading').length);
        console.log('结果容器存在：', $('#durationResultContainer').length);
        console.log('错误容器存在：', $('#durationError').length);
        console.log('结果字段存在：', $('#duration_seconds').length, $('#duration_formatted').length);
        
        const bucketName = $('#durationBucketName').val();
        const objectKey = $('#durationObjectKey').val();
        
        console.log('表单值：', bucketName, objectKey);
        
        if (!bucketName || !objectKey) {
            showToast('验证失败', '请填写所有必填字段', 'error');
            return;
        }
        
        // 显示加载状态，隐藏结果和错误
        console.log('尝试显示加载器...');
        $('#durationLoading').removeClass('d-none');
        $('#durationResultContainer').addClass('d-none');
        $('#durationError').addClass('d-none');
        console.log('加载器可见性：', !$('#durationLoading').hasClass('d-none'));
        
        const requestData = {
            bucketName: bucketName,
            objectKey: objectKey
        };
        
        // 使用AJAX直接处理响应，完全参照元数据获取的实现方式
        $.ajax({
            url: '/ffmpeg-processor/api/ffmpeg/getDuration',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(requestData),
            success: function(response) {
                // 隐藏加载状态
                $('#durationLoading').addClass('d-none');
                
                if (response.code === 0) {
                    // 显示结果容器
                    $('#durationResultContainer').removeClass('d-none');
                    
                    // 在控制台输出数据便于调试
                    console.log('接收到的时长数据:', response.data);
                    
                    // 直接使用displayMediaDuration处理数据，它已具备解析功能
                    displayMediaDuration(response.data);
                } else {
                    // 显示错误信息
                    $('#durationError').removeClass('d-none');
                    $('#durationErrorText').text(response.message || '获取时长数据失败');
                }
            },
            error: function(xhr, status, error) {
                // 隐藏加载状态，显示错误
                $('#durationLoading').addClass('d-none');
                $('#durationError').removeClass('d-none');
                $('#durationErrorText').text('请求失败: ' + error);
            }
        });
    });
    
    // 格式化时长显示函数
    function formatDurationForDisplay(durationSeconds) {
        const totalSeconds = Math.floor(durationSeconds);
        const hours = Math.floor(totalSeconds / 3600);
        const minutes = Math.floor((totalSeconds % 3600) / 60);
        const seconds = totalSeconds % 60;
        const milliseconds = Math.floor((durationSeconds - totalSeconds) * 1000);
        
        if (hours > 0) {
            return `${hours}:${padZero(minutes)}:${padZero(seconds)}.${padZeroMs(milliseconds)}`;
        } else {
            return `${padZero(minutes)}:${padZero(seconds)}.${padZeroMs(milliseconds)}`;
        }
    }
    
    function padZero(num) {
        return num < 10 ? '0' + num : num;
    }
    
    function padZeroMs(num) {
        if (num < 10) return '00' + num;
        if (num < 100) return '0' + num;
        return num;
    }
    
    // 生成雪碧图
    $('#getSpritesButton').click(function() {
        const bucketName = $('#spritesBucketName').val();
        const objectKey = $('#spritesObjectKey').val();
        const outputDir = $('#spritesOutputDir').val();
        const tile = $('#spritesTile').val();
        const start = parseInt($('#spritesStart').val()) || 0;
        const duration = parseInt($('#spritesDuration').val());
        const interval = parseInt($('#spritesInterval').val()) || 5;
        
        if (!bucketName || !objectKey || !outputDir || !tile) {
            showToast('验证失败', '请填写所有必填字段', 'error');
            return;
        }
        
        const requestData = {
            bucketName: bucketName,
            objectKey: objectKey,
            outputDir: outputDir,
            tile: tile,
            start: start,
            duration: duration,
            interval: interval
        };
        
        callApi('getSprites', requestData, '#spritesResult');
    });
    
    // 视频添加水印
    $('#watermarkButton').click(function() {
        const bucketName = $('#watermarkBucketName').val();
        const objectKey = $('#watermarkObjectKey').val();
        const outputDir = $('#watermarkOutputDir').val();
        const vfArgs = $('#watermarkVfArgs').val();
        const filterComplexArgs = $('#watermarkFilterComplexArgs').val();
        
        if (!bucketName || !objectKey || !outputDir || (!vfArgs && !filterComplexArgs)) {
            showToast('验证失败', '请填写所有必填字段', 'error');
            return;
        }
        
        const requestData = {
            bucketName: bucketName,
            objectKey: objectKey,
            outputDir: outputDir,
            vfArgs: vfArgs,
            filterComplexArgs: filterComplexArgs
        };
        
        callApi('videoWatermark', requestData, '#watermarkResult');
    });
    
    // 视频转GIF
    $('#gifButton').click(function() {
        const bucketName = $('#gifBucketName').val();
        const objectKey = $('#gifObjectKey').val();
        const outputDir = $('#gifOutputDir').val();
        const start = parseInt($('#gifStart').val()) || 0;
        const duration = parseInt($('#gifDuration').val());
        const vframes = parseInt($('#gifVframes').val());
        
        if (!bucketName || !objectKey || !outputDir) {
            showToast('验证失败', '请填写所有必填字段', 'error');
            return;
        }
        
        const requestData = {
            bucketName: bucketName,
            objectKey: objectKey,
            outputDir: outputDir,
            start: start,
            duration: duration,
            vframes: vframes
        };
        
        callApi('videoGif', requestData, '#gifResult');
    });
    
    // 音频格式转换
    $('#audioButton').click(function() {
        const bucketName = $('#audioBucketName').val();
        const objectKey = $('#audioObjectKey').val();
        const outputDir = $('#audioOutputDir').val();
        const dstType = $('#audioDstType').val();
        const ac = parseInt($('#audioAc').val());
        const ar = parseInt($('#audioAr').val());
        
        if (!bucketName || !objectKey || !outputDir || !dstType) {
            showToast('验证失败', '请填写所有必填字段', 'error');
            return;
        }
        
        const requestData = {
            bucketName: bucketName,
            objectKey: objectKey,
            outputDir: outputDir,
            dstType: dstType,
            ac: ac,
            ar: ar
        };
        
        callApi('audioConvert', requestData, '#audioResult');
    });
});
