import * as React from 'react';
import { connect } from 'react-redux';
import { login } from '../../actions/common';
import { Alert } from '../../components/Alert'
import {RootState} from '../../reducers';

export interface Props {
  errorData?,
  handleLogin
}

@connect(mapStateToProps, mapDispatchToProps)
export class LoginForm extends React.Component<Props, {}> {

  public render() {
    const { errorData, handleLogin } = this.props;
    const alert = errorData && <Alert data={errorData} dismissible={true}/>
    return (
      <div className='container justify-content-center w-50 mt-3 text-center'>
        <h1>Login</h1>
        <form id='login-form' className='text-left w-50 mx-auto'>
          { alert }
          <div className='main-login-form'>
            <div className='login-group'>
              <div className='form-group'>
                <label htmlFor='lg_username'
                  className='sr-only'>Username</label>
                <input type='text' className='form-control' id='lg_username'
                  name='lg_username' placeholder='username'/>
              </div>
              <div className='form-group'>
                <label htmlFor='lg_password'
                  className='sr-only'>Password</label>
                <input type='password' className='form-control' id='lg_password'
                  name='lg_password' placeholder='password'/>
              </div>
            </div>
            <button type='submit' className='btn btn-primary' onClick={handleLogin}>Login</button>
          </div>
        </form>
      </div>
    )
  }
}

function mapStateToProps(state: RootState) {
  return {
    errorData: state.errorData
  };
}

function mapDispatchToProps(dispatch) {
  return {
    handleLogin: (username, pass) => dispatch(login(username, pass))
  };
}
