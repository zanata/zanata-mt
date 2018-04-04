import * as React from 'react';
import { connect, GenericDispatch } from 'react-redux';
import { RootState } from '../../reducers';
import { getInfo } from '../../actions/info';
import { Alert } from '../../components'
import {ErrorData} from "../../types/models"
import { Layout, Spin, Icon, Row, Col } from 'antd'
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
        <Row>
          <Col span={8}>
            <Row gutter={8} justify='center' align='middle' type='flex'>
              <Col span={6}>Name</Col>
              <Col span={18}>
                {loading
                  ? loadingComp
                  : appName
                    ? <span>{appName}</span>
                    : emptyValue
                }
              </Col>
            </Row>
            <Row gutter={8} justify='center' align='middle' type='flex'>
              <Col span={6}>Version</Col>
              <Col span={18}>
                {loading
                  ? loadingComp
                  : version
                    ? <span>{version}</span>
                    : emptyValue
                }
              </Col>
            </Row>
            <Row gutter={8} justify='center' align='middle' type='flex'>
              <Col span={6}>Build</Col>
              <Col span={18}>
                {loading
                  ? loadingComp
                  :build
                    ? <span>{build}</span>
                    : emptyValue
                }
              </Col>
            </Row>
            {dev && <Row gutter={8} justify='center' align='middle' type='flex'>
              <Col span={6}>Dev Mode</Col>
              <Col span={18}>
                {loading
                  ? loadingComp
                  : <strong>Yes</strong>
                }
              </Col>
            </Row>}
          </Col>
        </Row>
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
