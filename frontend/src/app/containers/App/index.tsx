import * as React from 'react';
import { Alert } from '../../components'
import {ErrorData} from "../../types/models"

import { Layout } from 'antd'
const { Content } = Layout

interface AppProps {
  errorData?: ErrorData
}

const App: React.StatelessComponent<AppProps> = ({
    errorData
 }) => {
    const alert = errorData && <Alert data={errorData} dismissible={true}/>
    return (
      <Content style={{ padding: 24 }}>
        {alert}
        <h1>Home</h1>
      </Content>
    )
}

export default App
