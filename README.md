# IOTGateConsole

### 介绍
IOTGateConsole是IOTGate智能网关的控制台程序，用于查看当前IOTGate CLUSTER的运行状态，执行 网关规约解析服务的启动、关闭、重启以及网关多规约策略配置等操作

### 架构变更说明（v2.0）
本版本对架构做了两项重要调整：

1. **去除 Zookeeper 依赖**：网关节点发现不再依赖 ZK 注册中心，改为**网关主动注册 + 静态配置兜底**双通道：
   - **动态注册（推荐）**：网关启动时通过 `-c -r <consoleIp>` 主动注册到本控制台（`POST /gate/register`），注册成功后每 10s 心跳上报（`POST /gate/heartbeat`），30s 未收到心跳自动判离线并移除；网关正常关闭时主动反注册（`POST /gate/unregister`）。注册成功即自动同步规约，前端节点管理页实时可见。
   - **静态兜底**：`application.properties` 中 `gate.nodes` 配置网关节点 IP 列表（支持 `ip` 或 `ip:port` 格式，默认 RPC 端口 10916），与动态注册并存，注册的节点优先、静态配置兜底。
2. **前端 Vue3 重构 + SSE 实时推送**：原 LayUI/jQuery 静态页面已重构为 Vue3 + Vite 单页应用；前端通过 SSE（`GET /rpc/events`）实时接收网关节点上下线状态、规约变更事件，替代原 ZK 事件监听机制。

### 节点在线状态判断逻辑
节点列表中的"在线/离线"状态由 **NodeMonitorService 定时 TCP 探测**得出（每 10s 一次，探测节点 10916 RPC 端口，2s 超时），**动态注册节点与静态配置节点完全一致，无差别对待**：
- 连接成功 → 在线；连接失败/超时 → 离线
- 状态变化时通过 SSE `node-status` 事件实时推送前端更新
- **静态配置节点**虽无心跳上报（"最近心跳/在线时长"列显示 `-`），但其在线状态同样是真实探测结果：静态配置本机 `127.0.0.1` 且网关运行中 → 显示在线；静态配置不存在的 IP → 一直显示离线
- **动态注册节点**在线状态由注册表心跳（30s 超时移除）与 TCP 探测双重保障

### 智能体模式（v2.2 悬浮机器人）

控制台内置 **🤖 悬浮智能体机器人**（右下角，主流客服交互设计），集**对话解析**与**大模型配置管理**于一体：

**🗣️ 对话解析**：粘贴通信协议的**帧结构描述**（协议文档中的字段定义：帧头、各字段字节数、**长度域位置与定义**、字节序、校验帧尾等），AI 自动提取长度域信息，推导出网关拆包/黏包解码参数（大小端/起始符/长度域偏移/长度域长度/长度含长度域标志/额外长度/端口），并支持**一键填充**到新增规约表单：

![智能体对话解析](docs/screenshots/bot-chat-v2.2.png)

**⚙️ 大模型配置动态设置**：**厂商无关**，支持任意 OpenAI 兼容接口（DeepSeek / 通义千问 / 智谱GLM / Ollama / OpenAI 等）。点击机器人右上角 ⚙️ 可视化修改模型地址、模型名称、API Key、采样温度、超时时间，**保存立即生效，无需重启**，配置持久化本地（重启后保留）：

![模型设置面板](docs/screenshots/bot-settings-v2.2.png)

**📡 节点管理**：实时展示网关节点列表（动态注册 + 静态配置），TCP 探测在线状态，SSE 实时推送上下线；运行规约、最近心跳、在线时长一目了然：

![节点管理页](docs/screenshots/node-manage-v2.2.png)

![规约管理页](docs/screenshots/strategy-page-v2.png)

技术栈：**LangChain4j 1.18**（AiServices 声明式结构化输出，Java 8 老项目平滑升级至 **JDK 21 + Spring Boot 3.5**）

- 后端接口：
  - `POST /rpc/ai/parse` 解析帧结构描述
  - `GET /rpc/ai/config` 获取大模型配置（API Key 脱敏返回）
  - `POST /rpc/ai/config` 动态更新大模型配置（即时生效）
- 默认配置（application.properties / 环境变量，运行时可在前端 ⚙️ 设置中覆盖）：
  - `ai.api-key=${DEEPSEEK_API_KEY:}` 建议环境变量注入，勿硬编码提交
  - `ai.base-url=https://api.deepseek.com/v1` 兼容端点
  - `ai.model=deepseek-chat`
  - `ai.temperature=0.1` / `ai.timeout-seconds=60`
- 常见模型切换示例：
  - DeepSeek：`https://api.deepseek.com/v1` + `deepseek-chat`
  - 通义千问：`https://dashscope.aliyuncs.com/compatible-mode/v1` + `qwen-plus`
  - 智谱GLM：`https://open.bigmodel.cn/api/paas/v4` + `glm-4`
  - Ollama 本地：`http://localhost:11434/v1` + 本地模型名（无需 Key）

### 智能体版本重要更新说明（v2.0 → v2.1 → v2.2）

