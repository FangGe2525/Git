<template>
  <el-container class="layout">
    <el-aside width="200px" class="aside">
      <div class="logo">物业工单通</div>
      <el-menu :default-active="activeMenu" router class="menu">
        <el-menu-item index="/tickets">工单中心</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="title">工单管理</div>
        <div class="user">
          <span class="name">{{ auth.user?.realName }}</span>
          <el-tag size="small" type="info">{{ auth.roleLabel }}</el-tag>
          <el-button link type="primary" @click="onLogout">退出</el-button>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const activeMenu = computed(() => route.path)

function onLogout() {
  auth.logout()
  router.replace('/login')
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.aside {
  background: #1f2d3d;
  color: #fff;
}

.logo {
  height: 56px;
  line-height: 56px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.menu {
  border-right: none;
  background: transparent;
}

.menu :deep(.el-menu-item) {
  color: #bfcbd9;
}

.menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: #1677ff;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name {
  font-size: 14px;
}

.main {
  padding: 16px;
}
</style>
