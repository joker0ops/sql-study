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
              <el-dropdown-item command="aiConfig">AI 配置</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
      <div class="ai-float" @click="router.push('/ai')">
        <span class="ai-float-icon">AI</span>
        <span class="ai-float-text">AI 提问</span>
      </div>
    </el-main>

    <AiConfigDialog v-model="aiConfigVisible" />
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import AiConfigDialog from '../components/AiConfigDialog.vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const aiConfigVisible = ref(false)

const activeMenu = computed(() => {
  if (route.path.startsWith('/courses')) return '/courses'
  if (route.path.startsWith('/progress')) return '/progress'
  return '/'
})

function onCommand(command) {
  if (command === 'aiConfig') {
    aiConfigVisible.value = true
  } else if (command === 'logout') {
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
.ai-float {
  position: fixed;
  right: 28px;
  bottom: 32px;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
  border-radius: 24px;
  background: linear-gradient(135deg, #1f3b73, #2c5aa0);
  color: #fff;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(31, 59, 115, 0.35);
  transition: transform 0.15s ease;
}
.ai-float:hover {
  transform: translateY(-2px);
}
.ai-float-icon {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #fff;
  color: #1f3b73;
  font-weight: 700;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ai-float-text {
  font-size: 14px;
  font-weight: 600;
}
</style>
