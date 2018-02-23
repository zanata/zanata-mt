import { combineReducers } from 'redux'
import todos from './todos'
import info from './info'
import common from './common'

export interface RootState {
  todos: TodoStoreState
  info: InfoState
  common: CommonState
}

export default combineReducers<RootState>({
  todos,
  info,
  common
})
