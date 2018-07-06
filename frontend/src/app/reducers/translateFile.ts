import {handleActions} from 'redux-actions'
import * as Actions from '../constants/actions'
import moment from 'moment'
import {
  ErrorData,
  InfoState,
  TranslateFileData,
  TranslateFileState
} from '../types/models'

const initialState: TranslateFileState = {
  errorData: null,
  uploading: false,
  loading: false,
  supportedLocales: []
}

const uploadFailed = (payload: TranslateFileData) => {
  const now = moment().utc().format('d/MM/YYYY hh:mm:ss')
  return {
    summary: 'Unable to translate file',
    message: payload.message,
    stack: payload.stack,
    timestamp: now,
    type: Actions.MSG_TYPE.ERROR
  }
}

const getSupportedLocalesFailed = (payload: TranslateFileData) => {
  const now = moment().utc().format('d/MM/YYYY hh:mm:ss')
  return {
    summary: 'Unable get supported locales',
    message: payload.message,
    stack: payload.stack,
    timestamp: now,
    type: Actions.MSG_TYPE.ERROR
  }
}

export default handleActions<InfoState, TranslateFileData>({
  [Actions.TRANSLATE_FILE_REQUEST]: (state: TranslateFileData, action) => {
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
  [Actions.TRANSLATE_FILE_SUCCESS]: (state: TranslateFileData) => {
    return {
      ...state,
      uploading: false,
      errorData: null
    }
  },
  [Actions.TRANSLATE_FILE_FAILED]: (state: TranslateFileData, action) => {
    return {
      ...state,
      uploading: false,
      errorData: uploadFailed(action.payload)
    }
  },
  [Actions.GET_SUPPORTED_LOCALES_REQUEST]: (state: TranslateFileData, action) => {
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
  [Actions.GET_SUPPORTED_LOCALES_SUCCESS]: (state: TranslateFileData, action) => {
    return {
      ...state,
      loading: false,
      supportedLocales: action.payload
    }
  },
  [Actions.GET_SUPPORTED_LOCALES_FAILED]: (state: TranslateFileData, action) => {
    return {
      ...state,
      loading: false,
      errorData: getSupportedLocalesFailed(action.payload)
    }
  }
}, initialState)
