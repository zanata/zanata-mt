/**
 * Typescript definition for DTO, reducers and payload
 */
import {MSG_TYPE} from '../constants/actions'

declare interface ErrorData {
  summary?: string,
  message?: string,
  timestamp: string,
  stack: string,
  type: MSG_TYPE
}
declare type ErrorData = ErrorData

/**
 * ReduxMiddleware type
 */
declare interface ReduxMiddlewareError {
    message?: string,
    name?: string,
    stack?: string
}

/**
 * State type for reducers/info
 */
declare interface InfoData extends ReduxMiddlewareError{
  loading?: boolean
  appName?: string,
  version?: string,
  buildDate?: string,
  devMode?: boolean,
  errorData?: ErrorData
}
declare type InfoState = InfoData

declare interface AuthData {
  username: string
}

/**
 * State type for reducers/commmon
 */
declare interface CommonData extends ReduxMiddlewareError {
  errorData?: ErrorData,
  loading?: boolean,
  auth?: AuthData
  showLoginForm?: boolean
}
declare type CommonState = CommonData

/**
 * Locale DTO: org.zanata.magpie.model.Locale
 */
declare interface Locale {
  localeCode: string,
  name: string
}

/**
 * List of Locale DTO
 */
declare interface LocaleArray {
  [index: number]: Locale
}

/**
 * State type for reducers/translateFile
 */
declare interface TranslateFileData extends ReduxMiddlewareError {
  errorData?: ErrorData,
  uploading: boolean,
  loading: boolean,
  supportedLocales: LocaleArray
}
declare type TranslateFileState = TranslateFileData

/**
 * Payload type for reducers/translateFile
 */
declare interface TranslateFilePayload extends ReduxMiddlewareError, LocaleArray {
}
