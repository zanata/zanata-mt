import { assign } from 'lodash'
import {API_URL} from '../config'
import {RSAA, HTTPVerb, Types} from 'redux-api-middleware'
import * as Actions from '../constants/actions'
import {createAction} from 'redux-actions'
import {CommonData} from "../types/models"

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

export const getJsonHeaders = () => {
  return buildJsonHeaders()
}

const buildJsonHeaders = () => {
  return {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  }
}

export const toggleLoginFormDisplay = createAction<CommonData>(Actions.TOGGLE_LOGIN_FORM_DISPLAY)

export const login = (username: string, password: string) => {
  const endpoint = API_URL + '/account/login'
  const apiTypes = [
    Actions.LOGIN_REQUEST,
    {
      type: Actions.LOGIN_SUCCESS,
      payload: (action: typeof Actions, state: CommonData, res: Response) => {
        return {
          auth: {
            username: {username}
          }
        }
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    Actions.LOGIN_FAILED
  ]
  const base64 = btoa(username + ':' + password)
  const authContent = {Authorization: 'Basic ' + base64}
  const headers = assign(getJsonHeaders(), authContent)
  return {
    [RSAA]: buildAPIRequest(endpoint, 'POST', headers, apiTypes)
  }
}

export const logout = () => {
  const endpoint = API_URL + '/account/logout'
  const apiTypes = [
    Actions.LOGOUT_REQUEST,
    Actions.LOGOUT_SUCCESS,
    Actions.LOGIN_FAILED
  ]
  return {
    [RSAA]: buildAPIRequest(endpoint, 'POST', getJsonHeaders(), apiTypes)
  }
}
