import * as React from 'react';
import Layout from 'antd/lib/layout';
import 'antd/lib/layout/style/css';
import Menu from 'antd/lib/menu';
import 'antd/lib/menu/style/css';
import Row from 'antd/lib/row';
import 'antd/lib/row/style/css';
import Col from 'antd/lib/col';
import 'antd/lib/col/style/css';
import {EditorToolbar, SourceTrans, TargetTrans} from '../../components';
const { Header, Content } = Layout;

export class Editor extends React.Component<{}> {
    public render() {
        const rowTrans = (
            <Row>
                <Col span={12}>
                    <SourceTrans />
                </Col>
                <Col span={12}>
                    <TargetTrans />
                </Col>
            </Row>
        )
        return (
            <Layout className="editor">
                <Header>
                    <div className="logo">
                        MagpieMT
                    </div>
                    <Menu theme="dark"
                            mode="horizontal"
                            style={{ lineHeight: '64px' }}
                    >
                        <Menu.Item key="1">username</Menu.Item>
                    </Menu>
                </Header>
                <Content style={{ padding: '0 50px' }}>
                    <EditorToolbar />
                    <Row>
                    <Col span={12}>
                        <span className='columnHeading'>SOURCE</span>
                    </Col>
                    <Col span={12}>
                        <span className='columnHeading'>TARGET</span>
                    </Col>
                    </Row>
                    {rowTrans}
                    {rowTrans}
                    {rowTrans}
                    {rowTrans}
                    {rowTrans}
                    {rowTrans}
                    {rowTrans}
                    {rowTrans}
                    {rowTrans}
                </Content>
            </Layout>
        )
    }
}
