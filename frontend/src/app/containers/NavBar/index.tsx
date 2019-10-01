import * as React from 'react';
import {connect, GenericDispatch} from 'react-redux'
import {RootState} from "../../reducers/index"
import { Link } from 'react-router-dom'
import { findIndex } from 'lodash'
import LoginForm from "../../containers/LoginForm"
import {Action} from "redux-actions"
import { login, logout, toggleLoginFormDisplay } from '../../actions/common'
import history from '../../history'

import { Layout, Menu, Icon } from 'antd'
const { Sider } = Layout

interface Props {
  isLoggedIn?: boolean,
  showLoginForm?: boolean,
  handleLogout?: () => Action<void>,
  handleLogin?: (username: string, password: string) => Action<void>,
  handleSetLoginFormDisplay?: (display: boolean) => Action<void>
}

export interface State {
  collapsed: boolean
}

const items = [
  {name: 'Home', url: '/', icon: 'home'},
  {name: 'Info', url: '/app/info', icon: 'info-circle'},
  {name: 'Editor', url: '/app/editor', icon: 'file-text'}
]

@connect(mapStateToProps, mapDispatchToProps)
export class NavBar extends React.Component<Props, State> {
  constructor(props?: Props, context?: any) {
    super(props, context);
    this.state = {
      collapsed: false
    }
    this.toggleNav = this.toggleNav.bind(this)
    this.showLoginForm = this.showLoginForm.bind(this)
    this.hideLoginForm = this.hideLoginForm.bind(this)
    this.logout = this.logout.bind(this)
  }

  private toggleNav(collapsed: boolean) {
    this.setState({collapsed})
  }

  private showLoginForm(e: React.MouseEvent<HTMLElement>) {
    this.props.handleSetLoginFormDisplay(true)
  }

  private hideLoginForm() {
    this.props.handleSetLoginFormDisplay(false)
  }

  private getSelectedKey() {
    const path = window.location.pathname
    return findIndex(items, function(item) {
      return item.url === path
    }).toString()
  }

  private logout() {
    this.props.handleLogout()
    history.push('/')
  }

  public render() {
    const {showLoginForm, handleLogin, isLoggedIn} = this.props
      const username = 'username'
    return (
      <Sider collapsible={true}
          collapsed={this.state.collapsed} onCollapse={this.toggleNav}>
          <div className="logo">
              <img src='http://zanata.org/images/mtlogo.png'
                   alt='magpie logo' />
              <span>MagpieMT</span>
          </div>
          <a href="" className="username ellipsis" key="1">{username}</a>
        <Menu theme="dark" mode="inline"
          defaultSelectedKeys={[this.getSelectedKey()]}>
            {items.map((item, itemId) => {
                return <Menu.Item key={itemId}>
                    <Link to={item.url}>
                      <Icon type={item.icon} />
                      <span>{item.name}</span>
                    </Link>
                </Menu.Item>
            })}
            { isLoggedIn && <Menu.Item><Link to={'/app/translate'}>
              <Icon type='file-text' />
              <span>{'Translate'}</span>
            </Link></Menu.Item>
            }
            <Menu.Item>
              <a target='_blank'
                 href='https://github.com/zanata/zanata-mt'>
                <Icon type='github' />
                <span>Github</span>
              </a>
            </Menu.Item>
            <Menu.Item>
              <a target='_blank'
                  href='http://zanata.org/zanata-mt/apidocs/'>
                <Icon type='api' />
                <span>Docs</span>
              </a>
            </Menu.Item>
            <Menu.Item>
              { isLoggedIn
                ? <a onClick={this.logout} className='fc-danger'><Icon type='logout'/>Log out</a>
                : <a onClick={this.showLoginForm}><Icon type='login'/>Log in</a>
              }
            </Menu.Item>
        </Menu>
        {!isLoggedIn &&
          <LoginForm visible={showLoginForm}
            handleLogin={handleLogin} onClose={this.hideLoginForm}/>
        }
      </Sider>
    )
  }
}


function mapStateToProps(state: RootState) {
  return {
    errorData: state.common.errorData,
    loading: state.common.loading,
    isLoggedIn: state.common.auth && state.common.auth.username,
    showLoginForm: state.common.showLoginForm
  }
}

function mapDispatchToProps(dispatch: GenericDispatch) {
  return {
    handleLogin: (username: string, password: string) =>
      dispatch(login(username, password)),
    handleLogout: () => dispatch(logout()),
    handleSetLoginFormDisplay: (display: boolean) =>
      dispatch(toggleLoginFormDisplay({showLoginForm: display}))
  }
}
