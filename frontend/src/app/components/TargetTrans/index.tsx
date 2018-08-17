import * as React from 'react';
import Layout from 'antd/lib/layout';
import 'antd/lib/layout/style/css';
import Row from 'antd/lib/row';
import 'antd/lib/row/style/css';
import Button from 'antd/lib/button';
import 'antd/lib/button/style/css';
import { TransInfo } from '../TransInfo';
import { EditableText } from '../EditableText'

export class TargetTrans extends React.Component<{}> {
    public render() {
        const targetText = 'Guten morgen, Guten morgen Guten morgenGuten morgenGuten morgenGuten morgenGuten morgen' +
                'Guten morgenGuten morgenGuten morgenGuten morgenGuten' +
                ' morgenGuten morgenGuten morgen. Guten morgen!'
        return (
                <Layout className="targetTrans">
                    <span className="transWrapper">
                        <TransInfo />
                        <EditableText
                           editable={true}
                           editing={true}
                           placeholder='Add a description…'
                           emptyReadOnlyText='No description'>
                            {targetText}
                        </EditableText>
                        <Row>
                            <Button className='saveTrans' type='primary'>Save</Button>
                        </Row>
                    </span>
                </Layout>
        )
    }
}
