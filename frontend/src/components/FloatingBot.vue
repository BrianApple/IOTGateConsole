<template>
  <div class="ai-bot">
    <!-- 悬浮按钮 -->
    <transition name="fab">
      <div v-if="!open" class="bot-fab" @click="togglePanel">
        <div class="fab-pulse"></div>
        <div class="fab-icon"><BotIcon :size="30" /></div>
      </div>
    </transition>

    <!-- 悬浮面板 -->
    <transition name="panel">
      <div v-if="open" class="bot-panel">
        <!-- 头部 -->
        <div class="bot-header">
          <div class="bot-title">
            <div class="bot-avatar"><BotIcon :size="20" /></div>
            <div class="bot-meta">
              <div class="bot-name">规约解析智能体 <span class="bot-ver">v2.2</span></div>
              <div class="bot-status">
                <span class="dot" :class="{ ok: cfgReady }"></span>
                {{ cfgReady ? '在线 · ' + cfg.model : '未配置模型' }}
              </div>
            </div>
          </div>
          <div class="bot-actions">
            <el-tooltip content="模型设置" placement="bottom">
              <button class="icon-btn" @click="panel = panel === 'chat' ? 'settings' : 'chat'">
                <el-icon :size="16"><Setting /></el-icon>
              </button>
            </el-tooltip>
            <el-tooltip content="关闭" placement="bottom">
              <button class="icon-btn" @click="open = false">
                <el-icon :size="16"><Close /></el-icon>
              </button>
            </el-tooltip>
          </div>
        </div>

        <!-- 对话面板 -->
        <template v-if="panel === 'chat'">
          <div class="chat-box" ref="chatBox">
            <div class="msg msg-ai">
              <div class="avatar"><BotIcon :size="15" /></div>
              <div class="bubble">
                你好，我是规约解析智能体<br />
                粘贴通信协议的<strong>帧结构描述</strong>（字段定义：帧头、各字段字节数、长度域位置与定义、字节序、校验帧尾等），
                我会提取长度域信息推导出<strong>拆包/黏包解码参数</strong>，并可一键填入新增规约表单。
                <br /><br />
                示例：<code>帧头AA55(2字节) + 长度域(2字节,小端,长度值含长度域自身) + 命令字(1字节) + 数据域 + CRC16(2字节)</code>
              </div>
            </div>

            <div v-for="(m, i) in messages" :key="i" :class="['msg', m.role === 'user' ? 'msg-user' : 'msg-ai']">
              <div class="avatar">
                <BotIcon v-if="m.role === 'ai'" :size="15" />
                <el-icon v-else :size="15"><User /></el-icon>
              </div>
              <div class="bubble">
                <pre v-if="m.type === 'json'">{{ prettyJson(m.content) }}</pre>
                <div v-else>{{ m.content }}</div>
                <div v-if="m.type === 'json' && !m.filled" class="fill-btn">
                  <el-button type="primary" size="small" round @click="fillForm(m.content)">
                    一键填充到新增规约表单
                  </el-button>
                </div>
                <div v-else-if="m.filled" class="fill-tip">已跳转规约管理，表单已填充</div>
              </div>
            </div>

            <div v-if="loading" class="msg msg-ai">
              <div class="avatar"><BotIcon :size="15" /></div>
              <div class="bubble loading">
                <span class="dot-anim" v-for="n in 3" :key="n"></span>
              </div>
            </div>
          </div>

          <div class="chat-input">
            <el-input
              v-model="input"
              type="textarea"
              :rows="2"
              resize="none"
              placeholder="粘贴帧结构描述，如：帧头68(1字节) + 长度域(2字节,大端,长度值不含自身) + 数据域 + 校验(1字节)"
              @keydown.enter.exact.prevent="send"
            />
            <div class="chat-actions">
              <el-button size="small" text @click="clearChat">清空</el-button>
              <el-button type="primary" size="small" round :loading="loading" :disabled="!input.trim()" @click="send">
                发送
              </el-button>
            </div>
          </div>
        </template>

        <!-- 设置面板 -->
        <template v-else>
          <div class="settings-box">
            <div class="settings-tip">
              提示：支持任意 OpenAI 兼容接口，修改后<strong>立即生效，无需重启</strong>。
              常见：DeepSeek <code>api.deepseek.com/v1</code> · 通义 <code>dashscope.aliyuncs.com/compatible-mode/v1</code> · Ollama <code>localhost:11434/v1</code>
            </div>
            <el-form :model="cfg" label-position="top" size="default">
              <el-form-item label="模型地址 (Base URL)" required>
                <el-input v-model="cfg.baseUrl" placeholder="https://api.deepseek.com/v1" />
              </el-form-item>
              <el-form-item label="模型名称" required>
                <el-input v-model="cfg.model" placeholder="deepseek-chat / qwen-plus / glm-4" />
              </el-form-item>
              <el-form-item label="API Key">
                <el-input v-model="cfg.apiKey" type="password" show-password
                  :placeholder="cfg.hasApiKey ? cfg.apiKeyMasked + '（留空表示不修改）' : 'sk-...（本地模型可留空）'" />
              </el-form-item>
              <div class="settings-row">
                <el-form-item label="采样温度">
                  <el-input-number v-model="cfg.temperature" :min="0" :max="2" :step="0.1" style="width: 100%" />
                </el-form-item>
                <el-form-item label="超时(秒)">
                  <el-input-number v-model="cfg.timeoutSeconds" :min="10" :max="300" :step="10" style="width: 100%" />
                </el-form-item>
              </div>
            </el-form>
            <div class="settings-actions">
              <el-button size="small" round @click="loadConfig">重置</el-button>
              <el-button type="primary" size="small" round :loading="saving" @click="saveConfig">保存并生效</el-button>
            </div>
          </div>
        </template>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Setting, Close, User } from '@element-plus/icons-vue'
