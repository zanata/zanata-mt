import { assign } from 'lodash'
import {API_URL} from "../config"
import { RSAA } from 'redux-api-middleware'
import * as Actions from "../constants/actions"
import {createAction} from "redux-actions"

export const buildAPIRequest = (endpoint, method, headers, types, body) => {
  const result = {
    endpoint,
    method,
    headers,
    credentials: 'include',
    types,
    body: null
  }

  if (body) {
    result.body = body
  }
  return result
}

export const getJsonHeadersWithoutAuth = () => {
  return buildJsonHeaders()
}

export const getJsonHeaders = () => {
  const result = assign(getHeaders(), buildJsonHeaders())
  return result
}

const buildJsonHeaders = () => {
  return {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  }
}

export const getHeaders = () => {
  return {
    'x-auth-user': sessionStorage.getItem('username'),
    'x-auth-token': sessionStorage.getItem('token'),
  }
}

export const login = createAction<AuthData>('LOGIN')
export const logout = createAction(Actions.LOGOUT)

export const loginDisabled = (username, password) => {
  const endpoint = API_URL + '/login'
  // const data = new FormData()
  // data.append('username', username)
  // data.append('password', password)

  const apiTypes = [
    Actions.LOGIN_REQUEST,
    {
      type: Actions.LOGIN_SUCCESS,
      payload: (action, state, res) => {
        return {
          username,
          password
        }
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    Actions.LOGIN_FAILED
  ]
  return {
    [RSAA]: buildAPIRequest(endpoint, 'POST', getJsonHeadersWithoutAuth(), apiTypes, undefined)
  }
}
