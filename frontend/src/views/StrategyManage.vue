<template>
  <div class="strategy-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>规约列表</span>
          <div>
            <el-button type="success" size="small" @click="showAI = true">🤖 智能体助手</el-button>
            <el-button type="primary" size="small" @click="showAdd = true">新增规约</el-button>
            <el-button size="small" @click="loadData">刷新</el-button>
            <el-tag size="small" type="info" style="margin-left: 8px">
              规约总数：{{ strategies.length }} 条
            </el-tag>
          </div>
        </div>
      </template>

      <el-table :data="strategies" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="pId" label="规约ID" width="90" />
        <el-table-column prop="pName" label="规约名称" min-width="140" />
        <el-table-column prop="isBigEndian" label="大小端" width="90">
          <template #default="{ row }">{{ row.isBigEndian ? '大端' : '小端' }}</template>
        </el-table-column>
        <el-table-column prop="lengthFieldOffset" label="长度域偏移" width="110" />
        <el-table-column prop="lengthFieldLength" label="长度域长度" width="110" />
        <el-table-column prop="isDataLenthIncludeLenthFieldLenth" label="长度含长度域" width="120">
          <template #default="{ row }">{{ row.isDataLenthIncludeLenthFieldLenth ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column prop="exceptDataLenth" label="长度外偏移" width="110" />
        <el-table-column prop="port" label="端口" width="90" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              title="将停止并删除所有网关上该规约的解析服务，确认删除？"
              confirm-button-text="删除"
              cancel-button-text="取消"
              @confirm="delStrategy(row)"
            >
              <template #reference>
                <el-button type="danger" size="small" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增规约对话框 -->
    <el-dialog v-model="showAdd" title="新增规约" width="480px" :close-on-click-modal="false">
      <el-form :model="form" label-width="130px">
        <el-form-item label="规约ID (pid)" required>
          <el-input v-model="form.pid" placeholder="取值范围0-127" />
        </el-form-item>
        <el-form-item label="规约名称" required>
          <el-input v-model="form.straName" placeholder="如：MQTT规约" />
        </el-form-item>
        <el-form-item label="大小端" required>
          <el-radio-group v-model="form.bigdian">
            <el-radio :value="1">大端</el-radio>
            <el-radio :value="0">小端</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="长度域偏移" required>
          <el-input v-model="form.lenOffset" placeholder="lengthFieldOffset" />
        </el-form-item>
        <el-form-item label="长度域长度" required>
          <el-input v-model="form.lenrange" placeholder="lengthFieldLength" />
        </el-form-item>
        <el-form-item label="长度含长度域" required>
          <el-radio-group v-model="form.lenInfo">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="长度外偏移" required>
          <el-input v-model="form.leftLen" placeholder="exceptDataLenth" />
        </el-form-item>
        <el-form-item label="服务端口" required>
          <el-input v-model="form.port" placeholder="如：9814" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAdd">提交</el-button>
      </template>
    </el-dialog>

    <!-- 智能体模式：规约帧结构AI解析 -->
    <AIAssistant v-model="showAI" @fill="onAIFill" />
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AIAssistant from '../components/AIAssistant.vue'
import { addOneStrategy, delOneStrategyByPID, getAllStrategyAllInfo } from '../api'

const strategies = ref([])
const loading = ref(false)
const showAdd = ref(false)
const showAI = ref(false)
const submitting = ref(false)

const form = reactive({
  pid: '',
  straName: '',
  bigdian: 1,
  lenOffset: '',
  lenrange: '',
  lenInfo: 1,
  leftLen: '',
  port: ''
})

async function loadData() {
  loading.value = true
  try {
    const data = await getAllStrategyAllInfo()
    if (data.retSig === 200) {
      strategies.value = (data.data && data.data[0]) || []
    }
  } catch (e) {
    ElMessage.error('获取规约列表失败')
  } finally {
    loading.value = false
  }
}

async function submitAdd() {
  if (!form.pid || !form.straName || !form.lenOffset || !form.lenrange || !form.leftLen || !form.port) {
    ElMessage.warning('请填写完整规约信息')
    return
  }
  submitting.value = true
  try {
    const data = await addOneStrategy({ ...form })
    if (data.retSig === 200) {
      ElMessage.success('规约新增成功')
      showAdd.value = false
      resetForm()
      loadData()
    } else {
      ElMessage.error('新增失败')
    }
  } catch (e) {
    ElMessage.error('新增失败')
  } finally {
    submitting.value = false
  }
}

async function delStrategy(row) {
  try {
    const data = await delOneStrategyByPID(row.pId)
    if (data.retSig === 200) {
      ElMessage.success('已删除')
      loadData()
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function resetForm() {
  Object.assign(form, {
    pid: '', straName: '', bigdian: 1, lenOffset: '',
    lenrange: '', lenInfo: 1, leftLen: '', port: ''
  })
}

// 智能体解析结果填充到新增规约表单
function onAIFill(aiForm) {
  showAI.value = false
  Object.assign(form, {
    pid: aiForm.pid ?? '',
    straName: aiForm.straName ?? '',
    bigdian: aiForm.bigdian ?? 1,
    lenOffset: aiForm.lenOffset ?? '',
    lenrange: aiForm.lenrange ?? '',
    lenInfo: aiForm.lenInfo ?? 1,
    leftLen: aiForm.leftLen ?? '',
    port: aiForm.port ?? ''
  })
  showAdd.value = true
}

function handleStrategyChange() {
  // SSE 通知规约有变更(可能是其他客户端操作)，刷新列表
  loadData()
}

function bindSSE() {
  window.addEventListener('iotgate-strategy-change', handleStrategyChange)
}
function unbindSSE() {
  window.removeEventListener('iotgate-strategy-change', handleStrategyChange)
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
