# FFmpeg Processor Web Application

基于Spring Boot的FFmpeg处理器Web应用，支持通过阿里云函数计算进行视频处理。

## 项目简介

这是一个Web应用程序，用于调用阿里云函数计算上的FFmpeg处理功能。支持视频格式转换、压缩、剪辑等多种视频处理操作。

## 技术栈

- **框架**: Spring Boot 2.7.0
- **Java版本**: JDK 1.8
- **云服务**: 阿里云函数计算 3.0、阿里云OSS
- **前端**: Thymeleaf模板引擎
- **构建工具**: Maven

## 主要功能

- 视频文件上传
- FFmpeg视频处理（格式转换、压缩、剪辑等）
- 阿里云OSS存储集成
- 阿里云函数计算调用
- 处理结果下载

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- 阿里云账号（用于函数计算和OSS服务）

### 安装步骤

1. 克隆项目
```bash
git clone <repository-url>
cd ffmpeg-processor-web
```

2. 配置应用参数
```bash
# 复制配置文件模板
cp src/main/resources/application.yml.template src/main/resources/application.yml
# 编辑配置文件，填入你的阿里云相关配置
```

3. 编译项目
```bash
mvn clean compile
```

4. 运行项目
```bash
mvn spring-boot:run
```

5. 访问应用
```
http://localhost:8080
```

## 配置说明

在 `src/main/resources/application.yml` 中配置以下参数：

```yaml
aliyun:
  access-key-id: your-access-key-id
  access-key-secret: your-access-key-secret
  region: cn-hangzhou
  fc:
    endpoint: your-fc-endpoint
    function-name: your-function-name
  oss:
    endpoint: your-oss-endpoint
    bucket-name: your-bucket-name
```

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/ffmpeg/web/
│   │       ├── FFmpegProcessorApplication.java  # 主启动类
│   │       ├── controller/                      # 控制器层
│   │       ├── service/                         # 服务层
│   │       ├── config/                          # 配置类
│   │       └── model/                           # 数据模型
│   └── resources/
│       ├── application.yml                      # 应用配置
│       ├── templates/                           # Thymeleaf模板
│       └── static/                              # 静态资源
└── test/                                        # 测试代码
```

## 部署

### 本地部署
```bash
mvn clean package
java -jar target/ffmpeg-processor-web-1.0-SNAPSHOT.war
```

### Docker部署
```bash
# 构建镜像
docker build -t ffmpeg-processor-web .

# 运行容器
docker run -p 8080:8080 ffmpeg-processor-web
```

## 贡献

欢迎提交Issue和Pull Request来改进这个项目。

## 许可证

本项目采用MIT许可证，详见 [LICENSE](LICENSE) 文件。

## 联系方式

如有问题或建议，请通过以下方式联系：
- 提交Issue
- 发送邮件至：[your-email@example.com]

---

**注意**: 使用前请确保已正确配置阿里云相关服务的访问权限和参数。
