import { mapValues, isEmpty } from 'lodash'

const rootId = 'root'
const root = document.getElementById(rootId)

export const API_URL = root.getAttribute('data-api-url')

export const isLoggedIn = () => {
  return !isEmpty(getUsername()) && !isEmpty(getToken())
}

export const setAuth = (username: string, token: string) => {
  sessionStorage.setItem('username', username)
  sessionStorage.setItem('token', token)
}

export const logout = () => {
  sessionStorage.removeItem('username')
  sessionStorage.removeItem('token')
}

export const getUsername = () => {
  return sessionStorage.getItem('username')
}

export const getToken = () => {
  return sessionStorage.getItem('token')
}
