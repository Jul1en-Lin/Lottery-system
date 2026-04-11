/**
 * 解码 JWT token 获取 payload
 * @param {string} token
 * @returns {object|null}
 */
export function decodeJwt(token) {
  if (!token) return null
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
  } catch (e) {
    console.error('JWT 解码失败', e)
    return null
  }
}

/**
 * 检查 token 是否过期
 * @param {string} token
 * @returns {boolean}
 */
export function isTokenExpired(token) {
  const payload = decodeJwt(token)
  if (!payload || !payload.exp) return true
  return payload.exp * 1000 < Date.now()
}

/**
 * 奖品等级映射表
 */
const tierNameMap = {
  // 数字映射
  0: '特等奖',
  1: '一等奖',
  2: '二等奖',
  3: '三等奖',
  // 枚举名称映射
  'TIER_SPECIAL': '特等奖',
  'TIER_1': '一等奖',
  'TIER_2': '二等奖',
  'TIER_3': '三等奖',
  // 字符串数字映射
  '0': '特等奖',
  '1': '一等奖',
  '2': '二等奖',
  '3': '三等奖'
}

/**
 * 获取奖品等级的中文名称
 * @param {string|number} tier - 奖品等级（可能是 TIER_1, 1, "1" 等）
 * @returns {string}
 */
export function getTierName(tier) {
  if (!tier && tier !== 0) return '-'
  return tierNameMap[tier] || tierNameMap[String(tier)] || `${tier}等奖`
}
