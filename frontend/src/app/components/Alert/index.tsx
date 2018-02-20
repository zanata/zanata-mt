import * as React from 'react';
import { MSG_TYPE } from '../../constants/actions';

export interface Props {
  data,
  dismissible: boolean
}

export class Alert extends React.Component<Props, {}> {
  public render() {
    const { dismissible, data } = this.props;
    const className = this.getAlertClassName(dismissible, data)
    const button = dismissible &&
      (<button type="button" className="close" data-dismiss="alert" aria-label="Close">
      <span aria-hidden="true">&times;</span>
    </button>)

    const buttonClass = this.getButtonClassName(data.type)
    return (
      <div className={className} role='alert'>
        {button}
        { data.summary && <h4>{data.summary}</h4> }
        <p>{data.timestamp} {data.message}</p>

        {data.stack &&
        <button className={buttonClass} type='button' data-toggle='collapse'
          data-target='#stack' aria-expanded='false' aria-controls="stack">
          Stack trace
        </button>
        }
        {data.stack &&
        <div className='collapse' id='stack'>
          <div className='card card-block'>
            <em>{data.stack}</em>
          </div>
        </div>
        }
      </div>
    )
  }

  private getButtonClassName(type) {
    if (type === MSG_TYPE.ERROR) {
      return 'btn btn-danger'
    } else if (type === MSG_TYPE.WARNING) {
      return 'btn btn-warning'
    } else {
      return 'btn btn-info'
    }
  }

  private getAlertClassName(dismissible, data) {
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
