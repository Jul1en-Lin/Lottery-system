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
