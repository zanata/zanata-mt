import {handleActions} from 'redux-actions'
import * as Actions from '../constants/actions'
import moment from 'moment'
import {CommonData, CommonState} from '../types/models'

const initialState: CommonState = {
  errorData: null,
  loading: null,
  auth: null,
  showLoginForm: false
}

const loginFailed = (payload: CommonData) => {
  const now = moment().utc().format()
  return {
    summary: 'Invalid username and password',
    message: '',
    stack: '',
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
        loading: false,
        showLoginForm: true
      }
    } else {
      return {
        ...state,
        ...action.payload,
        loading: true
      }
    }
  },

  [Actions.LOGIN_SUCCESS]: (state, action) => {
    if (action.error) {
      return {
        ...initialState,
        errorData: loginFailed(action.payload),
        loading: false,
        showLoginForm: true
      }
    } else {
      return {
        errorData: null,
        loading: false,
        auth: {
          username: action.payload.auth.username
        },
        showLoginForm: false
      }
    }
  },

  [Actions.LOGIN_FAILED]: (state, action) => {
    return {
      ...initialState,
      errorData: loginFailed(action.payload),
      loading: false,
      showLoginForm: true
    }
  },

  [Actions.LOGOUT_SUCCESS]: (state, action) => {
    return {
      ...state,
      auth: null
    }
  },
  [Actions.LOGOUT_FAILED]: (state, action) => {
    return {
      ...state,
      auth: null
    }
  },
  [Actions.LOGOUT_REQUEST]: (state, action) => {
    return {
      ...state,
      auth: null
    }
  },

  [Actions.TOGGLE_LOGIN_FORM_DISPLAY]: (state, action) => {
    return {
      ...state,
      showLoginForm: action.payload.showLoginForm
    }
  }
}, initialState)
