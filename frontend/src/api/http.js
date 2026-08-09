import axios from 'axios'

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

export default http
