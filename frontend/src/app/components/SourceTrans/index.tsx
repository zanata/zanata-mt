import * as React from 'react';
import Layout from 'antd/lib/layout';
import 'antd/lib/layout/style/css';
import Card from 'antd/lib/card';
import 'antd/lib/card/style/css';

export class SourceTrans extends React.Component<{}> {
    public render() {
        const sourceText = 'Good morning'
        return (
                <Layout className="sourceTrans">
                        <p className='sourceText'>{sourceText}</p>
                </Layout>
        )
    }
}