import BotIcon from './BotIcon.vue'
import { aiParseProtocol, getAiConfig, saveAiConfig } from '../api'

const router = useRouter()
const open = ref(false)
const panel = ref('chat')
const input = ref('')
const loading = ref(false)
const saving = ref(false)
const messages = ref([])
const chatBox = ref(null)

const cfg = reactive({
  baseUrl: '',
  model: '',
  apiKey: '',
  apiKeyMasked: '',
  hasApiKey: false,
  temperature: 0.1,
  timeoutSeconds: 60
})

const cfgReady = computed(() => !!cfg.baseUrl && !!cfg.model)

function togglePanel() {
  open.value = !open.value
  if (open.value) {
    loadConfig()
    if (panel.value === 'settings' && !cfg.model) panel.value = 'chat'
  }
}

async function loadConfig() {
  try {
    const data = await getAiConfig()
    if (data.retSig === 200) {
      Object.assign(cfg, data.data || {})
      cfg.apiKey = ''
    }
  } catch (e) { /* http 拦截器已提示 */ }
}

async function saveConfig() {
  if (!cfg.baseUrl.trim() || !cfg.model.trim()) {
    ElMessage.warning('请填写模型地址与模型名称')
    return
  }
  saving.value = true
  try {
    const data = await saveAiConfig({
      baseUrl: cfg.baseUrl.trim(),
      model: cfg.model.trim(),
      apiKey: cfg.apiKey.trim(),
      temperature: cfg.temperature,
      timeoutSeconds: cfg.timeoutSeconds
    })
    if (data.retSig === 200) {
      ElMessage.success(data.msg || '配置已更新并生效')
      cfg.apiKey = ''
      await loadConfig()
    } else {
      ElMessage.error(data.error || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

function prettyJson(str) {
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch (e) {
    return str
  }
}

async function send() {
  const text = input.value.trim()
  if (!text) return
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  scrollBottom()
  loading.value = true
  try {
    const data = await aiParseProtocol(text)
    if (data.retSig === 200) {
      messages.value.push({ role: 'ai', type: 'json', content: JSON.stringify(data.data) })
    } else {
      messages.value.push({ role: 'ai', content: (data.error || '解析失败') })
    }
  } catch (e) {
    messages.value.push({ role: 'ai', content: '请求失败：' + (e.message || '网络错误') })
  } finally {
    loading.value = false
    scrollBottom()
  }
}

// 一键填充：跳转规约管理页并通过 sessionStorage 传递表单数据
function fillForm(jsonStr) {
  try {
    const obj = JSON.parse(jsonStr)
    const form = {
      pid: obj.pid ?? '',
      straName: obj.pName ?? '',
      bigdian: obj.isBigEndian ?? 1,
      lenOffset: obj.lengthFieldOffset ?? '',
      lenrange: obj.lengthFieldLength ?? '',
      lenInfo: obj.isDataLenthIncludeLenthFieldLenth ?? 1,
      leftLen: obj.exceptDataLenth ?? '',
      port: obj.port ?? ''
    }
    sessionStorage.setItem('ai_fill_form', JSON.stringify(form))
    const idx = messages.value.findIndex((m) => m.type === 'json' && m.content === jsonStr)
    if (idx > -1) messages.value[idx].filled = true
    router.push('/strategies')
    ElMessage.success('已填充到新增规约表单，请核对后提交')
  } catch (e) {
    ElMessage.error('解析结果格式异常，无法填充')
  }
}

function clearChat() {
  messages.value = []
}

function scrollBottom() {
  nextTick(() => {
    if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
  })
}

// 规约管理页"智能体助手"按钮触发展开
function handleOpenBot() {
  open.value = true
  panel.value = 'chat'
}

onMounted(() => {
  window.addEventListener('open-ai-bot', handleOpenBot)
})

onBeforeUnmount(() => {
  window.removeEventListener('open-ai-bot', handleOpenBot)
})
</script>

<style scoped>
.ai-bot {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 3000;
  font-family: inherit;
}

/* ===== 悬浮按钮 ===== */
.bot-fab {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b6fd4, #1f3b73);
  box-shadow: 0 8px 24px rgba(31, 59, 115, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.bot-fab:hover {
  transform: scale(1.08);
  box-shadow: 0 10px 30px rgba(31, 59, 115, 0.55);
}
.fab-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}
.fab-pulse {
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  border: 2px solid rgba(59, 111, 212, 0.5);
  animation: pulse 2s ease-out infinite;
}
@keyframes pulse {
  0% { transform: scale(0.9); opacity: 1; }
  70% { transform: scale(1.25); opacity: 0; }
  100% { opacity: 0; }
}

/* ===== 面板 ===== */
.bot-panel {
  width: 400px;
  height: 580px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 16px 48px rgba(0, 21, 41, 0.24);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e8ecf3;
}
.bot-header {
  background: linear-gradient(135deg, #3b6fd4, #1f3b73);
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  flex-shrink: 0;
}
.bot-title { display: flex; align-items: center; gap: 10px; }
.bot-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.bot-name { font-size: 15px; font-weight: 600; }
.bot-ver {
  font-size: 10.5px;
  font-weight: 500;
  opacity: 0.75;
  background: rgba(255, 255, 255, 0.16);
  border-radius: 8px;
  padding: 1px 6px;
  margin-left: 2px;
  vertical-align: 1px;
}
.bot-status {
  font-size: 11px;
  opacity: 0.9;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 2px;
}
.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #f56c6c;
  display: inline-block;
}
.dot.ok { background: #67c23a; }
.bot-actions { display: flex; gap: 6px; }
.icon-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}
.icon-btn:hover { background: rgba(255, 255, 255, 0.3); }

/* ===== 对话区 ===== */
.chat-box {
  flex: 1;
  overflow-y: auto;
  background: #f4f6fa;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.msg { display: flex; gap: 8px; }
.msg-user { flex-direction: row-reverse; }
.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #fff;
  border: 1px solid #e5e8ef;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b6fd4;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}
.bubble {
  max-width: 78%;
  padding: 9px 12px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #e8ecf3;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-all;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}
.msg-user .bubble {
  background: linear-gradient(135deg, #3b6fd4, #1f3b73);
  color: #fff;
  border: none;
}
.bubble code {
  background: #eef2f8;
  padding: 1px 5px;
  border-radius: 4px;
  font-size: 12px;
}
.msg-user .bubble code { background: rgba(255, 255, 255, 0.2); }
.bubble pre {
  margin: 0;
  white-space: pre-wrap;
  font-size: 11.5px;
  background: #f0f2f5;
  padding: 8px;
  border-radius: 8px;
  color: #333;
}
.fill-btn { margin-top: 8px; text-align: right; }
.fill-tip { margin-top: 6px; font-size: 12px; color: #67c23a; }

.loading { display: flex; gap: 4px; align-items: center; }
.dot-anim {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #8a94a6;
  animation: blink 1.2s infinite;
}
.dot-anim:nth-child(2) { animation-delay: 0.2s; }
.dot-anim:nth-child(3) { animation-delay: 0.4s; }
@keyframes blink {
  0%, 80%, 100% { opacity: 0.25; transform: scale(0.8); }
  40% { opacity: 1; transform: scale(1); }
}

/* ===== 输入区 ===== */
.chat-input {
  padding: 10px 12px;
  background: #fff;
  border-top: 1px solid #edf0f5;
  flex-shrink: 0;
}
.chat-input :deep(.el-textarea__inner) {
  border-radius: 10px;
  font-size: 13px;
  background: #f7f8fa;
  border-color: #e5e8ef;
}
.chat-actions {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 6px;
}

/* ===== 设置区 ===== */
.settings-box {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  background: #f4f6fa;
}
.settings-tip {
  background: #eef4ff;
  border: 1px solid #d6e4ff;
  color: #2c5bb5;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 12px;
  line-height: 1.7;
  margin-bottom: 12px;
}
.settings-tip code { background: #fff; padding: 1px 4px; border-radius: 4px; font-size: 11px; }
.settings-box :deep(.el-form-item) { margin-bottom: 14px; }
.settings-box :deep(.el-form-item__label) { font-size: 12.5px; color: #555; padding-bottom: 4px; }
.settings-row { display: flex; gap: 12px; }
.settings-row .el-form-item { flex: 1; }
.settings-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 4px;
}

/* ===== 过渡动画 ===== */
.fab-enter-active, .fab-leave-active { transition: opacity 0.2s, transform 0.2s; }
.fab-enter-from, .fab-leave-to { opacity: 0; transform: scale(0.7); }
.panel-enter-active { transition: opacity 0.25s, transform 0.25s; }
.panel-leave-active { transition: opacity 0.2s, transform 0.2s; }
.panel-enter-from, .panel-leave-to {
  opacity: 0;
  transform: translateY(16px) scale(0.96);
  transform-origin: bottom right;
}
</style>
