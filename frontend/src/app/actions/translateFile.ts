import { API_URL } from '../config'
import * as Actions from '../constants/actions'
import {
  buildAPIRequest, getJsonHeaders
} from './common'
import { RSAA } from 'redux-api-middleware'
import {TranslateFileData} from "../types/models"
import {Action} from "redux-actions"
import {RootState} from "../reducers"
import {GenericDispatch} from "react-redux"
import { saveAs } from 'file-saver'
import { isEmpty } from 'lodash'

const FILE_NAME_PATTERN = /(attachment; filename=[\"|\'])(.+)(\"|\')/

const translateFileAPI = (file: File, fromLocaleCode: string, toLocaleCode: string, type: string) => {
  const endpoint = API_URL + '/document/translate/file?fromLocaleCode=' +
    fromLocaleCode + '&toLocaleCode=' + toLocaleCode
  const formData = new FormData()
  formData.append('file', file, file.name)
  formData.append('fileName', file.name)
  formData.append('type', type)
  const apiTypes = [
    Actions.TRANSLATE_FILE_REQUEST,
    {
      type: Actions.TRANSLATE_FILE_SUCCESS,
      payload: (action: typeof Actions, state: TranslateFileData, res: Response) => {
        const contentDisposition = res.headers.get('content-disposition')
        return res.blob().then((blob) => {
          const fileName = generateFileName(file.name, toLocaleCode,
            contentDisposition, type)
          saveAs(blob, fileName)
        })
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    Actions.TRANSLATE_FILE_FAILED
  ]
  return {
    [RSAA]: buildAPIRequest(endpoint, 'POST', {}, apiTypes, formData)
  }
}

export const translateFile = (file: File, fromLocaleCode: string, toLocaleCode: string, type: string) => {
  return (dispatch: GenericDispatch, getState: () => Action<RootState>) => {
    dispatch(translateFileAPI(file, fromLocaleCode, toLocaleCode, type))
  }
}

export const fetchSupportedLocales = () => {
  const endpoint = API_URL + '/languages'

  const apiTypes = [
    Actions.GET_SUPPORTED_LOCALES_REQUEST,
    {
      type: Actions.GET_SUPPORTED_LOCALES_SUCCESS,
      payload: (action: typeof Actions, state: TranslateFileData, res: Response) => {
        return res.json().then((json) => {
          return json
        })
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    Actions.GET_SUPPORTED_LOCALES_FAILED
  ]
  return {
    [RSAA]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

const generateFileName = (filename: string, toLocaleCode: string, header: string, type: string) => {
  if (!isEmpty(header)) {
    const matches = FILE_NAME_PATTERN.exec(header)
    if (matches != null && !isEmpty(matches) && !isEmpty(matches[2])) {
      return matches[2]
    }
  }
  const filenameText = filename.replace(/\.[^/.]+$/, '')
  return filenameText + '_' + toLocaleCode + '.' + type
}
