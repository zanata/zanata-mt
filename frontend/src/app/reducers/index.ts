import { combineReducers } from 'redux'
import info from './info'
import common from './common'
import translateFile from './translateFile'
import {CommonState, InfoState, TranslateFileState} from "../types/models"

export interface RootState {
  info: InfoState
  common: CommonState
  translateFile: TranslateFileState
}

export default combineReducers<RootState>({
  info,
  common,
  translateFile
})
