///<reference path="../types/models.d.ts"/>
import { handleActions } from 'redux-actions';
import * as Actions from '../constants/actions';

const initialState: InfoState = {
  name: null,
  version: null,
  buildDate: null,
  devMode: true
};

export default handleActions<InfoState, InfoData>({
  [Actions.GET_INFO_REQUEST]: (state, action) => {
    return {
     ...action.payload,
     ...state
    }
  },

  [Actions.GET_INFO_SUCCESS]: (state, action) => {
    return {
      name: action.payload.name,
      version: action.payload.version,
      buildDate: action.payload.buildDate,
      devMode: action.payload.devMode
    }
  },

  [Actions.GET_INFO_FAILED]: (state, action) => {
    return initialState

  },
}, initialState);
