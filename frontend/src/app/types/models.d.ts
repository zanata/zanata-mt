/** TodoMVC model definitions **/

declare interface TodoItemData {
  id?: TodoItemId;
  text?: string;
  completed?: boolean;
}

declare type TodoItemId = number;

declare type TodoFilterType = 'SHOW_ALL' | 'SHOW_ACTIVE' | 'SHOW_COMPLETED';

declare type TodoStoreState = TodoItemData[];

declare interface ErrorData {
  summary?: string,
  message?: string,
  timestamp: string,
  stack: string,
  type: string
}

declare type ErrorData = ErrorData;

declare interface InfoData {
  loading?: boolean
  appName?: string,
  version?: string,
  buildDate?: string,
  devMode?: boolean,
  errorData?: ErrorData
  // generic redux-middleware error fields
  message?: string,
  name?: string,
  stack?: string
}

declare type InfoState = InfoData;
