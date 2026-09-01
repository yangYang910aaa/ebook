import { notification } from 'ant-design-vue'

let socket: WebSocket | null = null

export function connectWebSocket(token: string) {
  if (!token || socket) return
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  try {
    socket = new WebSocket(`${protocol}://${window.location.host}/ws/${token}`)
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
    socket.onclose = () => {
      socket = null
    }
    socket.onerror = () => {
      socket = null
    }
  } catch {
    notification.warning({ message: '提示', description: '浏览器不支持 WebSocket 或连接失败' })
  }
}

export function closeWebSocket() {
  if (socket) {
    socket.close()
    socket = null
  }
}
