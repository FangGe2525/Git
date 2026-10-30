<template>
  <div class="page login">
    <div class="brand">
      <div class="logo">工</div>
      <h1>物业工单通</h1>
      <p>报修 · 派单 · 进度可查</p>
    </div>

    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="username"
          name="username"
          label="账号"
          placeholder="请输入账号"
          :rules="[{ required: true, message: '请输入账号' }]"
        />
        <van-field
          v-model="password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
      </van-cell-group>

      <div class="submit">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          登录
        </van-button>
      </div>
    </van-form>

    <p class="hint">测试账号：owner1（业主） / worker1（维修工） / admin（客服），密码 123456</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const username = ref('')
const password = ref('')
const loading = ref(false)

async function onSubmit() {
  loading.value = true
  try {
    await auth.login(username.value.trim(), password.value)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/tickets'
    router.replace(redirect)
  } catch {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  padding: 56px 16px 24px;
  background: linear-gradient(180deg, #e8f1ff 0%, #f7f8fa 45%);
}

.brand {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  width: 56px;
  height: 56px;
  line-height: 56px;
  margin: 0 auto 12px;
  border-radius: 16px;
  background: var(--brand);
  color: #fff;
  font-size: 26px;
  font-weight: 600;
}

.brand h1 {
  margin: 0;
  font-size: 22px;
  letter-spacing: 2px;
}

.brand p {
  margin: 6px 0 0;
  color: #969799;
  font-size: 13px;
}

.submit {
  margin: 24px 16px 0;
}

.hint {
  margin-top: 24px;
  text-align: center;
  font-size: 12px;
  color: #c8c9cc;
  line-height: 1.6;
}
</style>
