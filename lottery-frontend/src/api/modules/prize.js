import api from '../index'

export const prizeApi = {
  // 获取奖品列表
  getPrizeList(page = 1, size = 10) {
    return api.get('/prize/getList', {
      params: { page, size }
    })
  },

  // 抽奖
  drawPrize(data) {
    return api.post('/drawPrize', data)
  },

  // 获取中奖记录
  getWinningRecords(data) {
    return api.post('/getWinningRecords', data)
  }
}
