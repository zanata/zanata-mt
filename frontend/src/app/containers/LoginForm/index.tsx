import * as React from 'react'
import { connect } from 'react-redux'
import { login } from '../../actions/common'
import { Alert } from '../../components/Alert'
import {RootState} from '../../reducers';
import {isEmpty} from 'lodash';
import {Modal, ModalBody, ModalFooter, size} from "../../components/Modal"

export interface Props {
  show: boolean
  loading?: boolean
  errorData?,
  handleLogin: (username: string, pass: string) => void
  onClose?: () => void
}

export interface State {
  username: string,
  password: string,
  validated: boolean
}

@connect(mapStateToProps, mapDispatchToProps)
export class LoginForm extends React.Component<Props, State> {
  constructor(props?: Props, context?: any) {
    super(props, context);
    this.state = {
      username: '',
      password: '',
      validated: false
    }
    this.handleChangeUsername = this.handleChangeUsername.bind(this)
    this.handleChangePassword = this.handleChangePassword.bind(this)
    this.login = this.login.bind(this)
    this.cancel = this.cancel.bind(this)
  }

  private handleChangeUsername(e) {
    this.setState({
      username: e.target.value,
      validated: !isEmpty(e.target.value) && !isEmpty(this.state.password)
    })
  }

  private handleChangePassword(e) {
    this.setState({
      password: e.target.value,
      validated: !isEmpty(e.target.value) && !isEmpty(this.state.username)
    })
  }

  private login() {
    this.props.handleLogin(this.state.username, this.state.password)
  }

  private cancel() {
    this.setState({
      username: '',
      password: '',
      validated: false
    })
    this.props.onClose() && this.props.onClose()
  }

  public render() {
    const { errorData, loading, show } = this.props
    const { username, password, validated } = this.state
    const alert = errorData && <Alert data={errorData} dismissible={true}/>

    const body = (
      <ModalBody>
        <div className='main-login-form'>
          {alert}
          <div className='login-group'>
            <div className='form-group'>
              <label htmlFor='lg_username'
                className='sr-only'>Username</label>
              <input type='text' className='form-control' value={username}
                onChange={this.handleChangeUsername}
                name='lg_username' placeholder='username'/>
            </div>
            <div className='form-group'>
              <label htmlFor='lg_password'
                className='sr-only'>Password</label>
              <input type='password' className='form-control' value={password}
                onChange={this.handleChangePassword}
                name='lg_password' placeholder='password'/>
            </div>
          </div>
        </div>
      </ModalBody>
    )

    const footer = (
      <ModalFooter>
        <button className='btn btn-primary'
          onClick={this.login} disabled={!validated || loading}>
          Login
        </button>
        <button className='btn btn-secondary' onClick={this.cancel}>
          Cancel
        </button>
      </ModalFooter>
    )
    return (
      <Modal title='Login' show={show}
        onClose={this.cancel} body={body}
        footer={footer} size={size.small} />
    )
  }
}

function mapStateToProps(state: RootState) {
  return {
    errorData: state.common.errorData,
    loading: state.common.loading
  }
}

function mapDispatchToProps(dispatch) {
  return {
    handleLogin: (username, password) => dispatch(login({username, password}))
  }
}
