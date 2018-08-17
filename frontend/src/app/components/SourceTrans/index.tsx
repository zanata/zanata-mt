import * as React from 'react';
import Layout from 'antd/lib/layout';
import 'antd/lib/layout/style/css';

export class SourceTrans extends React.Component<{}> {
    public render() {
        const sourceText = 'Good morning'
        return (
                <Layout className="sourceTrans">
                    <span className="transWrapper">
                        <p className='sourceText'>{sourceText}</p>
                    </span>
                </Layout>
        )
    }
}
