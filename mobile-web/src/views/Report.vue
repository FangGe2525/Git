<template>
  <div class="page">
    <van-nav-bar title="报修" left-text="返回" left-arrow @click-left="router.back()" />

    <van-cell-group inset class="section" title="位置">
      <van-field v-model="point" label="点位码" placeholder="扫码自动带入" readonly />
      <van-field v-model="building" label="楼栋" placeholder="如 B3" />
      <van-field v-model="unit" label="单元" placeholder="如 1" />
      <van-field v-model="room" label="房号" placeholder="如 1201" />
      <van-field v-model="locationDetail" label="具体位置" placeholder="如 厨房洗菜盆下方" />
    </van-cell-group>

    <van-cell-group inset class="section" title="问题">
      <van-field
        v-model="category"
        label="工种"
        placeholder="请选择"
        readonly
        is-link
        @click="showCategory = true"
      />
      <van-field
        v-model="problemType"
        label="问题分类"
        placeholder="请选择"
        readonly
        is-link
        @click="showProblem = true"
      />
      <van-field
        v-model="description"
        type="textarea"
        rows="4"
        label="描述"
        maxlength="500"
        show-word-limit
        placeholder="请描述故障现象，越具体越好"
      />
    </van-cell-group>

    <van-cell-group inset class="section" title="紧急程度">
      <van-radio-group v-model="priority" direction="horizontal" class="priority">
        <van-radio name="LOW">低</van-radio>
        <van-radio name="MEDIUM">中</van-radio>
        <van-radio name="HIGH">高</van-radio>
      </van-radio-group>
    </van-cell-group>

    <van-cell-group inset class="section" title="照片（最多 3 张）">
      <div class="uploader">
        <van-uploader
          v-model="fileList"
          :max-count="3"
          accept="image/*"
          capture="environment"
          :after-read="afterRead"
          @delete="onDelete"
        />
      </div>
    </van-cell-group>

    <div class="submit">
      <van-button round block type="primary" :loading="submitting" @click="onSubmit">
        提交报修
      </van-button>
    </div>

    <van-popup v-model:show="showCategory" position="bottom" round>
      <van-picker
        :columns="categoryColumns"
        @confirm="(v: any) => { category = v.selectedValues[0]; showCategory = false }"
        @cancel="showCategory = false"
      />
    </van-popup>

    <van-popup v-model:show="showProblem" position="bottom" round>
      <van-picker
        :columns="problemColumns"
        @confirm="(v: any) => { problemType = v.selectedValues[0]; showProblem = false }"
        @cancel="showProblem = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { createTicket, uploadFile } from '../api/ticket'
import { compressImage } from '../utils/image'
import { CATEGORIES, PROBLEM_TYPES } from '../utils/status'

const route = useRoute()
const router = useRouter()

const point = ref('')
const building = ref('')
const unit = ref('')
const room = ref('')
const locationDetail = ref('')
const description = ref('')
const category = ref('')
const problemType = ref('')
const priority = ref('MEDIUM')
const fileList = ref<any[]>([])
const submitting = ref(false)
const showCategory = ref(false)
const showProblem = ref(false)

const categoryColumns = CATEGORIES.map((name) => ({ text: name, value: name }))
const problemColumns = PROBLEM_TYPES.map((name) => ({ text: name, value: name }))

onMounted(() => {
  const queryPoint = String(route.query.point || '')
  if (queryPoint) {
    point.value = queryPoint
    parsePoint(queryPoint)
  }
})

/** 支持 B3-1201 与 B3-1-1201 两种点位码 */
function parsePoint(value: string) {
  const parts = value.split('-').filter(Boolean)
  if (parts.length >= 2) {
    building.value = parts[0]
    if (parts.length >= 3) {
      unit.value = parts[1]
      room.value = parts[2]
    } else {
      room.value = parts[1]
    }
  }
}

async function afterRead(item: any) {
  item.status = 'uploading'
  item.message = '上传中'
  try {
    const compressed = await compressImage(item.file)
    const res: any = await uploadFile(compressed)
    item.url = res.data.url
    item.status = 'done'
    item.message = ''
  } catch {
    item.status = 'failed'
    item.message = '上传失败'
  }
}

function onDelete(item: any) {
  if (item?.url) {
    fileList.value = fileList.value.filter((f) => f !== item)
  }
}

async function onSubmit() {
  if (!description.value.trim()) {
    showToast('请填写故障描述')
    return
  }
  if (fileList.value.some((f) => f.status === 'uploading')) {
    showToast('图片还在上传，请稍候')
    return
  }
  submitting.value = true
  try {
    await createTicket({
      description: description.value.trim(),
      building: building.value,
      unit: unit.value,
      room: room.value,
      locationCode: point.value,
      locationDetail: locationDetail.value,
      category: category.value,
      problemType: problemType.value,
      priority: priority.value,
      mediaUrls: fileList.value.filter((f) => f.url).map((f) => f.url)
    })
    showSuccessToast('报修已提交')
    router.replace('/tickets')
  } catch {
    // 统一提示
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.priority {
  padding: 12px 16px;
}

.uploader {
  padding: 12px 16px;
}

.submit {
  margin: 20px 16px 0;
}
</style>
