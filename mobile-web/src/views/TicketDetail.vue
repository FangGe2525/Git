<template>
  <div class="page">
    <van-nav-bar title="工单详情" left-text="返回" left-arrow @click-left="router.back()" />

    <template v-if="ticket">
      <div class="head">
        <div class="row">
          <span class="no">{{ ticket.ticketNo }}</span>
          <van-tag :type="STATUS_TYPE[ticket.status]">{{ STATUS_LABEL[ticket.status] }}</van-tag>
        </div>
        <div class="desc">{{ ticket.description }}</div>
        <div class="tags">
          <van-tag v-if="ticket.priority === 'HIGH'" type="danger" plain>紧急</van-tag>
          <van-tag v-if="ticket.category" plain>{{ ticket.category }}</van-tag>
          <van-tag v-if="ticket.problemType" plain>{{ ticket.problemType }}</van-tag>
          <van-tag type="default" plain>{{ SOURCE_LABEL[ticket.source] }}</van-tag>
        </div>
      </div>

      <van-cell-group inset class="section" title="报修信息">
        <van-cell title="位置" :value="locationText" />
        <van-cell v-if="ticket.locationDetail" title="具体位置" :value="ticket.locationDetail" />
        <van-cell title="报修人" :value="ticket.reporterName || '-'" />
        <van-cell title="报修时间" :value="formatTime(ticket.createdAt)" />
        <van-cell v-if="ticket.feeAmount" title="维修费用" :value="`¥${ticket.feeAmount}`" />
        <van-cell v-if="ticket.reworkCount" title="返修次数" :value="`${ticket.reworkCount} 次`" />
      </van-cell-group>

      <van-cell-group v-if="ticket.mediaUrls?.length" inset class="section" title="现场照片">
        <div class="images">
          <van-image
            v-for="(url, i) in ticket.mediaUrls"
            :key="i"
            :src="url"
            width="88"
            height="88"
            fit="cover"
            radius="6"
            @click="preview(i)"
          />
        </div>
      </van-cell-group>

      <van-cell-group inset class="section" title="进度">
        <div class="timeline">
          <div v-for="log in logs" :key="log.id" class="item">
            <div class="dot" />
            <div class="content">
              <div class="line1">
                <span class="action">{{ log.actionLabel }}</span>
                <span class="time">{{ formatTime(log.createdAt) }}</span>
              </div>
              <div class="line2">
                <span v-if="log.fromStatus" class="flow">
                  {{ STATUS_LABEL[log.fromStatus] }} → {{ STATUS_LABEL[log.toStatus] }}
                </span>
                <span class="op">{{ log.operatorName || '系统' }}</span>
              </div>
              <div v-if="log.remark" class="remark">{{ log.remark }}</div>
            </div>
          </div>
        </div>
      </van-cell-group>

      <div class="actions">
        <van-button v-if="canAccept" type="primary" block round :loading="acting" @click="act('accept')">
          接单
        </van-button>
        <van-button v-if="canStart" type="primary" block round :loading="acting" @click="act('start')">
          到场开始处理
        </van-button>
        <van-button v-if="canFinish" type="primary" block round @click="showFinish = true">
          完工提交
        </van-button>
        <van-button v-if="canConfirm" type="success" block round :loading="acting" @click="act('confirm')">
          确认完工
        </van-button>
        <van-button v-if="canRework" type="warning" block round plain @click="onRework">
          申请返修
        </van-button>
        <van-button v-if="canReturn" block round plain @click="onReturn">退回工单</van-button>
        <van-button v-if="canUrge" block round plain size="small" @click="act('urge')">催办</van-button>
      </div>
    </template>

    <van-popup v-model:show="showFinish" position="bottom" round>
      <div class="popup">
        <h3>完工提交</h3>
        <van-field
          v-model="finishResult"
          type="textarea"
          rows="3"
          maxlength="500"
          show-word-limit
          placeholder="请填写维修结果，如：更换角阀，已试水无渗漏"
        />
        <van-field
          v-model="feeAmount"
          type="number"
          label="费用"
          placeholder="无偿维修可留空"
        />
        <van-button type="primary" block round :loading="acting" @click="onFinish">提交</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showSuccessToast, showConfirmDialog, showImagePreview } from 'vant'
import {
  acceptTicket,
  confirmTicket,
  finishTicket,
  getTicket,
  returnTicket,
  reworkTicket,
  startTicket,
  urgeTicket
} from '../api/ticket'
import { useAuthStore } from '../stores/auth'
import { SOURCE_LABEL, STATUS_LABEL, STATUS_TYPE, formatTime } from '../utils/status'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const detail = ref<any>(null)
const acting = ref(false)
const showFinish = ref(false)
const finishResult = ref('')
const feeAmount = ref('')

const ticket = computed(() => detail.value?.ticket)
const logs = computed(() => detail.value?.logs || [])

