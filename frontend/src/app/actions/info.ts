import { API_URL } from '../config'
import * as Actions from '../constants/actions'
import {
  buildAPIRequest,
  getJsonHeadersWithoutAuth
} from './common'
import { RSAA } from 'redux-api-middleware'
import {CommonData} from "../types/models"
import {Action} from "redux-actions"
import {RootState} from "../reducers"
import {GenericDispatch} from "react-redux"

const getInfoAPI = () => {
  const endpoint = API_URL + '/info'
  const apiTypes = [
    Actions.GET_INFO_REQUEST,
    {
      type: Actions.GET_INFO_SUCCESS,
      payload: (action: typeof Actions, state: CommonData, res: Response) => {
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
    [RSAA]: buildAPIRequest(endpoint, 'GET', getJsonHeadersWithoutAuth(), apiTypes)
  }
}

export const getInfo = () => {
  return (dispatch: GenericDispatch, getState: () => Action<RootState>) => {
    dispatch(getInfoAPI())
  }
}
