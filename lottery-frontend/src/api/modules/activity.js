import api from '../index'

export const activityApi = {
  // 获取活动列表
  getActivityList(page = 1, size = 10) {
    return api.get('/activity/queryList', {
      params: { page, size }
    })
  },

  // 获取活动详情
  getActivityDetail(activityId) {
    return api.get('/activity/getDetail', {
      params: { activityId }
    })
  },

  // 创建活动
  createActivity(data) {
    return api.post('/activity/create', data)
  }
}
