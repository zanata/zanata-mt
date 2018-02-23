import {handleActions} from "redux-actions"
import * as Actions from '../constants/actions'
import moment from "moment"
import {getToken, getUsername, logout, setAuth} from '../config'

const initialState: CommonState = {
  errorData: null,
  loading: null,
  auth: null
}

const loginFailed = (payload) => {
  const now = moment().utc().format('d/MM/YYYY hh:mm:ss')
  return {
    summary: 'Invalid username and password',
    message: payload.message,
    stack: payload.stack,
    timestamp: now,
    type: Actions.MSG_TYPE.ERROR
  }
}

export default handleActions<CommonState, CommonData>({
  [Actions.LOGIN_REQUEST]: (state, action) => {
    if (action.error) {
      return {
        ...initialState,
        errorData: loginFailed(action.payload),
        loading: false
      }
    } else {
      return {
        ...action.payload,
        loading: true,
        ...state
      }
    }
  },

  [Actions.LOGIN_SUCCESS]: (state, action) => {
    setAuth(action.payload.auth.username, action.payload.auth.password)
    return {
      errorData: null,
      loading: false,
      auth: null
    }
  },

  [Actions.LOGIN_FAILED]: (state, action) => {
    return {
      ...initialState,
      errorData: loginFailed(action.payload),
      loading: false
    }
  },

  [Actions.LOGOUT]: (state, action) => {
    logout()
    return {
      ...initialState
    }
  }
}, initialState)
