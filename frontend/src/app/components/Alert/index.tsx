import * as React from 'react'
import { MSG_TYPE } from '../../constants/actions'
import {ErrorData} from '../../types/models'
import { Alert as AntdAlert } from 'antd'

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

  const message = data.summary ? <h3>{data.timestamp} {data.summary}</h3> : <h3>{data.timestamp}</h3>
  const description = (
      <div>
        <h4>{data.message}</h4>
        <pre><code>{data.stack}</code></pre>
      </div>
  )
  return (
      <AntdAlert type={alertType}
          message={message}
          description={description}
          closable={dismissible} />
  )
}