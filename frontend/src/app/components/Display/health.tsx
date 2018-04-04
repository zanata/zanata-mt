import * as React from 'react';
import { Layout } from 'antd'
const { Content } = Layout

interface Props {
    // empty
}

export const Health: React.StatelessComponent<Props> = ({

}) => {
  return (
    <Content style={{ padding: 24 }}>
      <h1>Health check</h1>
    </Content>
  )
}
