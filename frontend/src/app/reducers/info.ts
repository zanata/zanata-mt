import { handleActions } from 'redux-actions'
import * as Actions from '../constants/actions'
import moment from 'moment'

const initialState: InfoState = {
  loading: null,
  appName: null,
  version: null,
  buildDate: null,
  devMode: true,
  errorData: null,
  message: null,
  name: null,
  stack: null
}

const getInfoFailed = (payload) => {
  const now = moment().utc().format('d/MM/YYYY hh:mm:ss')
  return {
    summary: 'Unable to fetch info',
    message: payload.message,
    stack: payload.stack,
    timestamp: now,
    type: Actions.MSG_TYPE.ERROR
  }
}

export default handleActions<InfoState, InfoData>({
  [Actions.GET_INFO_REQUEST]: (state, action) => {
    if (action.error) {
      return {
        ...initialState,
        errorData: getInfoFailed(action.payload)
      }
    } else {
      return {
        ...action.payload,
        loading: true,
        ...state
      }
    }
  },

  [Actions.GET_INFO_SUCCESS]: (state, action) => {
    return {
      loading: false,
      appName: action.payload.name,
      version: action.payload.version,
      buildDate: action.payload.buildDate,
      devMode: action.payload.devMode,
      errorData: null
    }
  },

  [Actions.GET_INFO_FAILED]: (state, action) => {
    return {
      ...initialState,
      errorData: getInfoFailed(action.payload)
    }
  },
}, initialState)
