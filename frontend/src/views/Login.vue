<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-title">IOTGate 智能网关</h1>
      <p class="login-sub">IOTGateConsole · AI 智能体版 v2.2</p>
      <el-form :model="form" @keyup.enter="doLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名(随意填写)" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码(随意填写)" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" @click="doLogin">
          登 录
        </el-button>
      </el-form>
      <p class="login-tip">开源版演示：用户名密码随意输入即可登录</p>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const form = reactive({ username: '', password: '' })

function doLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  // 沿用原系统逻辑：登录信息仅存 localStorage，不落库
  localStorage.setItem('iotgate_login', JSON.stringify({
    username: form.username,
    time: Date.now()
  }))
  ElMessage.success('登录成功')
  router.push('/')
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f3b73 0%, #2d6a9f 50%, #3aa0c9 100%);
}
.login-card {
  width: 380px;
  padding: 40px 36px 24px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
}
.login-title {
  text-align: center;
  margin: 0;
  font-size: 26px;
  color: #1f3b73;
}
.login-sub {
  text-align: center;
  color: #999;
  margin: 6px 0 28px;
  font-size: 14px;
}
.login-tip {
  text-align: center;
  color: #bbb;
  font-size: 12px;
  margin-top: 20px;
}
</style>
