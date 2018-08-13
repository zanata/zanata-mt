import * as React from 'react';
import Layout from 'antd/lib/layout';
import 'antd/lib/layout/style/css';
import Card from 'antd/lib/card';
import 'antd/lib/card/style/css';
import { TransInfo } from '../TransInfo';

export class TargetTrans extends React.Component<{}> {
    public render() {
        const targetText = 'Guten morgen'
        return (
                <Layout className="targetTrans">
                    <Card hoverable>
                        <TransInfo />
                        <p className='targetText'>{targetText}</p>
                    </Card>
                </Layout>
        )
    }
}
