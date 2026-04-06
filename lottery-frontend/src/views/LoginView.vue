<template>
  <div class="login-view fade-in">
    <NewspaperTitle
      title="订阅登记表"
      subtitle="SUBSCRIPTION FORM"
    />

    <div class="form-container paper-card double-border">
      <!-- 登录方式切换 -->
      <div class="login-tabs">
        <button
          :class="['tab-btn', { active: loginMode === 'password' }]"
          @click="switchMode('password')"
        >
          密码登录
        </button>
        <button
          :class="['tab-btn', { active: loginMode === 'email' }]"
          @click="switchMode('email')"
        >
          验证码登录
        </button>
      </div>

      <el-form ref="formRef" :model="form" :rules="currentRules" label-position="top">
        <el-form-item label="电子邮箱" prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
            :prefix-icon="Message"
          />
        </el-form-item>

        <!-- 密码登录表单 -->
        <template v-if="loginMode === 'password'">
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
        </template>

        <!-- 验证码登录表单 -->
        <template v-else>
          <el-form-item label="验证码" prop="code">
            <div class="code-input">
              <el-input
                v-model="form.code"
                placeholder="请输入验证码"
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
        </template>

        <el-form-item>
          <InkButton primary @click="handleLogin" :disabled="loading" :loading="loading">
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
import { ref, reactive, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock, Key } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/modules/user'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import InkButton from '@/components/common/InkButton.vue'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const countdownTimer = ref(null)
const loginMode = ref('password') // 'password' | 'email'

const form = reactive({
  email: '',
  password: '',
  code: ''
})

// 邮箱格式验证
const isEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(form.email)
})

// 密码登录验证规则
const passwordRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

// 验证码登录验证规则
const emailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const currentRules = computed(() => {
  return loginMode.value === 'password' ? passwordRules : emailRules
})

function switchMode(mode) {
  loginMode.value = mode
  form.password = ''
  form.code = ''
  // 切换模式时清除验证状态
  formRef.value?.clearValidate()
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

async function handleLogin() {
  try {
    await formRef.value.validate()
    loading.value = true

    let res
    if (loginMode.value === 'password') {
      res = await userApi.adminPasswordLogin({
        email: form.email,
        password: form.password
      })
    } else {
      res = await userApi.adminEmailLogin({
        email: form.email,
        code: form.code
      })
    }

    // res 是 Result<T>，res.data 是 UserLoginResponse
    userStore.setToken(res.data.token)
    ElMessage.success('登录成功，欢迎回来！')
    router.push('/')
  } catch (error) {
    // 错误已在拦截器中处理
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

.login-tabs {
  display: flex;
  margin-bottom: 24px;
  border-bottom: 2px solid var(--ink-light);
}

.tab-btn {
  flex: 1;
  padding: 12px;
  background: transparent;
  border: none;
  font-family: var(--font-serif);
  font-size: 16px;
  color: var(--ink-secondary);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.tab-btn:hover {
  color: var(--ink-primary);
}

.tab-btn.active {
  color: var(--ink-primary);
  font-weight: 600;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--ink-primary);
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
  font-size: 14px;
  color: var(--ink-secondary);
}

.divider {
  margin: 0 12px;
}

@media (max-width: 480px) {
  .code-input {
    flex-direction: column;
  }
}
</style>
