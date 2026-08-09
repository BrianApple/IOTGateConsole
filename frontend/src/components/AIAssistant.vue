<template>
  <el-dialog
    v-model="visible"
    title="🤖 智能体模式 — 规约帧结构解析"
    width="720px"
    top="6vh"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <!-- 聊天记录区 -->
    <div class="chat-box" ref="chatBox">
      <div class="msg msg-ai">
        <div class="avatar">🤖</div>
        <div class="bubble">
          你好，我是规约解析智能体。请粘贴你的<strong>规约帧结构</strong>（十六进制报文示例、字段说明表格或文字描述均可），
          我会自动推导出解码参数并帮你填充到新增规约表单。
          <br />示例：<code>68 00 0F 00 00 00 00 00 16</code>
        </div>
      </div>

      <div v-for="(m, i) in messages" :key="i" :class="['msg', m.role === 'user' ? 'msg-user' : 'msg-ai']">
        <div class="avatar">{{ m.role === 'user' ? '👤' : '🤖' }}</div>
        <div class="bubble">
          <pre v-if="m.type === 'json'">{{ prettyJson(m.content) }}</pre>
          <div v-else>{{ m.content }}</div>
          <div v-if="m.type === 'json'" class="fill-btn">
            <el-button type="primary" size="small" @click="fillForm(m.content)">一键填充到新增规约表单</el-button>
          </div>
        </div>
      </div>

      <div v-if="loading" class="msg msg-ai">
        <div class="avatar">🤖</div>
        <div class="bubble">正在分析帧结构，请稍候…</div>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="chat-input">
      <el-input
        v-model="input"
        type="textarea"
        :rows="3"
        placeholder="粘贴规约帧结构，如：帧头68 + 长度域(2字节,大端) + 控制域(1字节) + 数据域 + 校验(1字节) + 帧尾16"
        resize="none"
      />
      <div class="chat-actions">
        <el-button @click="clearChat">清空</el-button>
        <el-button type="primary" :loading="loading" @click="send" :disabled="!input.trim()">
          发送解析
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { aiParseProtocol } from '../api'

const props = defineProps({
  modelValue: Boolean
})
const emit = defineEmits(['update:modelValue', 'fill'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const input = ref('')
const loading = ref(false)
const messages = ref([])
const chatBox = ref(null)

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
      messages.value.push({ role: 'ai', content: '❌ ' + (data.error || '解析失败') })
    }
  } catch (e) {
    messages.value.push({ role: 'ai', content: '❌ 请求失败：' + (e.message || '网络错误') })
  } finally {
    loading.value = false
    scrollBottom()
  }
}

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
    emit('fill', form)
    ElMessage.success('已填充到新增规约表单，请核对后提交')
  } catch (e) {
    ElMessage.error('解析结果格式异常，无法填充')
  }
}

function clearChat() {
  messages.value = []
  input.value = ''
}

function scrollBottom() {
  nextTick(() => {
    if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
  })
}
</script>

<style scoped>
.chat-box {
  height: 380px;
  overflow-y: auto;
  background: #f7f8fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}
.msg {
  display: flex;
  margin-bottom: 14px;
}
.msg-user {
  flex-direction: row-reverse;
}
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e8ecf3;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}
.bubble {
  max-width: 80%;
  margin: 0 10px;
  padding: 10px 14px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid #e5e8ef;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-all;
}
.msg-user .bubble {
  background: #1f3b73;
  color: #fff;
  border-color: #1f3b73;
}
.bubble pre {
  margin: 0;
  white-space: pre-wrap;
  font-size: 12px;
  background: #f0f2f5;
  padding: 8px;
  border-radius: 6px;
  color: #333;
}
.fill-btn {
  margin-top: 8px;
  text-align: right;
}
.chat-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.chat-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
