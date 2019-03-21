# IOTGateConsole

### 介绍
IOTGateConsole是IOTGate智能网关的控制台程序，用于查看当前GATE CLUSTER的运行状态，执行 网关启动、关闭、重启以及网关多规约策略配置等操作

### 环境
zookeeper集群环境  jdk1.8

### 软件架构
通过zookeeper动态发现网关，并通过调用网关的rpc服务执行操作网关各方法
	
### GATE CLUSTER 结构图
![集群版IOTGate架构](https://images.gitee.com/uploads/images/2019/0125/162345_24e4fa28_1038477.png "绘图1.png")
GATE CLIENT 是一个web工程，用户登录之后可以查看当前GATE CLUSTER的运行状态监控，网关重启、关闭、启动，网关多规约支持策略等操作（目前不考虑监控网关服务器状态功能）
### 联系方式
邮箱：1012702024@qq.com
