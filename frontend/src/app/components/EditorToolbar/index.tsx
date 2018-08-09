import * as React from 'react';
import { Layout } from 'antd';
const { Header } = Layout;

export class EditorToolbar extends React.Component<{}> {
    public render() {
        return (
            <Layout className="editorToolbar">
                <Header>
                   Test
                </Header>
            </Layout>
        )
    }
}
