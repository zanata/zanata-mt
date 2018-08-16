import * as React from 'react';
import Layout from 'antd/lib/layout';
import 'antd/lib/layout/style/css';
import Row from 'antd/lib/row';
import 'antd/lib/row/style/css';
import Card from 'antd/lib/card';
import 'antd/lib/card/style/css';
import Button from 'antd/lib/button';
import 'antd/lib/button/style/css';
import { TransInfo } from '../TransInfo';
import { EditableText } from '../EditableText'

export class TargetTrans extends React.Component<{}> {
    public render() {
        const targetText = 'Guten morgen'
        return (
                <Layout className="targetTrans">
                        <TransInfo />
                        <EditableText
                           maxLength={255}
                           editable={true}
                           editing={true}
                           placeholder='Add a description…'
                           emptyReadOnlyText='No description'>
                            {targetText}
                        </EditableText>
                        <Row>
                            <Button className='saveTrans' type='primary'>Save</Button>
                        </Row>
                </Layout>
        )
    }
}
