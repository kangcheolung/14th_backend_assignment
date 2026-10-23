const TOKEN_KEY = 'accessToken'

type Listener = () => void

const listeners = new Set<Listener>()

function notify() {
  listeners.forEach((listener) => listener())
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
  notify()
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  notify()
}

// useSyncExternalStore 용: 토큰이 바뀌면 화면이 같이 바뀌게 한다 (401 로 토큰이 지워질 때도 포함)
export function subscribeToken(listener: Listener) {
  listeners.add(listener)
  return () => {
    listeners.delete(listener)
  }
}
