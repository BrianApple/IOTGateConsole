<template>
  <div class="login-page">
    <!-- 背景装饰层 -->
    <div class="bg-grid"></div>
    <div class="bg-glow glow-a"></div>
    <div class="bg-glow glow-b"></div>
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>

    <div class="login-wrap">
      <!-- 左侧品牌区 -->
      <div class="brand">
        <div class="brand-bot">
          <BotIcon :size="76" />
        </div>
        <h1 class="brand-title">IOTGate 智能网关</h1>
        <p class="brand-sub">AI 智能体版 · v2.2</p>
        <p class="brand-desc">
          AI 智能体版物联网通信网关，支撑<strong>百万级设备长连接</strong>，
          <strong>多规约物联网设备同时接入管理</strong>；粘贴协议帧结构描述，
          大模型自动推导拆包/黏包解码参数，让规约接入更简单。
        </p>
        <div class="brand-feats">
          <div class="feat">
            <el-icon :size="16"><MagicStick /></el-icon>
            <span>AI 智能体对话解析协议帧结构</span>
          </div>
          <div class="feat">
            <el-icon :size="16"><Connection /></el-icon>
            <span>百万级设备长连接承载</span>
          </div>
          <div class="feat">
            <el-icon :size="16"><Document /></el-icon>
            <span>多规约设备同时接入管理</span>
          </div>
          <div class="feat">
            <el-icon :size="16"><Monitor /></el-icon>
            <span>网关节点动态发现与实时监控</span>
          </div>
        </div>
      </div>

      <!-- 右侧登录卡片 -->
      <div class="login-card">
        <div class="card-head">
          <div class="card-bot"><BotIcon :size="30" /></div>
          <h2 class="card-title">登录控制台</h2>
          <p class="card-sub">IOTGateConsole · AI 智能体版 v2.2</p>
        </div>
        <el-form :model="form" @keyup.enter="doLogin">
          <el-form-item>
            <el-input v-model="form.username" placeholder="用户名（随意填写）" size="large" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" placeholder="密码（随意填写）" size="large" show-password />
          </el-form-item>
          <el-button type="primary" size="large" class="login-btn" @click="doLogin">
            登 录
          </el-button>
        </el-form>
        <p class="login-tip">开源版演示：用户名密码随意输入即可登录</p>
      </div>
    </div>

    <div class="login-footer">IOTGate v2.2 · 智能物联网通信网关 · GPL-2.0</div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Monitor, Document, MagicStick, Connection } from '@element-plus/icons-vue'
import BotIcon from '../components/BotIcon.vue'

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
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0b1a3f 0%, #1f3b73 45%, #2d6a9f 100%);
  overflow: hidden;
}

/* ===== 背景装饰 ===== */
.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 44px 44px;
  -webkit-mask-image: radial-gradient(ellipse at 50% 40%, #000 30%, transparent 75%);
  mask-image: radial-gradient(ellipse at 50% 40%, #000 30%, transparent 75%);
}
.bg-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.5;
  pointer-events: none;
}
.glow-a {
  width: 520px;
  height: 520px;
  background: radial-gradient(circle, rgba(64, 128, 255, 0.55), transparent 70%);
  top: -160px;
  left: -120px;
}
.glow-b {
  width: 460px;
  height: 460px;
  background: radial-gradient(circle, rgba(64, 210, 255, 0.4), transparent 70%);
  bottom: -140px;
  right: -100px;
}
.orb {
  position: absolute;
  border-radius: 50%;
  background: rgba(120, 180, 255, 0.4);
  filter: blur(1px);
  animation: float 8s ease-in-out infinite;
  pointer-events: none;
}
.orb-1 { width: 8px; height: 8px; top: 22%; left: 12%; }
.orb-2 { width: 5px; height: 5px; top: 66%; left: 68%; animation-delay: 2s; }
.orb-3 { width: 6px; height: 6px; top: 38%; right: 14%; animation-delay: 4s; }
@keyframes float {
  0%, 100% { transform: translateY(0); opacity: 0.6; }
  50% { transform: translateY(-18px); opacity: 1; }
}

/* ===== 主布局 ===== */
.login-wrap {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 72px;
  max-width: 1000px;
  width: 100%;
  padding: 0 40px;
}

/* ===== 品牌区 ===== */
.brand { flex: 1.1; color: #fff; }
.brand-bot {
  width: 108px;
  height: 108px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 26px;
  background: linear-gradient(135deg, rgba(94, 146, 255, 0.35), rgba(45, 106, 159, 0.25));
  border: 1px solid rgba(140, 180, 255, 0.4);
  box-shadow: 0 0 40px rgba(70, 130, 255, 0.45), inset 0 0 24px rgba(120, 180, 255, 0.25);
  margin-bottom: 26px;
}
.brand-title { font-size: 34px; margin: 0 0 10px; letter-spacing: 1px; }
.brand-sub {
  display: inline-block;
  font-size: 13px;
  color: #bcd3ff;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(160, 195, 255, 0.35);
  border-radius: 20px;
  padding: 4px 14px;
  margin: 0 0 18px;
  letter-spacing: 2px;
}
.brand-desc {
  font-size: 14px;
  color: #c7d6f5;
  line-height: 1.9;
  max-width: 440px;
  margin: 0 0 26px;
}
.brand-desc strong {
  color: #7db4ff;
  font-weight: 600;
}
.brand-feats { display: flex; flex-direction: column; gap: 12px; }
.feat {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13.5px;
  color: #dbe7ff;
}
.feat .el-icon { color: #7db4ff; }

/* ===== 登录卡片 ===== */
.login-card {
  width: 400px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.94);
  border-radius: 18px;
  padding: 34px 32px 22px;
  box-shadow: 0 24px 64px rgba(4, 12, 40, 0.45);
}
.card-head { text-align: center; margin-bottom: 26px; }
.card-bot {
  width: 56px;
  height: 56px;
  margin: 0 auto 14px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3b6fd4, #1f3b73);
  color: #fff;
  box-shadow: 0 8px 20px rgba(31, 59, 115, 0.35);
}
.card-title { font-size: 20px; margin: 0 0 4px; color: #1f3b73; }
.card-sub { font-size: 12px; color: #93a1bd; margin: 0; letter-spacing: 0.5px; }
.login-btn {
  width: 100%;
  font-size: 15px;
  letter-spacing: 6px;
  background: linear-gradient(135deg, #3b6fd4, #1f3b73);
  border: none;
  margin-top: 4px;
}
.login-btn:hover { background: linear-gradient(135deg, #4a7fe8, #27458c); }
.login-tip {
  text-align: center;
  color: #a3aec2;
  font-size: 12px;
  margin-top: 18px;
}

.login-footer {
  position: absolute;
  bottom: 22px;
  width: 100%;
  text-align: center;
  color: rgba(210, 225, 255, 0.5);
  font-size: 12px;
  letter-spacing: 1px;
  z-index: 2;
}

@media (max-width: 880px) {
  .brand { display: none; }
  .login-wrap { justify-content: center; padding: 0 20px; }
}
</style>
