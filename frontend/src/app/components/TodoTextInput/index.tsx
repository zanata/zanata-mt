import * as React from 'react';
import classNames from 'classnames';
import * as style from './style.css';

export interface Props {
  text?: string;
  placeholder?: string;
  newTodo?: boolean;
  editing?: boolean;
  onSave: (text: string) => void;
}

export interface State {
  text: string;
}

export class TodoTextInput extends React.Component<Props, State> {

  // tslint:disable-next-line:no-any
  constructor(props?: Props, context?: any) {
    super(props, context);
    this.state = {
      text: this.props.text || ''
    };
    this.handleBlur = this.handleBlur.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }

  public render() {
    const classes = classNames({
      [style.edit]: this.props.editing,
      [style.new]: this.props.newTodo
    }, style.normal);

    return (
      <input className={classes}
        type="text"
        autoFocus
        placeholder={this.props.placeholder}
        value={this.state.text}
        onBlur={this.handleBlur}
        onChange={this.handleChange}
        onKeyDown={this.handleSubmit} />
    );
  }

  private handleSubmit(e) {
    const text = e.target.value.trim();
    if (e.which === 13) {
      this.props.onSave(text);
      if (this.props.newTodo) {
        this.setState({ text: '' });
      }
    }
  }

  private handleChange(e) {
    this.setState({ text: e.target.value });
  }

  private handleBlur(e) {
    const text = e.target.value.trim();
    if (!this.props.newTodo) {
      this.props.onSave(text);
    }
  }

}
