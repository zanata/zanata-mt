import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Popover from 'antd/lib/popover'
import 'antd/lib/popover/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'
import Icon from 'antd/lib/icon'
import 'antd/lib/icon/style/css'
import React from 'react'
import {MtAttribution} from '../MtAttribution'

export class TransInfo extends React.Component<{}> {
    render () {
        const title = (<span><Icon type='link' />Permalink</span>)
        const backendId = 'Google'
        const content = (
            <span>
                <Input placeholder='https://mt.magpie.org/app/editor/doc/language/translationid'
                       size='small' addonAfter={<Icon type='copy' />} style={{ width: 420 }} />
            </span>
        )
        return (
        <span className='targetInfo'>
            <Row>
                <MtAttribution backendId={backendId}/>
                <Popover title={title} trigger='click' arrowPointAtCenter
                  placement='left' content={content}>
                  <Button type='default' className='btnLink' icon='link' />
                </Popover>
            </Row>
        </span>
        )
    }
}
