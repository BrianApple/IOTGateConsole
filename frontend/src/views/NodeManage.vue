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
        <el-table-column prop="ip" label="节点 IP" min-width="150" />
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
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getGateData } from '../api'

const nodes = ref([])
const loading = ref(false)

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
