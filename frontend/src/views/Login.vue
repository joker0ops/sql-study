<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <div class="auth-header">
        <h1>关系型数据库学习网</h1>
        <p>SQL · MySQL · PostgreSQL</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="onSubmit">
          登 录
        </el-button>
      </el-form>
      <div class="auth-footer">
        还没有账号？
        <router-link to="/register">立即注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await auth.login({ username: form.username, password: form.password })
      ElMessage.success('登录成功')
      router.push(route.query.redirect || '/')
    } catch (e) {
      // 错误信息已由拦截器提示
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.auth-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f3b73 0%, #2c5aa0 100%);
}
.auth-card {
  width: 400px;
  padding: 12px 8px;
}
.auth-header {
  text-align: center;
  margin-bottom: 24px;
}
.auth-header h1 {
  font-size: 22px;
  color: #1f3b73;
}
.auth-header p {
  margin-top: 6px;
  color: #8a94a6;
  font-size: 13px;
  letter-spacing: 1px;
}
.submit-btn {
  width: 100%;
}
.auth-footer {
  margin-top: 16px;
  text-align: center;
  color: #8a94a6;
  font-size: 14px;
}
.auth-footer a {
  color: #2c5aa0;
  text-decoration: none;
}
</style>
