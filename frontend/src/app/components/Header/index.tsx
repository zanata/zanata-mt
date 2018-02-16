import * as React from 'react';
import { TodoTextInput } from '../TodoTextInput';
import {Action} from 'redux-actions';

export interface Props {
  addTodo: (todo: TodoItemData) => Action<TodoItemData>;
}

export class Header extends React.Component<Props, {}> {

    // tslint:disable-next-line:no-any
    constructor(props?: Props, context?: any) {
    super(props, context);
    this.handleSave = this.handleSave.bind(this);
  }

  public render() {
    return (
      <header>
        <h1>Todos</h1>
        <TodoTextInput
          newTodo
          onSave={this.handleSave}
          placeholder="What needs to be done?" />
      </header>
    );
  }

  private handleSave(text: string) {
    if (text.length) {
      this.props.addTodo({ text });
    }
  }

}
