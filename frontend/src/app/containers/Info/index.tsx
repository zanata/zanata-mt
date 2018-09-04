import * as React from 'react';
import { connect, GenericDispatch } from 'react-redux';
import { RootState } from '../../reducers';
import { getInfo } from '../../actions/info';
import { Alert } from '../../components'
import {ErrorData} from "../../types/models"
import { Layout, Spin, Icon, Row, Col, notification } from 'antd'
import {MSG_TYPE} from '../../constants/actions'
const { Content } = Layout

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
    const { appName, loading, version, build, dev, errorData } = this.props
    const alert = errorData && <Alert data={errorData} dismissible={true}/>

    const emptyValue = <em>Undefined</em>
    const loadingComp = loading && <Spin indicator={<Icon type="loading" spin />} />
    return (
      <Content style={{ padding: 24 }}>
        { alert }
        <h1>Information</h1>
        <Row justify='start' type='flex'>
          <Col span={4}>Name</Col>
          <Col>
            {loading
              ? loadingComp
              : appName
                ? <span>{appName}</span>
                : emptyValue
            }
          </Col>
        </Row>
        <Row justify='start' type='flex'>
          <Col span={4}>Version</Col>
          <Col>
            {loading
              ? loadingComp
              : version
                ? <span>{version}</span>
                : emptyValue
            }
          </Col>
        </Row>
        <Row justify='start' type='flex'>
          <Col span={4}>Build</Col>
          <Col>
            {loading
              ? loadingComp
              :build
                ? <span>{build}</span>
                : emptyValue
            }
          </Col>
        </Row>
        {dev && <Row justify='start' type='flex'>
          <Col span={4}>Dev Mode</Col>
          <Col>
            {loading
              ? loadingComp
              : <strong>Yes</strong>
            }
          </Col>
        </Row>}
      </Content>
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
