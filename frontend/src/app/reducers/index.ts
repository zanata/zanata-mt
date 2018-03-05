import { combineReducers } from 'redux'
import info from './info'
import common from './common'
import {CommonState, InfoState} from "../types/models"

export interface RootState {
  info: InfoState
  common: CommonState
}

export default combineReducers<RootState>({
  info,
  common
})
