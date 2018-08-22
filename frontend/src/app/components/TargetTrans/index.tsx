import * as React from 'react';
import Row from 'antd/lib/row';
import 'antd/lib/row/style/css';
import Button from 'antd/lib/button';
import 'antd/lib/button/style/css';
import { TransInfo } from '../TransInfo';
import Textarea from 'react-textarea-autosize'


export class TargetTrans extends React.Component<{}> {

    public render() {
        const targetText = 'Guten morgen, Guten morgenn morgenuten morgen,' +
                ' Guten morgen Guten morgenn morgenuten  Guten morgenn morgenuten  Guten morgenn morgenuten  Guten morgenn morgenuten  Guten morgenn morgenuten  Guten morgenn morgenuten  Guten morgenn morgenuten  Guten morgenn morgenuten  Guten morgenn morgenuten  Guten morgenn morgenuten '
        return (
                <div className="TransUnit-panel">
                    <span className="TransUnit-item">
                        <TransInfo />
                          <Textarea
                          className='TransUnit-text TransUnit-translation'
                          rows={1}
                          value={targetText}
                          placeholder="Enter a translation…"
                                />
                        <Row>
                            <Button className='saveTrans' type='primary'>Save</Button>
                        </Row>
                    </span>
                </div>
        )
    }
}
