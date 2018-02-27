import { assign } from 'lodash'
import {API_URL} from '../config'
import {RSAA, HTTPVerb, Types} from 'redux-api-middleware'
import * as Actions from '../constants/actions'
import {createAction} from 'redux-actions'
import {getUsername, getToken} from "../config"
import CryptoJS from 'crypto-js'
import {CommonData} from "../types/models"

export const buildAPIRequest =
        (endpoint: string, method: HTTPVerb,
         headers: { [propName: string]: string }, types: Types , body: FormData) => {
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

export const login = createAction<CommonData>(Actions.LOGIN_SUCCESS)
export const logout = createAction(Actions.LOGOUT)

export const toggleLoginFormDisplay = createAction<CommonData>(Actions.TOGGLE_LOGIN_FORM_DISPLAY)

/**
 * TODO: get API KEY from server after verification the username and password
 */
export const loginDisabled = (username: string, password: string) => {
  const endpoint = API_URL + '/login'
  const apiTypes = [
    Actions.LOGIN_REQUEST,
    {
      type: Actions.LOGIN_SUCCESS,
      payload: (action: typeof Actions, state: CommonData, res: Response) => {
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
  const digest = getAuthDigest(username, password, endpoint)
  const authContent = {Authentication: 'hmac ' + username + ':' + digest}
  const headers = assign(getJsonHeadersWithoutAuth(), authContent)
  return {
    [RSAA]: buildAPIRequest(endpoint, 'POST', headers, apiTypes, undefined)
  }
}
const getAuthDigest = (username: string, password: string, endpoint: string) => {
    const sha256 = CryptoJS.HmacSHA256(endpoint + '/' + username, password)
    return CryptoJS.enc.Base64.stringify(sha256)
}
