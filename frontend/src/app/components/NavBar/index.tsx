import * as React from 'react';
import { connect } from 'react-redux'
import {RootState} from "../../reducers/index"
import { Link } from 'react-router-dom'
import {LoginForm} from "../../containers/LoginForm/index"
import {Action} from "redux-actions"
import { login, logout } from '../../actions/common'

interface Props {
  isLoggedIn?: boolean
  handleLogout?: () => Action<void>,
  handleLogin?: (username: string, password: string) => Action<void>
}

export interface State {
  show: boolean
  showLogin: boolean
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
      show: false,
      showLogin: false
    }
    this.toggleNav = this.toggleNav.bind(this)
    this.toggleLoginForm = this.toggleLoginForm.bind(this)
    this.onCloseLoginForm = this.onCloseLoginForm.bind(this)
  }

  private toggleNav(e) {
    this.setState({
      show: !this.state.show
    })
  }

  private toggleLoginForm(e) {
    this.setState({
      showLogin: true
    })
  }

  private onCloseLoginForm() {
    this.setState({
      showLogin: false
    })
  }

  public render() {
    const path = window.location.pathname
    const {handleLogin, handleLogout, isLoggedIn} = this.props
    const {show, showLogin} = this.state
    const cssClass = 'navbar-collapse ' + (show ? 'show' : 'collapse')
    return (
      <nav className='navbar navbar-toggleable-md navbar-light bg-faded mr-auto'>
        <button className='navbar-toggler navbar-toggler-left'
          aria-controls='navbar'
          aria-expanded='false' aria-label='Toggle navigation'
          onClick={this.toggleNav}>
          <span className='navbar-toggler-icon'></span>
        </button>
        <span className='navbar-text'>&nbsp;</span>
        <div className={cssClass} id='navbar'>
          <ul className='navbar-nav mr-auto mt-2 mt-md-0'>
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
          { isLoggedIn
            ? <button className='btn btn-warning' onClick={handleLogout}>Logout</button>
            : <button className='btn btn-primary' onClick={this.toggleLoginForm}>Login</button>
          }
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
          </ul>
        </div>
        <LoginForm show={showLogin} handleLogin={handleLogin} onClose={this.onCloseLoginForm}/>
      </nav>
    )
  }
}


function mapStateToProps(state: RootState) {
  return {
    errorData: state.common.errorData,
    loading: state.common.loading,
    isLoggedIn: false
  }
}

function mapDispatchToProps(dispatch) {
  return {
    handleLogin: (username, password) => dispatch(login),
    handleLogout: () => dispatch(logout)
  }
}
