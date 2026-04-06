<template>
  <div class="signup-view fade-in">
    <NewspaperTitle
      title="新读者登记"
      subtitle="NEW READER REGISTRATION"
    />

    <div class="form-container paper-card double-border">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="电子邮箱" prop="email">
          <div class="email-input">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
            <InkButton @click="sendCode" :disabled="countdown > 0">
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </InkButton>
          </div>
        </el-form-item>

        <el-form-item label="验证码" prop="code">
          <el-input v-model="form.code" placeholder="请输入验证码" />
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
          <InkButton primary @click="handleSignup" :disabled="loading">
            {{ loading ? '注册中...' : '完 成 登 记' }}
          </InkButton>
        </el-form-item>
      </el-form>

      <div class="form-links">
        <span>已有账号？</span>
        <router-link to="/login" class="newspaper-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api/modules/user'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import InkButton from '@/components/common/InkButton.vue'

const router = useRouter()

const formRef = ref()
const loading = ref(false)
const countdown = ref(0)

const form = reactive({
  userName: '',
  email: '',
  code: '',
  password: ''
})

const rules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

async function sendCode() {
  if (!form.email) {
    return
  }
  try {
    await userApi.sendEmailCode(form.email)
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败', error)
  }
}

async function handleSignup() {
  try {
    await formRef.value.validate()
    loading.value = true

    await userApi.emailRegister(form)
    router.push('/login')
  } catch (error) {
    console.error('注册失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.signup-view {
  max-width: 500px;
  margin: 0 auto;
}

.form-container {
  padding: 32px;
  margin-top: 24px;
}

.email-input {
  display: flex;
  gap: 12px;
}

.email-input .el-input {
  flex: 1;
}

.form-links {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--ink-secondary);
}
</style>