const locationText = computed(() => {
  const t = ticket.value
  if (!t) return ''
  return [t.building, t.unit && `${t.unit}单元`, t.room && `${t.room}室`].filter(Boolean).join(' ') || '-'
})

const isAssignedWorker = computed(
  () => auth.isWorker && ticket.value?.assignedWorkerId === auth.userId
)
const isTicketOwner = computed(() => ticket.value?.ownerId === auth.userId)
const isTerminal = computed(() => ['COMPLETED', 'CANCELED'].includes(ticket.value?.status))

const canAccept = computed(() => isAssignedWorker.value && ticket.value?.status === 'PENDING')
const canStart = computed(() => isAssignedWorker.value && ticket.value?.status === 'ACCEPTED')
const canFinish = computed(() => isAssignedWorker.value && ticket.value?.status === 'REPAIRING')
const canConfirm = computed(
  () => (isTicketOwner.value || auth.isStaff) && ticket.value?.status === 'VERIFYING'
)
const canRework = computed(
  () => (isTicketOwner.value || auth.isStaff) && ticket.value?.status === 'VERIFYING'
)
const canReturn = computed(
  () => isAssignedWorker.value && ['ACCEPTED', 'REPAIRING'].includes(ticket.value?.status)
)
const canUrge = computed(() => !!ticket.value && !isTerminal.value)

async function load() {
  const res: any = await getTicket(route.params.id as string)
  detail.value = res.data
}

async function act(type: 'accept' | 'start' | 'confirm' | 'urge') {
  acting.value = true
  try {
    const id = route.params.id as string
    if (type === 'accept') await acceptTicket(id)
    if (type === 'start') await startTicket(id)
    if (type === 'confirm') await confirmTicket(id)
    if (type === 'urge') await urgeTicket(id)
    showSuccessToast('操作成功')
    await load()
  } catch {
    // 统一提示
  } finally {
    acting.value = false
  }
}

async function onFinish() {
  if (!finishResult.value.trim()) {
    showToast('请填写维修结果')
    return
  }
  acting.value = true
  try {
    const fee = feeAmount.value ? Number(feeAmount.value) : undefined
    await finishTicket(route.params.id as string, finishResult.value.trim(), fee)
    showFinish.value = false
    showSuccessToast('已提交，等待确认')
    await load()
  } catch {
    // 统一提示
  } finally {
    acting.value = false
  }
}

async function onRework() {
  try {
    const { showPrompt } = await import('vant')
    const result: any = await showPrompt({
      title: '申请返修',
      message: '请说明返修原因',
      messagePlaceholder: '如：水管仍在渗水'
    })
    await reworkTicket(route.params.id as string, result.value)
    showSuccessToast('已申请返修')
    await load()
  } catch {
    // 取消或失败
  }
}

async function onReturn() {
  try {
    const { showPrompt } = await import('vant')
    const result: any = await showPrompt({
      title: '退回工单',
      message: '请说明退回原因',
      messagePlaceholder: '如：缺少配件，需重新派单'
    })
    await returnTicket(route.params.id as string, result.value)
    showSuccessToast('已退回')
    await load()
  } catch {
    // 取消或失败
  }
}

function preview(index: number) {
  showImagePreview({ images: ticket.value.mediaUrls, startPosition: index })
}

onMounted(load)
</script>

<style scoped>
.head {
  background: #fff;
  padding: 16px;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.no {
  font-size: 13px;
  color: #969799;
  font-family: ui-monospace, monospace;
}

.desc {
  margin: 10px 0;
  font-size: 16px;
  line-height: 1.6;
}

.tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.images {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  flex-wrap: wrap;
}

.timeline {
  padding: 16px;
}

.item {
  display: flex;
  gap: 10px;
  padding-bottom: 18px;
  position: relative;
}

.item::before {
  content: '';
  position: absolute;
  left: 4px;
  top: 14px;
  bottom: 0;
  width: 1px;
  background: #ebedf0;
}

.item:last-child::before {
  display: none;
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: var(--brand);
  margin-top: 5px;
  flex-shrink: 0;
}

.content {
  flex: 1;
}

.line1 {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.action {
  font-weight: 600;
}

.time {
  color: #c8c9cc;
  font-size: 12px;
}

.line2 {
  margin-top: 4px;
  font-size: 12px;
  color: #969799;
  display: flex;
  gap: 8px;
}

.remark {
  margin-top: 6px;
  font-size: 13px;
  color: #646566;
  background: #f7f8fa;
  padding: 8px 10px;
  border-radius: 6px;
  line-height: 1.5;
}

.actions {
  margin: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.popup {
  padding: 20px 16px 28px;
}

.popup h3 {
  margin: 0 0 12px;
  font-size: 16px;
  text-align: center;
}
</style>
