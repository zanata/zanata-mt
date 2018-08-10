import * as React from 'react';
import Layout from 'antd/lib/layout';
import 'antd/lib/layout/style/css';
import Menu from 'antd/lib/menu';
import 'antd/lib/menu/style/css';
import Col from 'antd/lib/col';
import 'antd/lib/col/style/css';
import {EditorToolbar, SourceTrans, TargetTrans} from '../../components';
const { Header, Content } = Layout;

export class Editor extends React.Component<{}> {
    public render() {
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
                    <Col span={12}>
                        <h4>SOURCE</h4>
                        <SourceTrans />
                        <SourceTrans />
                        <SourceTrans />
                        <SourceTrans />
                        <SourceTrans />
                        <SourceTrans />
                        <SourceTrans />
                        <SourceTrans />
                        <SourceTrans />
                        <SourceTrans />
                    </Col>
                    <Col span={12}>
                        <h4>TARGET</h4>
                        <TargetTrans />
                        <TargetTrans />
                        <TargetTrans />
                        <TargetTrans />
                        <TargetTrans />
                        <TargetTrans />
                        <TargetTrans />
                        <TargetTrans />
                        <TargetTrans />
                        <TargetTrans />
                    </Col>
                </Content>
            </Layout>
        )
    }
}
