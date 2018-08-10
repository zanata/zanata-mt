import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'
import React from 'react'

export interface Props {
    backendId?: string
}

export class MtAttribution extends React.Component<Props, {}> {
    render () {
        const backendId = 'Google'
        const title = this.props.backendId === backendId ?
                <a href='https://translate.google.com/'>
                    <img src='http://zanata.org/images/translated-by-google-white-short.png'
                         alt='Translated by Google' /></a>
                : 'Translated by ' + this.props.backendId
        return (
                <span className='Button--MT'>
        <Tooltip placement='right' title={title}>
          <Button className='Button--snug u-roundish Button--neutral'>
           MT
          </Button>
        </Tooltip>
      </span>
        )
    }
}
