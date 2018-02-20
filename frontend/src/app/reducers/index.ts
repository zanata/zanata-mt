import { combineReducers } from 'redux'
import todos from './todos'
import info from './info'

export interface RootState {
  todos: TodoStoreState
  info: InfoState
  errorData: ErrorData
}

export default combineReducers<RootState>({
  todos,
  info
})
