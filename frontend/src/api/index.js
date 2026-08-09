import axios from 'axios'
import { ElMessage } from 'element-plus'

// 统一 axios 实例
const http = axios.create({
  baseURL: '',
  timeout: 15000
})

http.interceptors.response.use(
  (resp) => resp.data,
  (err) => {
    ElMessage.error(err.message || '请求失败')
    return Promise.reject(err)
  }
)

/**
 * 以 application/x-www-form-urlencoded 格式发送 POST
 * （原系统后端为 Spring MVC 表单绑定，data 为 Map 类型，
 *   需传 data[field]=value 格式，与旧版 jQuery $.post 行为一致）
 */
function postForm(url, params = {}) {
  const body = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value && typeof value === 'object' && !Array.isArray(value)) {
      // 嵌套对象：序列化为 data[pid]=5 形式
      for (const [k, v] of Object.entries(value)) {
        body.append(`${key}[${k}]`, v)
      }
    } else if (Array.isArray(value)) {
      // 数组：dataList[0]=1&dataList[1]=2
      value.forEach((item, idx) => body.append(`${key}[${idx}]`, item))
    } else if (value !== undefined && value !== null) {
      body.append(key, value)
    }
  }
  return http.post(url, body, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
  })
}

export default http

// 获取所有网关节点信息(含运行规约)
export function getGateData() {
  return http.post('/rpc/gateData')
}

// 获取所有规约(仅名称和编号)
export function getAllStrategeFromDB() {
  return http.post('/rpc/getAllStrategeFromDB')
}

// 获取所有规约完整信息
export function getAllStrategyAllInfo() {
  return http.post('/rpc/getAllStrategyAllInfo')
}

// 新增规约
export function addOneStrategy(data) {
  return postForm('/rpc/addOneStrategy', { data })
}

// 删除规约
export function delOneStrategyByPID(pid) {
  return postForm('/rpc/delOneStrategyByPID', { str: pid })
}

// 更新节点启用的规约
export function updateStrategyNode(ip, pidList) {
  return postForm('/rpc/updateStrategyNode', { str: ip, dataList: pidList })
}

// 智能体模式：解析规约帧结构
export function aiParseProtocol(frameDesc) {
  return http.post('/rpc/ai/parse', { frameDesc })
}

// 获取大模型配置（api-key 脱敏）
export function getAiConfig() {
  return http.get('/rpc/ai/config')
}

// 保存大模型配置（动态生效）
export function saveAiConfig(data) {
  return http.post('/rpc/ai/config', data)
}
