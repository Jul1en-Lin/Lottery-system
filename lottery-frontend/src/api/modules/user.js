import api from '../index'

export const userApi = {
  // 用户名密码注册
  register(data) {
    return api.post('/user/register', data)
  },

  // 发送邮箱验证码
  sendEmailCode(email) {
    return api.post('/user/sendEmailCode', null, { params: { email } })
  },

  // 邮箱验证码注册
  emailRegister(data) {
    return api.post('/user/emailRegister', data)
  },

  // 发送管理员邮箱验证码
  sendAdminEmailCode(email) {
    return api.post('/user/admin/sendEmailCode', null, { params: { email } })
  },

  // 管理员邮箱登录
  adminEmailLogin(data) {
    return api.post('/user/admin/emailLogin', data)
  },

  // 管理员密码登录
  adminPasswordLogin(data) {
    return api.post('/user/admin/passwordLogin', data)
  },

  // 获取用户列表
  getUserList(identity) {
    return api.get('/user/getListInfo', { params: { identity } })
  }
}
