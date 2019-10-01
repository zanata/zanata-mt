import * as React from 'react'
import * as ReactDOM from 'react-dom'
import {Provider} from 'react-redux'
import {
  Router,
  Route,
  Switch,
  Redirect, RouteComponentProps, RouteProps,
} from 'react-router'
import history from './history'
import {apiMiddleware} from 'redux-api-middleware'
import {createLogger} from 'redux-logger'
import {createStore, applyMiddleware, compose} from 'redux'
import rootReducer, {RootState} from './reducers'
import {App, Info, NavBar, Editor, TranslateFile} from './containers'
import thunk from 'redux-thunk'
import { NoMatch, Health } from './components'
import { Layout } from 'antd'

import './styles/index.less'
import {Component} from 'react'

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

  const store = ((initialState?: RootState) => {
    const finalStore = finalCreateStore(rootReducer, initialState)
    if (module.hot) {
      // Enable Webpack hot module replacement for reducers
      module.hot.accept('./reducers', () => {
        const nextRootReducer = require('./reducers')
        finalStore.replaceReducer(nextRootReducer)
      })
    }
    return finalStore
  })()

  return store
}

const store = configureStore();

type RouteComponent = React.StatelessComponent<RouteComponentProps<{}>> | React.ComponentClass<any>
const PrivateRoute: React.StatelessComponent<RouteProps> = ({component, ...rest}) => {
  const renderFn = (Component?: RouteComponent) => (props: RouteProps) => {
    const auth = store.getState().common.auth
    const loggedIn = auth && auth.username
    if (!Component) {
      return null
    } else if (loggedIn) {
      return <Component {...props} />
    } else {
      return <Redirect to='/app/error'/>
    }
  }
  return <Route {...rest} render={renderFn(component)} />
}

ReactDOM.render(
  <Provider store={store}>
    <Router history={history}>
      <Layout style={{ minHeight: '100vh' }}>
        <NavBar />
        <Layout>
          <Switch>
            <Route exact strict path="(/|/app|/app/)" component={App} />
            <Route path='/app/info' component={Info}/>
            <Route path='/app/editor' component={Editor}/>
            <Route path='/app/health' component={Health}/>
            <Route path='/app/error' component={NoMatch}/>
            <PrivateRoute path='/app/translate' component={TranslateFile} />
            <Route component={NoMatch}/>
          </Switch>
        </Layout>
      </Layout>
    </Router>
  </Provider>,
  document.getElementById('root')
);

