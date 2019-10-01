import * as React from 'react';
import Layout from 'antd/lib/layout';
import 'antd/lib/layout/style/css';
import Row from 'antd/lib/row';
import 'antd/lib/row/style/css';
import Col from 'antd/lib/col';
import 'antd/lib/col/style/css';
 import {EditorToolbar, SourceTrans, TargetTrans, TargetTransActive} from '../../components';
const { Content } = Layout;

export class Editor extends React.Component<{}> {
    public render() {
        const rowTrans = (
                <span className='TransUnit'>
                <Col span={12}>
                    <SourceTrans />
                </Col>
                <Col span={12} className='transBorder'>
                    <TargetTrans />
                </Col>
            </span>
        )
        const rowTransFocused = (
            <span className='TransUnit is-focused'>
                <Col span={12}>
                    <SourceTrans />
                </Col>
                <Col span={12} className='transBorder'>
                    <TargetTransActive />
                </Col>
            </span>
        )
        return (
            <Layout className="editor">
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
