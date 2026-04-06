<template>
  <div class="login-view fade-in">
    <NewspaperTitle
      title="订阅登记表"
      subtitle="SUBSCRIPTION FORM"
    />

    <div class="form-container paper-card double-border">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="rememberMe">30天内记住此设备</el-checkbox>
        </el-form-item>

        <el-form-item>
          <InkButton primary @click="handleLogin" :disabled="loading">
            {{ loading ? '登录中...' : '提 交 订 阅' }}
          </InkButton>
        </el-form-item>
      </el-form>

      <div class="form-links">
        <span>还未订阅？</span>
        <router-link to="/signup" class="newspaper-link">立即登记</router-link>
        <span class="divider">|</span>
        <router-link to="/forgot-password" class="newspaper-link">忘记密码？</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/modules/user'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import InkButton from '@/components/common/InkButton.vue'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const rememberMe = ref(false)

const form = reactive({
  email: '',
  password: ''
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

async function handleLogin() {
  try {
    await formRef.value.validate()
    loading.value = true

    const res = await userApi.adminPasswordLogin(form)
    // res 是 Result<T>，res.data 是 UserLoginResponse
    userStore.setToken(res.data.token)
    router.push('/')
  } catch (error) {
    console.error('登录失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-view {
  max-width: 500px;
  margin: 0 auto;
}

.form-container {
  padding: 32px;
  margin-top: 24px;
}

.form-links {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--ink-secondary);
}

.divider {
  margin: 0 12px;
}
</style>
