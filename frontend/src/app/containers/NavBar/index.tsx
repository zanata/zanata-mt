import * as React from 'react';
import {connect, GenericDispatch} from 'react-redux'
import {RootState} from "../../reducers/index"
import { Link } from 'react-router-dom'
import {LoginForm} from "../../containers/LoginForm/index"
import {Action} from "redux-actions"
import { login, logout, toggleLoginFormDisplay } from '../../actions/common'
import { isLoggedIn } from '../../config'

interface Props {
  isLoggedIn?: boolean,
  showLoginForm?: boolean,
  handleLogout?: () => Action<void>,
  handleLogin?: (username: string, password: string) => Action<void>,
  handleSetLoginFormDisplay?: (display: boolean) => Action<void>
}

export interface State {
  show: boolean
}

const items = [
  {name: 'Home', url: '/'},
  {name: 'Info', url: '/app/info'}
]

@connect(mapStateToProps, mapDispatchToProps)
export class NavBar extends React.Component<Props, State> {
  constructor(props?: Props, context?: any) {
    super(props, context);
    this.state = {
      show: false
    }
    this.toggleNav = this.toggleNav.bind(this)
    this.showLoginForm = this.showLoginForm.bind(this)
    this.hideLoginForm = this.hideLoginForm.bind(this)
  }

  private toggleNav(e: React.MouseEvent<HTMLElement>) {
    this.setState({
      show: !this.state.show
    })
  }

  private showLoginForm(e: React.MouseEvent<HTMLElement>) {
    this.props.handleSetLoginFormDisplay(true)
  }

  private hideLoginForm() {
    this.props.handleSetLoginFormDisplay(false)
  }

  public render() {
    const path = window.location.pathname
    const {showLoginForm, handleLogin, handleLogout, isLoggedIn} = this.props
    const {show} = this.state
    const disableLogin = true
    const cssClass = 'navbar-collapse ' + (show ? 'show' : 'collapse')
    return (
      <nav className='navbar navbar-expand-lg navbar-light bg-light'>
        <button className='navbar-toggler navbar-toggler-left'
          aria-controls='navbar'
          aria-expanded='false' aria-label='Toggle navigation'
          onClick={this.toggleNav}>
          <span className='navbar-toggler-icon'></span>
        </button>
        <div className={cssClass} id='navbar'>
          <ul className='navbar-nav mr-auto'>
            {items.map((item, itemId) => {
              const isSelected = item.url === path
              return <li className={isSelected ? 'nav-item active' : 'nav-item'}
                key={itemId}>
                <Link className='nav-link' to={item.url}>
                  {item.name} {isSelected &&
                <span className='sr-only'>(current)</span>}
                </Link>
              </li>
            })}
          </ul>
          <ul className='navbar-nav'>
            <li className='nav-item'>
              <a className='nav-link' target='_blank'
                href='https://github.com/zanata/zanata-mt'>
                Github
              </a>
            </li>
            <li className='nav-item'>
              <a className='nav-link' target='_blank'
                href='http://zanata.org/zanata-mt/apidocs/'>
                Docs
              </a>
            </li>
            {
              disableLogin ? undefined :
                <li className='nav-item'>
                  { isLoggedIn
                    ? <button className='btn btn-danger' onClick={handleLogout}>Logout</button>
                    : <button className='btn btn-primary' onClick={this.showLoginForm}>Login</button>
                  }
                </li>
            }
          </ul>
        </div>
        <LoginForm show={showLoginForm}
          handleLogin={handleLogin} onClose={this.hideLoginForm}/>
      </nav>
    )
  }
}


function mapStateToProps(state: RootState) {
  return {
    errorData: state.common.errorData,
    loading: state.common.loading,
    isLoggedIn: isLoggedIn(),
    showLoginForm: state.common.showLoginForm
  }
}

function mapDispatchToProps(dispatch: GenericDispatch) {
  return {
    handleLogin: (username: string, password: string) =>
      dispatch(login({auth: {username, password}})),
    handleLogout: () => dispatch(logout()),
    handleSetLoginFormDisplay: (display: boolean) =>
      dispatch(toggleLoginFormDisplay({showLoginForm: display}))
  }
}
