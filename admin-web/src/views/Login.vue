<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2>物业工单通 · 管理后台</h2>
      <p class="sub">客服与社区工作人员入口</p>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @submit.prevent>
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" size="large" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            size="large"
            type="password"
            show-password
            placeholder="请输入密码"
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="submit" :loading="loading" @click="onSubmit">
          登录
        </el-button>
      </el-form>

      <p class="hint">测试账号：admin / 123456（客服）　community1 / 123456（社区）</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value?.validate(async (valid: boolean) => {
    if (!valid) return
    loading.value = true
    try {
      await auth.login(form.username.trim(), form.password)
      const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/tickets'
      router.replace(redirect)
    } catch (err: any) {
      ElMessage.error(err?.response?.data?.message || err?.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f2d3d 0%, #2b4a6f 100%);
}

.login-card {
  width: 380px;
  padding: 12px 8px;
}

h2 {
  margin: 0 0 4px;
  font-size: 20px;
  text-align: center;
}

.sub {
  margin: 0 0 20px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.submit {
  width: 100%;
  margin-top: 4px;
}

.hint {
  margin: 18px 0 0;
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
}
</style>
