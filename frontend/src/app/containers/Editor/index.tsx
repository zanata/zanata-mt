import * as React from 'react';
import Layout from 'antd/lib/layout';
import 'antd/lib/layout/style/css';
import Menu from 'antd/lib/menu';
import 'antd/lib/menu/style/css';
import Row from 'antd/lib/row';
import 'antd/lib/row/style/css';
import Col from 'antd/lib/col';
import 'antd/lib/col/style/css';
import {EditorToolbar, SourceTrans, TargetTrans, TargetTransActive} from '../../components';
const { Header, Content } = Layout;

export interface Props {
    selected?: boolean
}

export class Editor extends React.Component<Props, {}> {
    constructor(props?: Props, context?: any) {
        super(props, context);
        this.state = {
            selected: true
        }
    }
    public render() {
        const rowTrans = (
                <span className='TransUnit'>
                <Col span={12}>
                    <SourceTrans />
                </Col>
                <Col span={12}>
                    <TargetTrans />
                </Col>
            </span>
        )
        const rowTransFocused = (
            <span className='TransUnit is-focused'>
                <Col span={12}>
                    <SourceTrans />
                </Col>
                <Col span={12}>
                    <TargetTransActive />
                </Col>
            </span>
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
                    <Row className='rowHeading'>
                    <Col span={12}>
                        <span className='columnHeading'>SOURCE</span>
                    </Col>
                    <Col span={12}>
                        <span className='columnHeading'>TARGET</span>
                    </Col>
                    </Row>
                    {rowTrans}
                    {rowTransFocused}
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
