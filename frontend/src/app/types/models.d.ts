import {MSG_TYPE} from '../constants/actions'

declare interface ErrorData {
  summary?: string,
  message?: string,
  timestamp: string,
  stack: string,
  type: MSG_TYPE
}
declare type ErrorData = ErrorData

declare interface ReduxMiddlewareError {
    message?: string,
    name?: string,
    stack?: string
}

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
  username: string,
  password: string
}

declare interface CommonData extends ReduxMiddlewareError {
  errorData?: ErrorData,
  loading?: boolean,
  auth?: AuthData
  showLoginForm?: boolean
}
declare type CommonState = CommonData
