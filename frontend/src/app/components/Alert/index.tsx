import * as React from 'react';
import { MSG_TYPE } from '../../constants/actions';

export interface Props {
  data?,
  dismissible: boolean
}

export class Alert extends React.Component<Props, {}> {
  public render() {
    const { dismissible, data } = this.props;
    const className = this.getClassName(dismissible, data)
    const button = dismissible &&
      (<button type="button" className="close" data-dismiss="alert" aria-label="Close">
      <span aria-hidden="true">&times;</span>
    </button>)

    return (
      <div className={className} role='alert' title={data.summary}>
        {button}
        <div className='small'>{data.timestamp}</div>
        <div><strong>{data.message}</strong></div>
        <div>{data.stack}</div>
      </div>
    );
  }

  private getClassName(dismissible, data) {
    let className = 'fade show alert' + (dismissible ? ' alert-dismissible' : '')
    if (data.type === MSG_TYPE.ERROR) {
      className += ' alert-danger'
    } else if (data.type === MSG_TYPE.WARNING) {
      className += ' alert-warning'
    } else {
      className += ' alert-info'
    }
    return className
  }
}