本仓库在 v2.0 基础上完成了 **从"无 AI"到"智能体全功能版"** 的三级演进，各版本关键差异如下：

| 版本 | 核心能力 | 智能体形态 | 技术栈 |
|---|---|---|---|
| **v2.0** | 去 Zookeeper、Vue3 重构、SSE 实时推送 | ❌ 无 | Spring Boot 2.x + JDK 8 |
| **v2.1** | 新增智能体模式：AI 解析协议帧结构、自动填充规约表单 | 页面内嵌面板 | LangChain4j 初版集成 |
| **v2.2（当前）** | 悬浮机器人 + 大模型配置动态化 + 节点/规约管理增强 | 右下角悬浮机器人 | JDK 21 + Spring Boot 3.5 + LangChain4j 1.18 |

**v2.2 相比 v2.1 的重要更新：**

1. **🪄 交互形态升级**：智能体由页面内嵌面板改为**右下角悬浮机器人**（主流客服交互设计，带呼吸脉冲动画），随时呼出、随时收起，不占页面空间
2. **⚙️ 大模型配置动态化**：模型地址/模型名称/API Key/温度/超时**可视化修改、保存立即生效、无需重启**，配置持久化本地；v2.1 只能改配置文件重启生效
3. **🌐 厂商无关化**：支持任意 OpenAI 兼容接口（DeepSeek / 通义千问 / 智谱GLM / Ollama / OpenAI），v2.1 仅内置 DeepSeek 单一厂商
4. **🖥️ 技术栈升级**：JDK 8 → **21**、Spring Boot 2.x → **3.5**、LangChain4j 规范化重构（AiServices 声明式结构化输出），性能与可维护性全面提升
5. **🎨 界面焕新**：登录页升级为 AI 智能体版科技风格（突出百万设备长连接与多规约接入）、页面新增 v2.2 版本标识、悬浮机器人图标由 emoji 替换为 **SVG 矢量图**（修复无 emoji 字体环境下显示为 × 的问题）
6. **📡 运行管理增强**：规约启停管理落地、节点管理页增强（注册来源/最近心跳/在线时长/运行规约展示）、网关主动注册机制（动态发现 + 静态配置兜底）

> 注：v2.1 → v2.2 为增量演进，v2.1 的 AI 解析帧结构、一键填充规约表单等核心能力在 v2.2 中原样保留并增强。

### 环境要求
jdk21（Temurin 21 LTS 及以上）、mysql5.5+ 以及 IOTGate 节点

### 运行方式
1. 搭建 mysql 服务并导入 `src/main/resources/strategy.sql` 建表
2. 在 `src/main/resources/application.properties` 中配置数据库参数和 `gate.nodes` 网关节点列表
3. 执行 `mvn package` 打成可执行 jar 包，启动 jar 包，默认端口为 8686
4. 访问 http://127.0.0.1:8686/static/index.html ，首次访问会跳转到登录页，用户名密码随意填写（没有存库！）

![新版登录页](docs/screenshots/login-v2.2-new.png)

5. 前端源码位于 `frontend/` 目录（Vue3 + Vite），如需二次开发：`cd frontend && npm install && npm run dev`（开发模式代理 /rpc 到 8686）；构建产物已集成到 `src/main/resources/static/`

### GATE CLUSTER 结构图
![集群版IOTGate架构](https://images.gitee.com/uploads/images/2019/0402/194105_f06b6623_1038477.png "IOTGate整体架构图.png")
GATE Console 是一个web工程，用户登录之后可以查看当前GATE CLUSTER的运行状态监控，执行网关重启、关闭、启动，网关多规约支持策略等操作（目前不考虑监控网关服务器状态功能）

### 入口类

	IotGateConsoleApplication

### 接口说明
- `POST /gate/register` 网关主动注册(网关 -c -r 模式启动时调用，注册成功即同步规约)
- `POST /gate/heartbeat` 网关心跳上报(默认每10s一次；节点不存在时返回 retSig=404 触发网关重新注册)
- `POST /gate/unregister` 网关反注册(正常关闭时调用)
- `GET /gate/nodes` 查看当前注册表中的节点
- `POST /rpc/gateData` 获取所有网关节点信息(含运行规约)
- `POST /rpc/addOneStrategy` 新增规约(表单格式 data[pid]=xx&data[straName]=xx...)
- `POST /rpc/getAllStrategeFromDB` 获取所有规约名称与编号
- `POST /rpc/getAllStrategyAllInfo` 获取所有规约完整信息
- `POST /rpc/updateStrategyNode` 更新网关节点启用的规约
- `POST /rpc/delOneStrategyByPID` 删除规约(str=pid)
- `POST /rpc/ai/parse` 智能体解析帧结构描述
- `GET /rpc/ai/config` 获取大模型配置(Key脱敏)
- `POST /rpc/ai/config` 动态更新大模型配置(即时生效)
- `GET /rpc/events` SSE 事件流(节点状态/规约变更实时推送)

### 交流群
- QQ群：844082385
