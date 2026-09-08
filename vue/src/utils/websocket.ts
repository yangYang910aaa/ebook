/* WebSocket 工具：登录后建立长连接接收服务端通知，以系统通知形式弹出；单例模式，全局仅一个连接 */
import { notification } from 'ant-design-vue'

// 全局唯一 WebSocket 实例
let socket: WebSocket | null = null

/**
 * 建立 WebSocket 连接
 * @param token 登录令牌，用于服务端鉴权
 * 连接地址根据当前页面协议自动选择 ws/wss，路径为 /ws/{token}
 */
export function connectWebSocket(token: string) {
  // 已连接或无 token 时不重复创建
  if (!token || socket) return
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  try {
    socket = new WebSocket(`${protocol}://${window.location.host}/ws/${token}`)
    // 收到消息：解析 JSON，若含 message 字段则弹出系统通知
    socket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.message) {
          notification.open({ message: '通知', description: data.message })
        }
      } catch {
        // 忽略无法解析的消息
      }
    }
    // 连接关闭时清空实例，允许后续重新连接
    socket.onclose = () => {
      socket = null
    }
    // 连接出错时清空实例
    socket.onerror = () => {
      socket = null
    }
  } catch {
    notification.warning({ message: '提示', description: '浏览器不支持 WebSocket 或连接失败' })
  }
}

/** 关闭 WebSocket 连接（退出登录时调用） */
export function closeWebSocket() {
  if (socket) {
    socket.close()
    socket = null
  }
}
