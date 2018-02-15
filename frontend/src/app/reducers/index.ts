import { combineReducers, Reducer } from 'redux';
import todos from './todos';
import info from './info';

export interface RootState {
  todos: TodoStoreState
  info: InfoState
}

export default combineReducers<RootState>({
  todos,
  info
});
