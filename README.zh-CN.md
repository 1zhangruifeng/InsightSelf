# InsightSelf 2.0

[English](README.md) | [简体中文](README.zh-CN.md)

> 一个中英双语 Android 自我认知平台，将人格测评、八字、现代占星、每日反思和 AI 辅助报告整合为连贯的使用体验。

InsightSelf 关注一个常见问题：年轻人对人格类型、星座内容和自我认知有很高的兴趣，但相关结果通常分散在不同应用、短视频、截图和一次性测试中。

InsightSelf 将这些碎片化内容整理成结构化的自我反思旅程。用户只需建立一个统一档案，即可使用多个反思模块、保存历史结果并生成带有明确安全边界的综合总结。本产品用于自我反思和沟通，不提供诊断，也不进行确定性预测。

## 项目亮点

- **一个档案，多个模块**：出生信息、语言和偏好只需填写一次，即可在整个应用中复用。
- **中英双语体验**：主要操作流程和生成内容支持英文与简体中文。
- **结构化自我反思**：八字、星座、测评、每日洞察和报告相互关联，而不是彼此孤立。
- **常见测评模块**：包含 IPIP 风格大五人格、MBTI 风格偏好、O*NET Mini 职业兴趣、WHO-5、Rosenberg 自尊量表和依恋反思。
- **AI 完全可选**：可以配置 Qwen 生成更丰富的综合报告；不配置付费 API 时，本地模板仍可完成核心演示。
- **注重演示隐私**：内置演示账号只包含虚构演示数据，不包含真实个人信息。
- **便于本地复现**：使用 Spring Boot、SQLite 和 Android 模拟器即可运行完整原型。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| Android 应用 | Kotlin、Jetpack Compose、Material 3、Navigation Compose、Retrofit、OkHttp、DataStore |
| 应用架构 | MVVM 风格 ViewModel、Repository 层、REST API |
| 后端 | Java 21、Spring Boot 3.3、Spring Security、Spring Data JPA |
| 数据库 | 默认 SQLite、Flyway 迁移、可选 MySQL 配置 |
| 领域计算 | `6tail/lunar-java`、Swiss Ephemeris Java |
| 报告生成 | 本地模板或可选 Qwen 兼容 API |
| 测试 | Spring Boot 集成测试、JUnit、MockWebServer |

## 项目结构

```text
InsightSelf/
|-- android/                 # 原生 Android 应用
|-- backend/                 # Spring Boot REST API
|-- docs/                    # API、数据库、测试和演示文档
|-- insightself_mockups/     # UI 设计稿
|-- ANDROID_TESTING.md       # Android 测试说明
|-- THIRD_PARTY_NOTICES.md   # 第三方依赖和测评工具声明
|-- LICENSE
|-- README.md                # 默认英文说明
`-- README.zh-CN.md          # 中文说明
```

## 快速启动

最简单的运行结构如下：

```text
Windows/macOS/Linux 电脑
|-- Spring Boot 后端：http://localhost:8080
`-- Android Studio 模拟器：http://10.0.2.2:8080
```

默认演示不需要安装 MySQL，也不需要配置 AI Key。

### 1. 环境要求

请先安装：

- **JDK 21**
- **Maven 3.8 或更高版本**
- **Android Studio**，并安装 Android SDK Platform 35 和 Android 模拟器
- Git

项目已经包含 Gradle Wrapper，无需单独安装 Gradle。

检查命令行环境：

```powershell
java -version
mvn -version
git --version
```

如果尚未安装 Android SDK Platform 35，请在 Android Studio 中打开 `Tools > SDK Manager` 完成安装。

### 2. 克隆仓库

在 GitHub 仓库页面点击 **Code**，复制 HTTPS 地址，然后执行：

```powershell
git clone <repository-url>
cd InsightSelf
```

### 3. 启动后端

在项目根目录打开终端：

```powershell
cd backend
mvn spring-boot:run
```

