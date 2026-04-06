<template>
  <div class="signup-view fade-in">
    <NewspaperTitle
      title="新读者登记"
      subtitle="NEW READER REGISTRATION"
    />

    <div class="form-container paper-card double-border">
      <!-- 注册方式切换 -->
      <div class="signup-tabs">
        <button
          :class="['tab-btn', { active: signupMode === 'email' }]"
          @click="switchMode('email')"
        >
          邮箱验证码注册
        </button>
        <button
          :class="['tab-btn', { active: signupMode === 'password' }]"
          @click="switchMode('password')"
        >
          用户名密码注册
        </button>
      </div>

      <el-form ref="formRef" :model="form" :rules="currentRules" label-position="top">
        <!-- 用户名密码注册表单 -->
        <template v-if="signupMode === 'password'">
          <el-form-item label="用户名" prop="userName">
            <el-input
              v-model="form.userName"
              placeholder="请输入用户名"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
        </template>

        <!-- 邮箱验证码注册表单 -->
        <template v-else>
          <el-form-item label="用户名" prop="userName">
            <el-input
              v-model="form.userName"
              placeholder="请输入用户名"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item label="电子邮箱" prop="email">
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱"
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

        <el-form-item>
          <el-checkbox v-model="agreedTerms">
            我已阅读并同意
            <a href="#" class="newspaper-link" @click.prevent="showTerms">服务条款</a>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <InkButton primary @click="handleSignup" :disabled="loading || !agreedTerms" :loading="loading">
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
import { ref, reactive, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Key } from '@element-plus/icons-vue'
import { userApi } from '@/api/modules/user'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import InkButton from '@/components/common/InkButton.vue'

const router = useRouter()

const formRef = ref()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const countdownTimer = ref(null)
const agreedTerms = ref(false)
const signupMode = ref('email') // 'email' | 'password'

const form = reactive({
  userName: '',
  email: '',
  code: '',
  password: '',
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
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 用户名密码注册验证规则
const passwordRules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 邮箱验证码注册验证规则
const emailRules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const currentRules = computed(() => {
  return signupMode.value === 'password' ? passwordRules : emailRules
})

function switchMode(mode) {
  signupMode.value = mode
  // 重置表单
  form.email = ''
  form.code = ''
  form.confirmPassword = ''
  formRef.value?.clearValidate()
}

function showTerms() {
  ElMessage.info('服务条款页面开发中...')
}

async function sendCode() {
  if (!isEmailValid.value) {
    ElMessage.warning('请先输入正确的邮箱地址')
    return
  }

  sendingCode.value = true
  try {
    await userApi.sendEmailCode(form.email)
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

async function handleSignup() {
  try {
    await formRef.value.validate()

    if (!agreedTerms.value) {
      ElMessage.warning('请先同意服务条款')
      return
    }

    loading.value = true

    if (signupMode.value === 'password') {
      // 用户名密码注册
      await userApi.register({
        userName: form.userName,
        password: form.password
      })
    } else {
      // 邮箱验证码注册
      await userApi.emailRegister({
        userName: form.userName,
        email: form.email,
        code: form.code,
        password: form.password
      })
    }

    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    // 错误已在拦截器中处理
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

.signup-tabs {
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
  font-size: 14px;
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

@media (max-width: 480px) {
  .signup-tabs {
    flex-direction: column;
  }

  .tab-btn.active::after {
    display: none;
  }

  .tab-btn.active {
    background: var(--paper-dark);
  }

  .code-input {
    flex-direction: column;
  }
}
</style>
