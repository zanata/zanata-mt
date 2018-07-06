import * as React from 'react'
import {MSG_TYPE} from '../../constants/actions'
import {ErrorData} from '../../types/models'
import {Alert as AntdAlert} from 'antd'

export interface Props {
  data: ErrorData,
  dismissible: boolean
}

export const Alert: React.StatelessComponent<Props> = ({
                                                         dismissible, data
                                                       }) => {
  function getAlertType(dismissible: boolean, data: ErrorData) {
    if (data.type === MSG_TYPE.ERROR) {
      return 'error'
    } else if (data.type === MSG_TYPE.WARNING) {
      return 'warning'
    } else {
      return 'info'
    }
  }

  const alertType = getAlertType(dismissible, data)
  const timestamp = <span>{data.timestamp}</span>
  const message = <div>{timestamp} {data.summary && data.summary}</div>
  const description = (data.message || data.stack) && (
    <p>
      {data.message && <div>{data.message}</div>}
      {data.stack && <pre><code>{data.stack}</code></pre>}
    </p>
  )
  return (
    <AntdAlert type={alertType}
      showIcon={true}
      message={message}
      description={description}
      closable={dismissible}/>
  )
}