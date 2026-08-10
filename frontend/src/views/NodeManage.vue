<template>
  <div class="node-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>网关节点列表</span>
          <div>
            <el-button size="small" @click="loadData">刷新</el-button>
            <el-tag size="small" type="info" style="margin-left: 8px">
              节点总数：{{ nodes.length }} 个
            </el-tag>
          </div>
        </div>
      </template>

      <el-table :data="nodes" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="gateNum" label="编号" width="80">
          <template #default="{ row }">
            <span v-if="row.gateNum && row.gateNum !== '-'">{{ row.gateNum }}</span>
            <span v-else style="color: #bbb">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="节点 IP" min-width="150" />
        <el-table-column label="注册来源" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.source === 'dynamic'" type="success" size="small" effect="plain">
              动态注册
            </el-tag>
            <el-tag v-else type="info" size="small" effect="plain">静态配置</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最近心跳" width="150">
          <template #default="{ row }">
            <span v-if="row.source === 'dynamic' && row.lastHeartbeat > 0">
              {{ formatTime(row.lastHeartbeat) }}
            </span>
            <span v-else style="color: #bbb">-</span>
          </template>
        </el-table-column>
        <el-table-column label="在线时长" width="110">
          <template #default="{ row }">
            <span v-if="row.source === 'dynamic' && row.regTime > 0">
              {{ formatDuration(row.regTime) }}
            </span>
            <span v-else style="color: #bbb">-</span>
          </template>
        </el-table-column>
        <el-table-column label="运行规约" min-width="260">
          <template #default="{ row }">
            <span v-if="row.data">{{ row.data }}</span>
            <el-tag v-else type="info" size="small">无</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.online ? 'success' : 'danger'" size="small" effect="dark">
              {{ row.online ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="RPC状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.stat === 'ok' ? 'success' : 'danger'" size="small">
              {{ row.stat === 'ok' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openStrategyDialog(row)">
              规约管理
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 规约启停对话框 -->
    <el-dialog v-model="strategyDialogVisible" title="节点规约管理" width="520px" :close-on-click-modal="false">
      <el-alert
        v-if="currentNode"
        :title="`节点 ${currentNode.ip}（${currentNode.source === 'dynamic' ? '动态注册' : '静态配置'}）`"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 12px"
      />
      <el-checkbox-group v-model="checkedPids">
        <div v-for="s in allStrategies" :key="s.pid" style="margin-bottom: 8px">
          <el-checkbox :value="String(s.pid)">{{ s.pName }}（pId={{ s.pid }}）</el-checkbox>
        </div>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="strategyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitStrategies">保存启停</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllStrategeFromDB, getGateData, updateStrategyNode } from '../api'

const nodes = ref([])
const loading = ref(false)

// 规约启停对话框状态
const strategyDialogVisible = ref(false)
const submitting = ref(false)
const allStrategies = ref([])
const checkedPids = ref([])
const currentNode = ref(null)

async function loadData() {
  loading.value = true
  try {
    const data = await getGateData()
    if (data.retSig === 200) {
      const list = (data.data || []).map((n) => ({
        ...n,
        online: nodes.value.find((x) => x.ip === n.ip)?.online ?? false
      }))
      // 合并 SSE 快照中的在线状态
      mergeStatus(list)
      nodes.value = list
    }
  } catch (e) {
    ElMessage.error('获取节点信息失败')
  } finally {
    loading.value = false
  }
}

// 用 SSE 推送的在线状态覆盖
function mergeStatus(list) {
  for (const n of list) {
    const online = onlineMap.value.get(n.ip)
    if (online !== undefined) n.online = online
  }
}

const onlineMap = ref(new Map())

function handleNodeStatus(e) {
  const { ip, online } = e.detail
  onlineMap.value.set(ip, online)
  const node = nodes.value.find((n) => n.ip === ip)
  if (node) node.online = online
}

function handleSnapshot(e) {
  const list = e.detail || []
  onlineMap.value = new Map(list.map((n) => [n.ip, n.online]))
  for (const n of nodes.value) {
    if (onlineMap.value.has(n.ip)) n.online = onlineMap.value.get(n.ip)
  }
}

// 解析运行规约字段 "pid:port/ pid:port" -> pid 集合
function parseRunningPids(dataStr) {
  if (!dataStr) return []
  return dataStr
    .split('/')
    .map((s) => s.trim())
    .filter(Boolean)
    .map((s) => s.split(':')[0])
}

// 打开规约管理对话框
async function openStrategyDialog(row) {
  currentNode.value = row
  checkedPids.value = parseRunningPids(row.data)
  try {
    const data = await getAllStrategeFromDB()
    if (data.retSig === 200) {
      // data[0] 为 {规约名: pid} 映射
      const map = (data.data && data.data[0]) || {}
      allStrategies.value = Object.entries(map).map(([name, pid]) => ({
        pName: name,
        pid
      }))
    }
  } catch (e) {
    ElMessage.error('获取规约列表失败')
  }
  strategyDialogVisible.value = true
}

// 提交规约启停
async function submitStrategies() {
  submitting.value = true
  try {
    const data = await updateStrategyNode(currentNode.value.ip, checkedPids.value)
    if (data.retSig === 200) {
      ElMessage.success('规约启停已下发')
      strategyDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error('下发失败：' + (data.erroInfo || data.retSig))
    }
  } catch (e) {
    ElMessage.error('下发失败：' + (e?.message || e))
  } finally {
    submitting.value = false
  }
}

// 时间戳格式化
function formatTime(ts) {
  const d = new Date(Number(ts))
  const p = (n) => String(n).padStart(2, '0')
  return `${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

// 在线时长格式化
function formatDuration(ts) {
  const diff = Math.max(0, Date.now() - Number(ts))
  const sec = Math.floor(diff / 1000)
  if (sec < 60) return `${sec}秒`
  const min = Math.floor(sec / 60)
  if (min < 60) return `${min}分钟`
  const hour = Math.floor(min / 60)
  if (hour < 24) return `${hour}小时${min % 60}分`
  const day = Math.floor(hour / 24)
  return `${day}天${hour % 24}小时`
}

function bindSSE() {
  window.addEventListener('iotgate-node-status', handleNodeStatus)
  window.addEventListener('iotgate-node-snapshot', handleSnapshot)
}

function unbindSSE() {
  window.removeEventListener('iotgate-node-status', handleNodeStatus)
  window.removeEventListener('iotgate-node-snapshot', handleSnapshot)
}

onMounted(() => {
  bindSSE()
  loadData()
})

onBeforeUnmount(unbindSSE)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}
</style>
