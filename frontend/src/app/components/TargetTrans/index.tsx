import * as React from 'react';
import Layout from 'antd/lib/layout';
import 'antd/lib/layout/style/css';
import Card from 'antd/lib/card';
import 'antd/lib/card/style/css';
import { MtAttribution } from '../MtAttribution';

export class TargetTrans extends React.Component<{}> {
    public render() {
        const targetText = 'Guten morgen'
        return (
                <Layout className="targetTrans">
                    <Card hoverable>
                        <MtAttribution />
                        <p>{targetText}</p>
                    </Card>
                </Layout>
        )
    }
}
