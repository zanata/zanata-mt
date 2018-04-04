import * as React from 'react';
import { Layout, Alert } from 'antd'
const { Content } = Layout

interface NoMatchProps {
    // empty
}

export const NoMatch: React.StatelessComponent<NoMatchProps> = ({

}) => {
  const description = <h1>404</h1>
  const message =
    <h2>Sorry, we cannot find the page you are looking for.</h2>

  return (
    <Content style={{ padding: 24 }}>
      <Alert message={message} type='error' showIcon
        description={description}/>
    </Content>
  )
}
