import * as React from 'react';
import { Layout, Menu } from 'antd';
import { EditorToolbar } from '../../components';
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
                    <div style={{ background: '#fff', padding: 24, minHeight: 280 }}>Content</div>
                </Content>
            </Layout>
        )
    }
}
