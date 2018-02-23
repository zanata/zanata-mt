import * as React from 'react';
import { MSG_TYPE } from '../../constants/actions';

export interface Props {
  data,
  dismissible: boolean
}

export interface State {
  show: boolean,
  showStackTrace: boolean
}

export class Alert extends React.Component<Props, State> {
  constructor(props?: Props, context?: any) {
    super(props, context);
    this.state = {
      show: true,
      showStackTrace: false
    }
    this.dismissAlert = this.dismissAlert.bind(this)
    this.toggleStackTrace = this.toggleStackTrace.bind(this)
  }

  private dismissAlert(e) {
    this.setState({
      show: false
    })
  }

  private toggleStackTrace(e) {
    this.setState({
      showStackTrace: !this.state.showStackTrace
    })
  }

  public render() {
    const { dismissible, data } = this.props;
    const { showStackTrace, show } = this.state
    const className = this.getAlertClassName(dismissible, data, show)
    const buttonClass = this.getButtonClassName(data.type)
    return (
      <div className={className}>
        { show
          ? <div>
            {dismissible &&
            (<button type="button" className='close' aria-label='Close' onClick={this.dismissAlert}>
              <span aria-hidden="true">&times;</span>
            </button>)}
            {data.summary && <h4>{data.summary}</h4>}
            <p>{data.timestamp} {data.message}</p>

            {data.stack &&
            <button className={buttonClass} onClick={this.toggleStackTrace}
              aria-controls="stack">
              Stack trace
            </button>
            }
            {data.stack &&
            <div className={showStackTrace ? '' : 'collapse'} id='stack'>
              <div className='card card-block'>
                <em>{data.stack}</em>
              </div>
            </div>
            }
          </div>
          : undefined
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

  private getAlertClassName(dismissible, data, show) {
    let className = 'alert' + (dismissible ? ' alert-dismissible' : '') + (show ? '' : ' hide')
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