等待控制台显示 Tomcat 已在 `8080` 端口启动，然后在浏览器中访问：

```text
http://localhost:8080/api/health
```

首次运行时，后端会自动创建并迁移本地数据库：

```text
backend/insightself.sqlite
```

该运行时数据库已被 Git 忽略，不会上传到仓库。

### 4. 启动 Android 应用

1. 打开 Android Studio。
2. 选择 **Open**，打开仓库中的 `android/` 文件夹。
3. 等待 Gradle Sync 完成。
4. 启动 Android 模拟器，建议使用 API 33 或更高版本。
5. 选择 `app` 运行配置并点击 **Run**。

模拟器版本默认通过以下地址连接本地后端：

```text
http://10.0.2.2:8080/
```

`10.0.2.2` 是 Android 模拟器访问宿主电脑 `localhost` 的专用地址。

### 5. 进入演示

在登录页面使用内置演示入口。后端会创建或刷新演示账号，并自动完成登录。

演示账号包含虚构的用户档案、已完成的测评示例、八字和星座数据、首页内容以及综合报告，**不包含任何真实个人信息**。

也可以注册新账号并亲自完成完整的新用户流程。

## 命令行构建与测试

### 后端

运行全部后端测试：

```powershell
cd backend
mvn clean test
```

构建后端程序包：

```powershell
cd backend
mvn clean package
```

运行打包后的程序：

```powershell
java -jar target/insightself-backend-0.0.1-SNAPSHOT.jar
```

### Android

在 Windows 上构建 Debug APK：

```powershell
cd android
.\gradlew.bat :app:assembleDebug
```

运行 Android 单元测试：

```powershell
cd android
.\gradlew.bat :app:testDebugUnitTest
```

Debug APK 生成在：

```text
android/app/build/outputs/apk/debug/
```

macOS 或 Linux 用户请将 `.\gradlew.bat` 替换为 `./gradlew`。

## 可选 AI 配置

不连接外部 AI 服务也可以完整演示主要功能。未设置 `QWEN_API_KEY` 时，综合报告使用本地 `TEMPLATE` 生成器，AI 对话则会明确提示服务尚未配置。

如需启用 Qwen，请在启动后端之前设置环境变量：

```powershell
$env:QWEN_API_KEY = "your-api-key"
$env:QWEN_MODEL = "qwen-plus"
$env:QWEN_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"
mvn spring-boot:run
```

不要将 API Key 写入源码，也不要将其提交到 GitHub。

## 可选 MySQL 配置

本地使用建议保留默认 SQLite。如果需要改用 MySQL，请先创建空的 `insightself` 数据库，然后执行：

```powershell
$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:MYSQL_URL = "jdbc:mysql://localhost:3306/insightself?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:MYSQL_USERNAME = "root"
$env:MYSQL_PASSWORD = "your-password"
cd backend
mvn spring-boot:run
```

Flyway 会根据 SQLite、MySQL 或测试使用的 H2 数据库加载对应迁移脚本。

## 使用 Android 实体设备

默认 Android 配置面向模拟器，实体手机无法使用 `10.0.2.2`。

使用实体设备进行开发时：

1. 将手机和电脑连接到同一个网络。
2. 查找电脑的局域网 IPv4 地址，例如 `192.168.1.20`。
3. 将 `android/app/src/main/java/com/example/insightself/data/api/RetrofitClient.kt` 中的 `BASE_URL` 改为 `http://192.168.1.20:8080/`。
4. Windows 防火墙提示时，允许 Java/Spring Boot 访问网络。
5. 重新构建并运行应用。

正式发布时应使用 HTTPS 后端，并关闭明文 HTTP 通信。

## 产品流程

1. 注册账号或载入内置演示账号。
2. 创建统一个人档案。
3. 查看首页综合洞察和每日反思。
4. 探索八字五行结构。
5. 查看星座、本命盘位置和每日相位。
6. 完成结构化测评并查看报告。
7. 生成综合报告，并导出或分享。
8. 在个人页面切换英文和简体中文。

