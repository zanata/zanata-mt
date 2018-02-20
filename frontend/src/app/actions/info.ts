import { API_URL } from "../config"
import * as Actions from '../constants/actions'
import {
  buildAPIRequest,
  getJsonHeaders,
  getJsonHeadersWithoutAuth
} from './common'
import { RSAA } from 'redux-api-middleware'

const getInfoAPI = () => {
  const endpoint = API_URL + '/info'
  const apiTypes = [
    Actions.GET_INFO_REQUEST,
    {
      type: Actions.GET_INFO_SUCCESS,
      payload: (action, state, res) => {
        return res.json().then((json) => {
          return json
        })
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    Actions.GET_INFO_FAILED
  ]
  return {
    [RSAA]: buildAPIRequest(endpoint, 'GET', getJsonHeadersWithoutAuth(), apiTypes, undefined)
  }
}

export const getInfo = () => {
  return (dispatch, getState) => {
    dispatch(getInfoAPI())
  }
}
