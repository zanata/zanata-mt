import * as React from 'react'
import {connect, GenericDispatch} from 'react-redux'
import { login } from '../../actions/common'
import { Alert } from '../../components'
import {RootState} from '../../reducers'
import {isEmpty} from 'lodash'
import {ErrorData} from "../../types/models"
import { Modal, Button, Form, Icon, Input} from 'antd'
import {FormComponentProps} from 'antd/lib/form'
import {KeyboardEvent} from 'react'
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
  password: string
}

class LoginForm extends React.Component<Props & FormComponentProps, State> {
  constructor(props?: Props & FormComponentProps) {
    super(props);
    this.state = {
      username: '',
      password: ''
    }
    this.handleChangeUsername = this.handleChangeUsername.bind(this)
    this.handleChangePassword = this.handleChangePassword.bind(this)
    this.login = this.login.bind(this)
    this.cancel = this.cancel.bind(this)
    this.handleKeyPress = this.handleKeyPress.bind(this)
  }

  private handleChangeUsername(e: React.FormEvent<HTMLInputElement>) {
    const value = e.currentTarget.value
    this.props.form.setFieldsValue({
      username: value
    })
    this.setState({
      username: value
    })
  }

  private handleChangePassword(e: React.FormEvent<HTMLInputElement>) {
    const value = e.currentTarget.value
    this.props.form.setFieldsValue({
      password: value
    })
    this.setState({
      password: value
    })
  }

  private handleSubmit(e: React.FormEvent<Form>) {
    e.preventDefault()
  }

  private login() {
    this.props.handleLogin(this.state.username, this.state.password)
  }

  private handleKeyPress(event: KeyboardEvent) {
    if(event.key == 'Enter'){
      if (!isEmpty(this.state.username) && !isEmpty(this.state.password)) {
        this.login()
      }
    }
  }

  private cancel() {
    this.setState({
      username: '',
      password: ''
    })
    this.props.onClose()
  }

  public render() {
    const { errorData, loading, visible, form } = this.props
    const { getFieldDecorator, isFieldsTouched }  = form
    const alert = errorData && <Alert data={errorData} dismissible={true}/>
    const { username, password } = this.state
    const validated = !isEmpty(username) && !isEmpty(password)

    const footer = (
        <FormItem>
          <Button type='primary'
            loading={loading}
            onClick={this.login}
            disabled={!validated || loading || !isFieldsTouched()}>
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
        <Form onSubmit={this.handleSubmit} onKeyPress={this.handleKeyPress}>
          {alert}
          <FormItem>
            {getFieldDecorator('username', {
              rules: [{ required: true, message: 'Please input your username!' }],
            })(
              <Input placeholder='Username' onChange={this.handleChangeUsername} autoFocus />
            )}
          </FormItem>
          <FormItem>
            {getFieldDecorator('password', {
              rules: [{ required: true, message: 'Please input your Password!' }]
            })(
              <Input type="password" placeholder="Password" onChange={this.handleChangePassword} />
            )}
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
      dispatch(login(username, password))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)
(Form.create({})(LoginForm) as any)