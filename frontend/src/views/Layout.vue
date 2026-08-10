<template>
  <el-container class="layout">
    <el-aside width="200px" class="aside">
      <div class="logo">IOTGateConsole</div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#1f3b73"
        text-color="#cfd8ea"
        active-text-color="#ffffff"
        class="side-menu"
      >
        <el-menu-item index="/nodes">
          <el-icon><Monitor /></el-icon>
          <span>节点管理</span>
        </el-menu-item>
        <el-menu-item index="/strategies">
          <el-icon><Document /></el-icon>
          <span>规约管理</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <BotIcon :size="16" />
        <div class="ver-meta">
          <span class="ver-main">AI 智能体版</span>
          <span class="ver-sub">v2.2</span>
        </div>
      </div>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="header-title">{{ $route.meta.title }}</span>
        <div class="header-right">
          <el-tag v-if="sseConnected" type="success" size="small" effect="dark">SSE 已连接</el-tag>
          <el-tag v-else type="info" size="small" effect="dark">SSE 重连中…</el-tag>
          <el-dropdown @command="handleCommand">
            <span class="user-name">{{ username }}<el-icon><ArrowDown /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>

    <!-- 全局悬浮智能体机器人（对话 + 模型设置） -->
    <FloatingBot />
  </el-container>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Monitor, Document, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import BotIcon from '../components/BotIcon.vue'
import FloatingBot from '../components/FloatingBot.vue'

const router = useRouter()
const username = ref('')
const sseConnected = ref(false)

let eventSource = null

onMounted(() => {
  const login = JSON.parse(localStorage.getItem('iotgate_login') || '{}')
  username.value = login.username || '用户'
  connectSSE()
})

onBeforeUnmount(() => {
  if (eventSource) eventSource.close()
})

function connectSSE() {
  eventSource = new EventSource('/rpc/events')
  eventSource.onopen = () => { sseConnected.value = true }
  eventSource.onerror = () => { sseConnected.value = false }
  // 节点状态与规约变更事件由各子页面自行监听，
  // 这里通过自定义事件转发，供 NodeManage/StrategyManage 接收
  eventSource.addEventListener('node-status', (e) => {
    window.dispatchEvent(new CustomEvent('iotgate-node-status', { detail: JSON.parse(e.data) }))
  })
  eventSource.addEventListener('strategy-change', (e) => {
    window.dispatchEvent(new CustomEvent('iotgate-strategy-change', { detail: JSON.parse(e.data) }))
  })
  eventSource.addEventListener('snapshot', (e) => {
    window.dispatchEvent(new CustomEvent('iotgate-node-snapshot', { detail: JSON.parse(e.data) }))
  })
}

function handleCommand(cmd) {
  if (cmd === 'logout') {
    localStorage.removeItem('iotgate_login')
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside {
  background: #1f3b73;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 56px;
  line-height: 56px;
  text-align: center;
  color: #fff;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}
.aside :deep(.el-menu) { border-right: none; flex: 1; }
.sidebar-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  color: #9db4e8;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
}
.ver-meta { display: flex; flex-direction: column; line-height: 1.25; }
.ver-main { font-size: 12.5px; color: #e8eefc; font-weight: 600; }
.ver-sub { font-size: 11px; color: #8fa5d8; letter-spacing: 1px; }
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}
.header-title { font-size: 16px; font-weight: 600; color: #333; }
.header-right { display: flex; align-items: center; gap: 12px; }
.user-name {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #333;
  cursor: pointer;
  font-size: 14px;
}
.main { background: #f0f2f5; padding: 16px; }
</style>
