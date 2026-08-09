# IOTGateConsole

### 介绍
IOTGateConsole是IOTGate智能网关的控制台程序，用于查看当前IOTGate CLUSTER的运行状态，执行 网关规约解析服务的启动、关闭、重启以及网关多规约策略配置等操作

### 架构变更说明（v2.0）
本版本对架构做了两项重要调整：

1. **去除 Zookeeper 依赖**：网关节点发现不再依赖 ZK 注册中心，改为在 `application.properties` 中通过 `gate.nodes` 静态配置网关节点 IP 列表（支持 `ip` 或 `ip:port` 格式，默认 RPC 端口 10916）。
2. **前端 Vue3 重构 + SSE 实时推送**：原 LayUI/jQuery 静态页面已重构为 Vue3 + Vite 单页应用；前端通过 SSE（`GET /rpc/events`）实时接收网关节点上下线状态、规约变更事件，替代原 ZK 事件监听机制。

### 智能体模式（v2.1 新增）
规约管理页提供 **🤖 智能体助手**：用户只需粘贴规约的**帧结构描述**（十六进制报文示例、字段说明或文字描述均可），AI 自动推导出网关解码参数（大小端/长度域偏移/长度域长度/长度含长度域标志/额外长度/建议端口等），并支持**一键填充**到新增规约表单。

- 后端接口：`POST /rpc/ai/parse`，基于 DeepSeek 大模型
- 配置（application.properties 或环境变量）：
  - `ai.api-key`：DeepSeek API Key（建议通过环境变量 `DEEPSEEK_API_KEY` 注入，勿硬编码提交）
  - `ai.base-url`：默认 `https://api.deepseek.com`
  - `ai.model`：默认 `deepseek-chat`
- 使用示例：在规约管理页点击"🤖 智能体助手"，粘贴如 `68 11 22 33 44 55 66 68 01 02 12 34 56 78 16`（DL/T645电表帧），AI 会返回完整解码参数

### 环境要求
jdk1.8、mysql5.5+ 以及 IOTGate 节点

### 运行方式
1. 搭建 mysql 服务并导入 `src/main/resources/strategy.sql` 建表
2. 在 `src/main/resources/application.properties` 中配置数据库参数和 `gate.nodes` 网关节点列表
3. 执行 `mvn package` 打成可执行 jar 包，启动 jar 包，默认端口为 8686
4. 访问 http://127.0.0.1:8686/rpc/index 或 http://127.0.0.1:8686/static/index.html ，首次访问会跳转到登录页，用户名密码随意填写（没有存库！）
5. 前端源码位于 `frontend/` 目录（Vue3 + Vite），如需二次开发：`cd frontend && npm install && npm run dev`（开发模式代理 /rpc 到 8686）；构建产物已集成到 `src/main/resources/static/`

### GATE CLUSTER 结构图
![集群版IOTGate架构](https://images.gitee.com/uploads/images/2019/0402/194105_f06b6623_1038477.png "IOTGate整体架构图.png")
GATE Console 是一个web工程，用户登录之后可以查看当前GATE CLUSTER的运行状态监控，执行网关重启、关闭、启动，网关多规约支持策略等操作（目前不考虑监控网关服务器状态功能）

### 入口类
	IotGateConsoleApplication

### 接口说明
- `POST /rpc/gateData` 获取所有网关节点信息(含运行规约)
- `POST /rpc/addOneStrategy` 新增规约(表单格式 data[pid]=xx&data[straName]=xx...)
- `POST /rpc/getAllStrategeFromDB` 获取所有规约名称与编号
- `POST /rpc/getAllStrategyAllInfo` 获取所有规约完整信息
- `POST /rpc/updateStrategyNode` 更新网关节点启用的规约
- `POST /rpc/delOneStrategyByPID` 删除规约(str=pid)
- `GET /rpc/events` SSE 事件流(节点状态/规约变更实时推送)

### 交流群
- QQ群：844082385
