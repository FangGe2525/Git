<template>
  <div>
    <el-card shadow="never" class="filters">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 130px">
            <el-option v-for="s in STATUS_OPTIONS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.category" clearable placeholder="全部" style="width: 120px">
            <el-option v-for="c in CATEGORIES" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼栋">
          <el-input v-model="query.building" clearable placeholder="如 B3" style="width: 110px" />
        </el-form-item>
        <el-form-item label="维修工">
          <el-select v-model="query.workerId" clearable placeholder="全部" style="width: 150px">
            <el-option v-for="w in workers" :key="w.id" :label="w.realName" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
            v-model="query.keyword"
            clearable
            placeholder="单号 / 描述 / 位置"
            style="width: 190px"
            @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="rows"
        stripe
        class="table"
        @row-click="openDetail"
      >
        <el-table-column prop="ticketNo" label="单号" width="150" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="STATUS_TYPE[row.status]" size="small">{{ STATUS_LABEL[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="PRIORITY_TYPE[row.priority]" size="small" effect="plain">
              {{ PRIORITY_LABEL[row.priority] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="位置" width="150">
          <template #default="{ row }">{{ locationText(row) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip min-width="220" />
        <el-table-column label="报修人" width="100">
          <template #default="{ row }">{{ row.reporterName || '-' }}</template>
        </el-table-column>
        <el-table-column label="维修工" width="100">
          <template #default="{ row }">{{ workerName(row.assignedWorkerId) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="openDetail(row)">详情</el-button>
            <el-button
              link
              type="primary"
              :disabled="isTerminal(row)"
              @click.stop="openAssign(row)"
            >
              派单
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        class="pager"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="onSearch"
      />
    </el-card>

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawer" size="760px" :title="detail?.ticket?.ticketNo || '工单详情'">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="状态">
            <el-tag :type="STATUS_TYPE[detail.ticket.status]" size="small">
              {{ STATUS_LABEL[detail.ticket.status] }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="PRIORITY_TYPE[detail.ticket.priority]" size="small" effect="plain">
              {{ PRIORITY_LABEL[detail.ticket.priority] }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="位置">{{ locationText(detail.ticket) }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ SOURCE_LABEL[detail.ticket.source] }}</el-descriptions-item>
          <el-descriptions-item label="报修人">{{ detail.ticket.reporterName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detail.ticket.reporterPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="工种">{{ detail.ticket.category || '-' }}</el-descriptions-item>
          <el-descriptions-item label="问题分类">{{ detail.ticket.problemType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="维修工">
            {{ workerName(detail.ticket.assignedWorkerId) || '未派单' }}
          </el-descriptions-item>
          <el-descriptions-item label="费用">
            <span v-if="detail.ticket.feeAmount">¥{{ detail.ticket.feeAmount }}</span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="报修时间">{{ formatTime(detail.ticket.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="返修次数">{{ detail.ticket.reworkCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="故障描述" :span="2">
            {{ detail.ticket.description }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="detail.ticket.mediaUrls?.length" class="images">
          <div class="images-label">报修照片</div>
          <el-image
            v-for="(url, i) in detail.ticket.mediaUrls"
            :key="'m' + i"
            :src="url"
            :preview-src-list="detail.ticket.mediaUrls"
            :initial-index="i"
            fit="cover"
            class="thumb"
            preview-teleported
          />
        </div>

        <div v-if="detail.ticket.finishMediaUrls?.length" class="images">
          <div class="images-label">完工照片</div>
          <el-image
            v-for="(url, i) in detail.ticket.finishMediaUrls"
            :key="'f' + i"
            :src="url"
            :preview-src-list="detail.ticket.finishMediaUrls"
            :initial-index="i"
            fit="cover"
            class="thumb"
            preview-teleported
          />
        </div>

        <h4 class="section-title">状态时间线</h4>
        <el-timeline>
          <el-timeline-item
            v-for="log in detail.logs"
            :key="log.id"
            :timestamp="formatTime(log.createdAt)"
            placement="top"
          >
            <div class="log-head">
              <strong>{{ log.actionLabel }}</strong>
              <span v-if="log.fromStatus" class="flow">
                {{ STATUS_LABEL[log.fromStatus] }} → {{ STATUS_LABEL[log.toStatus] }}
              </span>
              <span class="operator">{{ log.operatorName || '系统' }}</span>
            </div>
            <div v-if="log.remark" class="log-remark">{{ log.remark }}</div>
          </el-timeline-item>
        </el-timeline>
      </template>

      <template #footer>
        <div class="footer-actions">
          <el-button v-if="detail && !isTerminal(detail.ticket)" @click="openAssign(detail.ticket)">
            派单 / 转派
          </el-button>
          <el-button
            v-if="detail && detail.ticket.status === 'VERIFYING'"
            type="success"
            @click="onConfirm"
          >
            代确认完工
          </el-button>
          <el-button v-if="detail && !isTerminal(detail.ticket)" @click="onUrge">催办</el-button>
          <el-button v-if="detail" @click="onRemark">备注</el-button>
          <el-button
            v-if="detail && !isTerminal(detail.ticket)"
            type="danger"
            plain
            @click="onCancel"
          >
            作废
          </el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 派单对话框 -->
    <el-dialog v-model="assignVisible" title="派单" width="460px">
      <el-radio-group v-model="assignWorkerId" class="worker-list">
        <el-radio v-for="w in workers" :key="w.id" :value="w.id" class="worker-item">
          <span class="worker-name">{{ w.realName }}</span>
          <el-tag size="small" type="info" effect="plain" class="worker-tag">
            在手工单 {{ w.activeCount }}
          </el-tag>
          <span class="worker-skill">{{ w.skillTags || '未设置技能' }}</span>
        </el-radio>
      </el-radio-group>
      <el-input v-model="assignRemark" placeholder="备注（可选）" class="assign-remark" />
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="acting" :disabled="!assignWorkerId" @click="doAssign">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addRemark,
  assignTicket,
  cancelTicket,
  confirmTicket,
  getTicket,
  listTickets,
  listWorkers,
  urgeTicket
} from '../api/ticket'
import {
  CATEGORIES,
  PRIORITY_LABEL,
  PRIORITY_TYPE,
  SOURCE_LABEL,
  STATUS_LABEL,
  STATUS_OPTIONS,
  STATUS_TYPE,
  formatTime,
  locationText
} from '../utils/dict'

interface Worker {
  id: number
  realName: string
  skillTags?: string
  activeCount: number
}

const rows = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const workers = ref<Worker[]>([])

const query = reactive({
  page: 1,
  size: 10,
  status: '',
  category: '',
  building: '',
  workerId: undefined as number | undefined,
  keyword: ''
})

const drawer = ref(false)
const detail = ref<any>(null)

const assignVisible = ref(false)
const assignWorkerId = ref<number | undefined>(undefined)
const assignRemark = ref('')
const assignTargetId = ref<number | null>(null)
const acting = ref(false)

function isTerminal(row: any) {
  return row?.status === 'COMPLETED' || row?.status === 'CANCELED'
}

function workerName(id?: number) {
  if (!id) return ''
  return workers.value.find((w) => w.id === id)?.realName || `#${id}`
}

async function load() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: query.page, size: query.size }
    if (query.status) params.status = query.status
    if (query.category) params.category = query.category
    if (query.building) params.building = query.building
    if (query.workerId) params.workerId = query.workerId
    if (query.keyword.trim()) params.keyword = query.keyword.trim()

    const res: any = await listTickets(params)
    rows.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.page = 1
  load()
}

function onReset() {
  query.status = ''
  query.category = ''
  query.building = ''
  query.workerId = undefined
  query.keyword = ''
  query.page = 1
  load()
}

async function loadWorkers() {
  const res: any = await listWorkers()
  workers.value = res.data || []
}

async function openDetail(row: any) {
  const res: any = await getTicket(row.id)
  detail.value = res.data
  drawer.value = true
}

async function refreshDetail() {
  if (!detail.value) return
  const res: any = await getTicket(detail.value.ticket.id)
  detail.value = res.data
}

function openAssign(row: any) {
  assignTargetId.value = row.id
  assignWorkerId.value = row.assignedWorkerId ?? undefined
  assignRemark.value = ''
  assignVisible.value = true
  if (!workers.value.length) loadWorkers()
}

async function doAssign() {
  if (!assignTargetId.value || !assignWorkerId.value) return
  acting.value = true
  try {
    await assignTicket(assignTargetId.value, assignWorkerId.value, assignRemark.value || undefined)
    ElMessage.success('派单成功')
    assignVisible.value = false
    await load()
    await refreshDetail()
  } finally {
    acting.value = false
  }
}

async function onConfirm() {
  await ElMessageBox.confirm('确认该工单已维修完成？', '代确认完工', { type: 'warning' })
  await confirmTicket(detail.value.ticket.id)
  ElMessage.success('已确认完工')
  await load()
  await refreshDetail()
}

async function onCancel() {
  const { value } = await ElMessageBox.prompt('请填写作废原因', '作废工单', {
    inputPlaceholder: '如：业主取消报修',
    inputValidator: (v: string) => (v && v.trim() ? true : '请填写作废原因')
  })
  await cancelTicket(detail.value.ticket.id, value)
  ElMessage.success('已作废')
  await load()
  await refreshDetail()
}

async function onUrge() {
  await urgeTicket(detail.value.ticket.id)
  ElMessage.success('已催办')
  await refreshDetail()
}

async function onRemark() {
  const { value } = await ElMessageBox.prompt('输入备注内容', '添加备注', {
    inputPlaceholder: '如：已电话联系业主'
  })
  if (!value?.trim()) return
  await addRemark(detail.value.ticket.id, value.trim())
  ElMessage.success('已添加')
  await refreshDetail()
}

onMounted(async () => {
  await loadWorkers()
  await load()
})
</script>

<style scoped>
.filters {
  margin-bottom: 12px;
}

.filters :deep(.el-form-item) {
  margin-bottom: 0;
}

.table-card {
  min-height: 400px;
}

.table :deep(.el-table__row) {
  cursor: pointer;
}

.pager {
  margin-top: 16px;
  justify-content: flex-end;
}

.images {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.images-label {
  width: 100%;
  font-size: 13px;
  color: #909399;
}

.thumb {
  width: 96px;
  height: 96px;
  border-radius: 6px;
}

.section-title {
  margin: 24px 0 12px;
  font-size: 14px;
}

.log-head {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.flow {
  font-size: 12px;
  color: #909399;
}

.operator {
  margin-left: auto;
  font-size: 12px;
  color: #c0c4cc;
}

.log-remark {
  margin-top: 6px;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 6px 10px;
  border-radius: 4px;
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.worker-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.worker-item {
  height: auto;
  padding: 8px 0;
  margin-right: 0;
}

.worker-name {
  font-weight: 500;
  margin-right: 8px;
}

.worker-tag {
  margin-right: 8px;
}

.worker-skill {
  font-size: 12px;
  color: #909399;
}

.assign-remark {
  margin-top: 16px;
}
</style>
