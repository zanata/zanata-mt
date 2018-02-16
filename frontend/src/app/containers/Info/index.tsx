import * as React from 'react';
import { connect } from 'react-redux';
import { RootState } from '../../reducers';
import { getInfo } from '../../actions/info';
import { Alert } from '../../components/Alert'

export interface Props {
  appName?: string,
  version?: string,
  build?: string,
  dev?: boolean
  handleGetInfo: typeof getInfo,
  errorData?
}

@connect(mapStateToProps, mapDispatchToProps)
export class Info extends React.Component<Props, {}> {

  public componentDidMount () {
    this.props.handleGetInfo()
  }

  public render() {
    const { appName, version, build, dev, errorData } = this.props;
    const alert = errorData && <Alert data={errorData} dismissible={true}/>

    return (
      <div className='container justify-content-center w-50'>
        <h1>Information</h1>
        <div className='p-3 mt-3'>
          {errorData
            ? alert
            :
            <table className='table'>
              <tbody>
              <tr>
                <td>Name</td>
                <td>
                  <span className='badge badge-info'>{appName}</span>
                </td>
              </tr>
              <tr>
                <td>Version</td>
                <td>
                  <span className='badge badge-info'>{version}</span>
                </td>
              </tr>
              <tr>
                <td>Build</td>
                <td>
                  <span className='badge badge-info'>{build}</span>
                </td>
              </tr>
              {dev && <tr>
                <td>Dev Mode</td>
                <td>
                  <span className='badge badge-warning text-white'>Yes</span>
                </td>
              </tr>}
              </tbody>
            </table>
          }
        </div>
      </div>
    )
  }
}

function mapStateToProps(state: RootState) {
  const { appName, version, buildDate, devMode, errorData } = state.info;
  return {
    appName,
    version,
    build: buildDate,
    dev: devMode,
    errorData
  };
}

function mapDispatchToProps(dispatch) {
  return {
    handleGetInfo: () => dispatch(getInfo())
  };
}
