import * as React from 'react'
import {connect, GenericDispatch} from 'react-redux'
import { login } from '../../actions/common'
import { Alert } from '../../components'
import {RootState} from '../../reducers'
import {isEmpty} from 'lodash'
import {ErrorData} from "../../types/models"
import { Modal, Button, Form, Icon, Input} from 'antd'
const FormItem = Form.Item;

export interface Props {
  visible: boolean
  loading?: boolean
  errorData?: ErrorData,
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

  private handleChangeUsername(e: React.FormEvent<HTMLInputElement>) {
    this.setState({
      username: e.currentTarget.value,
      validated: !isEmpty(e.currentTarget.value) && !isEmpty(this.state.password)
    })
  }

  private handleChangePassword(e: React.FormEvent<HTMLInputElement>) {
    this.setState({
      password: e.currentTarget.value,
      validated: !isEmpty(e.currentTarget.value) && !isEmpty(this.state.username)
    })
  }

  private handleSubmit(e: React.FormEvent<Form>) {
    e.preventDefault()
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
    const { errorData, loading, visible } = this.props
    const { username, password, validated } = this.state
    const alert = errorData && <Alert data={errorData} dismissible={true}/>

    const footer = (
        <FormItem>
          <Button type='primary'
            loading={loading}
            onClick={this.login} disabled={!validated || loading}>
            Log in
          </Button>
          <Button onClick={this.cancel} type='danger'>
            Cancel
          </Button>
        </FormItem>
    )
    return (
      <Modal title='Login' visible={visible}
        closable={false}
        onCancel={this.cancel}
        footer={footer} >
        <Form className='login-form' onSubmit={this.handleSubmit}>
          {alert}
          <FormItem>
            <Input prefix={<Icon type="user" style={{ color: 'rgba(0,0,0,.25)' }} />}
                placeholder="Username" value={username} onChange={this.handleChangeUsername} />
          </FormItem>
          <FormItem>
            <Input prefix={<Icon type="lock" style={{ color: 'rgba(0,0,0,.25)' }} />}
                type="password" placeholder="Password" value={password} onChange={this.handleChangePassword} />
          </FormItem>
        </Form>
      </Modal>
    )
  }
}

function mapStateToProps(state: RootState) {
  return {
    errorData: state.common.errorData,
    loading: state.common.loading
  }
}

function mapDispatchToProps(dispatch: GenericDispatch) {
  return {
    handleLogin: (username: string, password: string) =>
      dispatch(login({auth: {username, password}}))
  }
}
