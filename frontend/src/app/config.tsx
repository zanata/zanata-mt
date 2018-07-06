import { isEmpty } from 'lodash'
import Cookies from 'universal-cookie'


const maxAge = 21600 // 6hours in seconds
const rootId = 'root'
const root = document.getElementById(rootId)
const cookies = new Cookies()
export const API_URL = root.getAttribute('data-api-url')

export const isLoggedIn = ():boolean => {
  return !isEmpty(getUsername()) && !isEmpty(getToken())
}

export const setAuth = (username: string, token: string) => {
  cookies.set('username', username, {maxAge: maxAge, path: '/' })
  cookies.set('token', token, {maxAge: maxAge, path: '/'})
}

export const logout = () => {
  cookies.remove('username', {path: '/'})
  cookies.remove('token', {path: '/'})
}

export const getUsername = ():string => {
  return cookies.get('username', {path: '/'})
}

export const getToken = ():string => {
  return cookies.get('token', {path: '/'})
}
