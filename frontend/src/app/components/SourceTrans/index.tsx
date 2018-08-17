import * as React from 'react';
import Layout from 'antd/lib/layout';
import 'antd/lib/layout/style/css';

export class SourceTrans extends React.Component<{}> {
    public render() {
        const sourceText = 'Guten morgen, Guten morgen Guten morgenGuten morgenGuten morgenGuten morgenGuten morgen' +
                'Guten morgenGuten morgenGuten morgenGuten morgenGuten' +
                ' morgenGuten morgenGuten morgen. Guten morgen!'
        return (
                <Layout className="sourceTrans">
                    <span className="transWrapper">
                        <p className='sourceText'>{sourceText}</p>
                    </span>
                </Layout>
        )
    }
}
