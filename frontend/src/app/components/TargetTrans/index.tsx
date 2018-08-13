import * as React from 'react';
import Layout from 'antd/lib/layout';
import 'antd/lib/layout/style/css';
import Card from 'antd/lib/card';
import 'antd/lib/card/style/css';
import { TransInfo } from '../TransInfo';
import { EditableText } from '../EditableText'

export class TargetTrans extends React.Component<{}> {
    public render() {
        const targetText = 'Guten morgen'
        return (
                <Layout className="targetTrans">
                    <Card hoverable >
                        <TransInfo />
                        <EditableText
                           maxLength={255}
                           editable={true}
                           editing={true}
                           placeholder='Add a description…'
                           emptyReadOnlyText='No description'>
                            {targetText}
                        </EditableText>
                    </Card>
                </Layout>
        )
    }
}
