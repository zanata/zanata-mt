import * as React from 'react'
import * as ReactDOM from 'react-dom'
import {Provider} from 'react-redux'
import {Router, Route, Switch} from 'react-router'
import {createBrowserHistory} from 'history'
import {apiMiddleware} from 'redux-api-middleware'
import {createLogger} from 'redux-logger'
import {createStore, applyMiddleware, compose} from 'redux'
import rootReducer, {RootState} from './reducers'
import {App, Info, NoMatch} from './containers'
import thunk from 'redux-thunk'

import './styles/index.less'

function configureStore(initialState?: RootState) {
  const logger = createLogger({
    predicate: (getState, action) =>
      process.env && (process.env.NODE_ENV === 'development')
  })

  const finalCreateStore = compose(
    applyMiddleware(
      thunk,
      apiMiddleware,
      logger
    )
  )(createStore)

  const store2 = ((initialState2) => {
    const finalStore = finalCreateStore(rootReducer, initialState2)
    if (module.hot) {
      // Enable Webpack hot module replacement for reducers
      module.hot.accept('./reducers', () => {
        const nextRootReducer = require('./reducers')
        finalStore.replaceReducer(nextRootReducer)
      })
    }
    return finalStore
  })()

  return store2
}

const store = configureStore();
const history = createBrowserHistory();

ReactDOM.render(
  <Provider store={store}>
    <Router history={history}>
      <Switch>
        <Route exact path="/" component={App}/>
        <Route path="/info" component={Info}/>
        <Route component={NoMatch}/>
      </Switch>
    </Router>
  </Provider>,
  document.getElementById('root')
);
