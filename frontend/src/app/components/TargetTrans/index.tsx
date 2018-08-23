import * as React from 'react';
import Row from 'antd/lib/row';
import 'antd/lib/row/style/css';
import Button from 'antd/lib/button';
import 'antd/lib/button/style/css';
import Textarea from 'react-textarea-autosize'
import Popover from 'antd/lib/popover'
import 'antd/lib/popover/style/css'
import {MtAttribution} from '../MtAttribution';
import Icon from 'antd/lib/icon';
import 'antd/lib/icon/style/css';
import Input from 'antd/lib/input';
import 'antd/lib/input/style/css';

export class TargetTransActive extends React.Component<{}> {

    public render() {
        const title = (<span><Icon type='link' />Permalink</span>)
        const content = (
              <span>
                <Input placeholder='https://mt.magpie.org/app/editor/doc/language/translationid'
                       size='small' addonAfter={<Icon type='copy' />} style={{ width: 420 }} />
            </span>
        )
        const targetText = 'Guten'
        return (
                <div className='TransUnit-panel'>
                    <span className='TransUnit-item'>
                       <span className='targetInfo'>
                            <Row>
                                <span>
                                    <Button type='default' className='btnLink' icon='rollback' />
                                    <Popover title={title} trigger='click' arrowPointAtCenter
                                             placement='left' content={content}>
                                      <Button type='default' className='btnLink' icon='link' />
                                    </Popover>
                                </span>
                            </Row>
                        </span>
                          <Textarea
                          className='TransUnit-text TransUnit-translation'
                          rows={1}
                          value={targetText}
                          placeholder='Enter a translation…'
                                />
                        <Row>
                            <Button className='saveTrans' type='primary'>Save</Button>
                        </Row>
                    </span>
                </div>
        )
    }
}


export class TargetTrans extends React.Component<{}> {

    public render() {
        const targetText = 'Guten morgen'
        const title = (<span><Icon type='link' />Permalink</span>)
        const backendId = 'Google'
        const content = (
              <span>
                <Input placeholder='https://mt.magpie.org/app/editor/doc/language/translationid'
                       size='small' addonAfter={<Icon type='copy' />} style={{ width: 420 }} />
            </span>
        )
        return (
                <div className='TransUnit-panel'>
                    <span className='TransUnit-item'>
                          <span className='targetInfo'>
                            <Row>
                                <MtAttribution backendId={backendId}/>
                                <span>
                                    <Popover title={title} trigger='click' arrowPointAtCenter
                                             placement='left' content={content}>
                                      <Button type='default' className='btnLink' icon='link' />
                                    </Popover>
                                </span>
                            </Row>
                        </span>
                          <Textarea
                                  className='TransUnit-text TransUnit-translation'
                                  rows={1}
                                  value={targetText}
                                  placeholder='Enter a translation…'
                          />
                    </span>
                </div>
        )
    }
}