## 主要 API

```text
GET  /api/health             公开健康检查
POST /api/users/register     注册
POST /api/users/login        登录
POST /api/demo/seed          创建并登录内置演示账号
GET  /api/profiles/{userId}  统一个人档案
GET  /api/dashboard/{userId} 综合首页
POST /api/bazi/generate/{userId}
GET  /api/zodiac/daily/{userId}
GET  /api/assessments/types/{userId}
POST /api/ai-reports/generate/{userId}
POST /api/ai/chat            上下文 AI 对话
```

详细 API 说明见 [`docs/api_overview.md`](docs/api_overview.md)，数据模型见 [`docs/database_schema.md`](docs/database_schema.md)。

## 安全边界

InsightSelf 是一款用于教育和自我反思的产品。其中的八字、星座、关系、测评、AI 和报告功能：

- 不提供医学或心理诊断；
- 不能替代专业建议；
- 不主张确定性预测；
- 应被理解为促进反思和沟通的提示；
- 在真实部署时需要妥善处理出生信息和测评数据。

MBTI 风格模块用于提供熟悉的偏好反思体验，**并非**受版权保护的官方 MBTI 测评。依赖、数据集和问卷说明见 [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md)。

## 常见问题

### Android 显示网络或服务器错误

- 确认后端终端仍在运行。
- 在电脑浏览器打开 `http://localhost:8080/api/health`。
- 确认模拟器使用 `http://10.0.2.2:8080/`，而不是 `localhost`。
- 后端启动完成后重新打开应用。

### 8080 端口被占用

停止正在使用 `8080` 端口的其他程序。如果将后端改到其他端口，也必须同步修改 Android 的后端地址。

### Android Studio 找不到 SDK

应当将 `android/` 作为 Android Studio 项目打开。Android Studio 通常会自动创建 `android/local.properties`。使用命令行构建时，请设置 `ANDROID_HOME` 或 `ANDROID_SDK_ROOT`。

### Windows 单元测试出现 `GradleWorkerMain`

如果 APK 可以构建，但 Gradle 测试进程找不到 `GradleWorkerMain`，请将 `GRADLE_USER_HOME` 设置为简短且只包含 ASCII 字符的路径。这种情况可能由 Windows 用户目录中的字符编码导致。

```powershell
$env:GRADLE_USER_HOME = "C:\gradle-home"
.\gradlew.bat --no-daemon --max-workers=1 :app:testDebugUnitTest
```

### 旧版本数据库迁移失败

先备份需要的数据，停止后端，删除本地且已被 Git 忽略的 `backend/insightself.sqlite`，再重新启动。Flyway 会自动创建新的数据库结构。

### 报告显示 `TEMPLATE`

未配置 Qwen Key 时这是正常现象。本地模板是用于离线兼容的正式降级方案，并非错误。

## 更多文档

- [`docs/demo_script.md`](docs/demo_script.md)：产品演示流程
- [`docs/testing_notes.md`](docs/testing_notes.md)：测试范围与验证说明
- [`ANDROID_TESTING.md`](ANDROID_TESTING.md)：Android 测试说明
- [`IMPLEMENTATION_PLAN.md`](IMPLEMENTATION_PLAN.md)：项目实现背景
- [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md)：第三方声明

## 上传 GitHub 前

仓库已配置为忽略本地数据库、IDE 设置、构建产物、API 环境文件和 Android SDK 本地路径。上传前仍应检查暂存文件，确认其中没有密钥或个人数据：

```powershell
git status
git diff --cached
```

推荐的 GitHub topics：

```text
android  kotlin  jetpack-compose  spring-boot  sqlite  self-reflection  wellness
```

## 许可证

本项目使用 **AGPL-3.0-or-later** 许可证。重新分发或部署前，请阅读 [`LICENSE`](LICENSE) 和 [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md)。
