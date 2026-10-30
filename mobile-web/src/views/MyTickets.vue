<template>
  <div class="page">
    <van-nav-bar title="我的工单">
      <template #right>
        <span class="logout" @click="onLogout">退出</span>
      </template>
    </van-nav-bar>

    <div class="me">
      <div class="avatar">{{ (auth.user?.realName || auth.user?.username || '?').slice(0, 1) }}</div>
      <div>
        <div class="name">{{ auth.user?.realName || auth.user?.username }}</div>
        <div class="role">{{ roleLabel }}</div>
      </div>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div v-for="t in list" :key="t.id" class="card" @click="router.push(`/tickets/${t.id}`)">
          <div class="row">
            <span class="no">{{ t.ticketNo }}</span>
            <van-tag :type="STATUS_TYPE[t.status]" plain>{{ STATUS_LABEL[t.status] }}</van-tag>
          </div>
          <div class="desc">{{ t.description }}</div>
          <div class="meta">
            <span>{{ locationText(t) }}</span>
            <span>{{ formatTime(t.createdAt) }}</span>
          </div>
          <div v-if="t.mediaUrls?.length" class="meta">
            <span>📷 {{ t.mediaUrls.length }} 张照片</span>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <van-empty v-if="!list.length && finished" description="还没有工单" />

    <div class="fab">
      <van-button type="primary" round icon="plus" @click="router.push('/report')">我要报修</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { listTickets } from '../api/ticket'
import { useAuthStore } from '../stores/auth'
import { STATUS_LABEL, STATUS_TYPE, formatTime } from '../utils/status'

const router = useRouter()
const auth = useAuthStore()

const list = ref<any[]>([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const size = 10

const roleLabel = computed(() => {
  const map: Record<string, string> = {
    OWNER: '业主',
    WORKER: '维修工',
    CS: '物业客服',
    COMMUNITY: '社区工作人员'
  }
  return map[auth.role] || ''
})

function locationText(t: any) {
  return [t.building, t.unit && t.unit + '单元', t.room && t.room + '室'].filter(Boolean).join(' ') || '未填写位置'
}

async function onLoad() {
  loading.value = true
  try {
    const res: any = await listTickets({ page: page.value, size })
    const records = res.data.records || []
    list.value = page.value === 1 ? records : list.value.concat(records)
    finished.value = page.value >= res.data.pages
    page.value += 1
  } catch {
    finished.value = true
  } finally {
    loading.value = false
  }
}

async function onRefresh() {
  page.value = 1
  finished.value = false
  list.value = []
  refreshing.value = true
  try {
    await onLoad()
  } finally {
    refreshing.value = false
  }
}

function onLogout() {
  auth.logout()
  router.replace('/login')
}
</script>

<style scoped>
.logout {
  font-size: 14px;
  color: #969799;
}

.me {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fff;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--brand);
  color: #fff;
  text-align: center;
  line-height: 44px;
  font-size: 18px;
}

.name {
  font-size: 16px;
  font-weight: 600;
}

.role {
  font-size: 12px;
  color: #969799;
  margin-top: 2px;
}

.card {
  margin: 12px;
  padding: 14px;
  background: #fff;
  border-radius: 10px;
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
  margin: 8px 0;
  font-size: 15px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #c8c9cc;
  margin-top: 4px;
}

.fab {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 24px;
  text-align: center;
}
</style>
