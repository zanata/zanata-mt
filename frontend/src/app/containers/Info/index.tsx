import * as React from 'react';
import { connect, GenericDispatch } from 'react-redux';
import { RootState } from '../../reducers';
import { getInfo } from '../../actions/info';
import { Alert } from '../../components'
import {ErrorData} from "../../types/models"

export interface Props {
  loading?: boolean,
  appName?: string,
  version?: string,
  build?: string,
  dev?: boolean
  handleGetInfo: typeof getInfo,
  errorData?: ErrorData
}

@connect(mapStateToProps, mapDispatchToProps)
export class Info extends React.Component<Props, {}> {

  public componentDidMount () {
    this.props.handleGetInfo()
  }

  public render() {
    const { loading, appName, version, build, dev, errorData } = this.props;
    const alert = errorData && <Alert data={errorData} dismissible={true}/>

    const emptyValue = <em className='badge badge-default'>Undefined</em>
    const loadingComp = loading ? <span>loading</span> : undefined
    return (
      <div className='container justify-content-center w-50 mt-3'>
        { alert }
        <h1>Information</h1>
        <div className='p-3 mt-3'>
          <table className='table'>
            <tbody>
              <tr>
                <td>Name</td>
                <td>
                  {loading
                    ? loadingComp
                    : appName
                      ? <span className='badge badge-info'>{appName}</span>
                      : emptyValue
                  }
                </td>
              </tr>
              <tr>
                <td>Version</td>
                <td>
                  {loading
                    ? loadingComp
                    : version
                      ? <span className='badge badge-info'>{version}</span>
                      : emptyValue
                  }
                </td>
              </tr>
              <tr>
                <td>Build</td>
                <td>
                  {loading
                    ? loadingComp
                    :build
                      ? <span className='badge badge-info'>{build}</span>
                      : emptyValue
                  }
                </td>
              </tr>
              {dev && <tr>
                <td>Dev Mode</td>
                <td>
                  {loading
                    ? loadingComp
                    : <span
                      className='badge badge-warning text-white'>Yes</span>
                  }
                </td>
              </tr>}
            </tbody>
          </table>
        </div>
      </div>
    )
  }
}

function mapStateToProps(state: RootState) {
  const {loading, appName, version, buildDate, devMode, errorData} = state.info;
  return {
    loading,
    appName,
    version,
    build: buildDate,
    dev: devMode,
    errorData
  };
}

function mapDispatchToProps(dispatch: GenericDispatch) {
  return {
    handleGetInfo: () => dispatch(getInfo())
  }
}
