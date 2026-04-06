<template>
  <div class="forgot-password-view fade-in">
    <NewspaperTitle
      title="密码重置申请"
      subtitle="PASSWORD RESET REQUEST"
    />

    <div class="form-container paper-card double-border">
      <div class="alert-box">
        <el-icon class="alert-icon"><InfoFilled /></el-icon>
        <div class="alert-content">
          <p class="alert-title">温馨提示</p>
          <p class="alert-desc">如果您忘记了密码，请联系系统管理员进行密码重置。</p>
          <p class="alert-desc">管理员邮箱：admin@lottery-system.com</p>
        </div>
      </div>

      <div class="divider-section">
        <span class="divider-text">或者尝试以下方式</span>
      </div>

      <p class="form-desc">如果您是管理员，可通过邮箱验证码重置密码。</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="管理员邮箱" prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入管理员邮箱"
            :prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item label="验证码" prop="code">
          <div class="code-input">
            <el-input
              v-model="form.code"
              placeholder="请输入6位验证码"
              :prefix-icon="Key"
            />
            <InkButton
              @click="sendCode"
              :disabled="countdown > 0 || !isEmailValid"
              :loading="sendingCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </InkButton>
          </div>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item>
          <InkButton primary @click="handleSubmit" :disabled="loading" :loading="loading">
            {{ loading ? '提交中...' : '提 交 申 请' }}
          </InkButton>
        </el-form-item>
      </el-form>

      <div class="form-links">
        <router-link to="/login" class="newspaper-link">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock, Key, InfoFilled } from '@element-plus/icons-vue'
import { userApi } from '@/api/modules/user'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import InkButton from '@/components/common/InkButton.vue'

const router = useRouter()

const formRef = ref()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const countdownTimer = ref(null)

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 邮箱格式验证
const isEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(form.email)
})

// 确认密码验证
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function sendCode() {
  if (!isEmailValid.value) {
    ElMessage.warning('请先输入正确的邮箱地址')
    return
  }

  sendingCode.value = true
  try {
    await userApi.sendAdminEmailCode(form.email)
    ElMessage.success('验证码已发送，请查收邮箱')
    countdown.value = 60
    countdownTimer.value = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer.value)
        countdownTimer.value = null
      }
    }, 1000)
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    sendingCode.value = false
  }
}

onUnmounted(() => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
})

async function handleSubmit() {
  try {
    await formRef.value.validate()
    loading.value = true

    // 注意：当前后端可能没有密码重置接口
    // 这里使用邮箱登录接口进行验证，然后提示用户联系管理员
    // TODO: 等待后端提供密码重置接口

    ElMessage.info('密码重置功能开发中，请联系管理员处理')
    // 验证码验证成功后，应该调用密码重置接口
    // await userApi.resetPassword({
    //   email: form.email,
    //   code: form.code,
    //   newPassword: form.newPassword
    // })
    // ElMessage.success('密码重置成功，请登录')
    // router.push('/login')

  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.forgot-password-view {
  max-width: 500px;
  margin: 0 auto;
}

.form-container {
  padding: 32px;
  margin-top: 24px;
}

.alert-box {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: var(--paper-dark);
  border-left: 4px solid var(--ink-primary);
  margin-bottom: 24px;
}

.alert-icon {
  font-size: 24px;
  color: var(--ink-primary);
  flex-shrink: 0;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-weight: 600;
  color: var(--ink-primary);
  margin-bottom: 8px;
}

.alert-desc {
  font-size: 14px;
  color: var(--ink-secondary);
  margin: 4px 0;
}

.divider-section {
  text-align: center;
  margin: 24px 0;
  position: relative;
}

.divider-section::before {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  top: 50%;
  height: 1px;
  background: var(--ink-light);
}

.divider-text {
  background: var(--paper-light);
  padding: 0 16px;
  color: var(--ink-secondary);
  font-size: 14px;
  position: relative;
}

.form-desc {
  color: var(--ink-secondary);
  margin-bottom: 24px;
  font-size: 14px;
}

.code-input {
  display: flex;
  gap: 12px;
}

.code-input .el-input {
  flex: 1;
}

.form-links {
  text-align: center;
  margin-top: 24px;
}

@media (max-width: 480px) {
  .code-input {
    flex-direction: column;
  }
}
</style>
