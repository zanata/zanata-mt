import {handleActions} from 'redux-actions'
import * as Actions from '../constants/actions'
import moment from 'moment'
import {
  TranslateFilePayload,
  TranslateFileState
} from '../types/models'

const initialState: TranslateFileState = {
  errorData: null,
  uploading: false,
  loading: false,
  supportedLocales: []
}

const uploadFailed = (payload: TranslateFilePayload) => {
  const now = moment().utc().format('d/MM/YYYY hh:mm:ss')
  return {
    summary: 'Unable to translate file',
    message: payload.message,
    stack: payload.stack,
    timestamp: now,
    type: Actions.MSG_TYPE.ERROR
  }
}

const getSupportedLocalesFailed = (payload: TranslateFilePayload) => {
  const now = moment().utc().format('d/MM/YYYY hh:mm:ss')
  return {
    summary: 'Unable get supported locales',
    message: payload.message,
    stack: payload.stack,
    timestamp: now,
    type: Actions.MSG_TYPE.ERROR
  }
}

export default handleActions<TranslateFileState, TranslateFilePayload>({
  [Actions.TRANSLATE_FILE_REQUEST]: (state, action) => {
    if (action.error) {
      return {
        ...state,
        uploading: false,
        errorData: uploadFailed(action.payload)
      }
    } else {
      return {
        ...state,
        ...action.payload,
        uploading: true
      }
    }
  },
  [Actions.TRANSLATE_FILE_SUCCESS]: (state) => {
    return {
      ...state,
      uploading: false,
      errorData: null
    }
  },
  [Actions.TRANSLATE_FILE_FAILED]: (state, action) => {
    return {
      ...state,
      uploading: false,
      errorData: uploadFailed(action.payload)
    }
  },
  [Actions.GET_SUPPORTED_LOCALES_REQUEST]: (state, action) => {
    if (action.error) {
      return {
        ...state,
        loading: false,
        errorData: getSupportedLocalesFailed(action.payload)
      }
    } else {
      return {
        ...state,
        loading: true
      }
    }
  },
  [Actions.GET_SUPPORTED_LOCALES_SUCCESS]: (state, action) => {
    return {
      ...state,
      loading: false,
      errorData: null,
      supportedLocales: action.payload
    }
  },
  [Actions.GET_SUPPORTED_LOCALES_FAILED]: (state, action) => {
    return {
      ...state,
      loading: false,
      errorData: getSupportedLocalesFailed(action.payload)
    }
  }
}, initialState)
