import { assign } from 'lodash'
import {API_URL} from '../config'
import {RSAA, HTTPVerb, Types} from 'redux-api-middleware'
import * as Actions from '../constants/actions'
import {createAction} from 'redux-actions'
import {getUsername, getToken} from "../config"
import CryptoJS from 'crypto-js'
import {AuthData, CommonData} from "../types/models"

export const buildAPIRequest =
        (endpoint: string, method: HTTPVerb,
         headers: { [propName: string]: string }, types: Types , body?: FormData) => {
  const result = {
    endpoint,
    method,
    headers,
    credentials: 'include',
    types,
    body
  }
  return result
}

export const getJsonHeadersWithoutAuth = () => {
  return buildJsonHeaders()
}

export const getJsonHeaders = () => {
  return assign(getAuthHeaders(), buildJsonHeaders())
}

const buildJsonHeaders = () => {
  return {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  }
}

export const getAuthHeaders = () => {
  return {
    'x-auth-user': getUsername(),
    'x-auth-token': getToken(),
  }
}

export const logout = createAction(Actions.LOGOUT)

export const toggleLoginFormDisplay = createAction<CommonData>(Actions.TOGGLE_LOGIN_FORM_DISPLAY)

export const login = (auth: AuthData) => {
  const {username, password} = auth
  const endpoint = API_URL + '/account/login'
  const apiTypes = [
    Actions.LOGIN_REQUEST,
    {
      type: Actions.LOGIN_SUCCESS,
      payload: (action: typeof Actions, state: CommonData, res: Response) => {
        return {
          auth: {username, password}
        }
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    Actions.LOGIN_FAILED
  ]
  const digest = getAuthDigest(username, password, endpoint)
  const authContent = {Authentication: 'hmac ' + username + ':' + digest}
  const headers = assign(getJsonHeadersWithoutAuth(), authContent)
  return {
    [RSAA]: buildAPIRequest(endpoint, 'POST', headers, apiTypes)
  }
}
const getAuthDigest = (username: string, password: string, endpoint: string) => {
  return CryptoJS.AES.encrypt(password, endpoint + '/' + username)
}
