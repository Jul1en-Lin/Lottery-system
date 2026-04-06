<template>
  <div class="forgot-password-view fade-in">
    <NewspaperTitle
      title="密码重置申请"
      subtitle="PASSWORD RESET REQUEST"
    />

    <div class="form-container paper-card double-border">
      <p class="form-desc">请输入您的注册邮箱，我们将发送密码重置链接。</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入注册邮箱" />
        </el-form-item>

        <el-form-item>
          <InkButton primary @click="handleSubmit" :disabled="loading">
            {{ loading ? '发送中...' : '提 交 申 请' }}
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
import { ref, reactive } from 'vue'
import { userApi } from '@/api/modules/user'
import NewspaperTitle from '@/components/common/NewspaperTitle.vue'
import InkButton from '@/components/common/InkButton.vue'

const formRef = ref()
const loading = ref(false)

const form = reactive({
  email: ''
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
    loading.value = true

    await userApi.sendEmailCode(form.email)
    alert('重置链接已发送到您的邮箱')
  } catch (error) {
    console.error('发送失败', error)
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

.form-desc {
  color: var(--ink-secondary);
  margin-bottom: 24px;
}

.form-links {
  text-align: center;
  margin-top: 24px;
}
</style>
