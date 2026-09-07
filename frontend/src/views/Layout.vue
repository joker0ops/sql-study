<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo" @click="router.push('/')">
        <span class="logo-icon">DB</span>
        <span class="logo-text">关系型数据库学习网</span>
      </div>
      <el-menu mode="horizontal" :default-active="activeMenu" router class="nav-menu" :ellipsis="false">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/courses">课程</el-menu-item>
        <el-menu-item index="/progress">学习进度</el-menu-item>
      </el-menu>
      <div class="user-area">
        <el-dropdown @command="onCommand">
          <span class="user-name">
            <el-icon><User /></el-icon>
            {{ auth.user?.username || '用户' }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const activeMenu = computed(() => {
  if (route.path.startsWith('/courses')) return '/courses'
  if (route.path.startsWith('/progress')) return '/progress'
  return '/'
})

function onCommand(command) {
  if (command === 'logout') {
    auth.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.header {
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e6e8eb;
  padding: 0 24px;
}
.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-right: 32px;
}
.logo-icon {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: linear-gradient(135deg, #1f3b73, #2c5aa0);
  color: #fff;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
}
.logo-text {
  font-size: 17px;
  font-weight: 600;
  color: #1f3b73;
}
.nav-menu {
  flex: 1;
  border-bottom: none;
}
.user-area {
  margin-left: auto;
}
.user-name {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #1f3b73;
  font-size: 14px;
}
.main {
  background: #f5f7fa;
  padding: 24px;
}
</style>
