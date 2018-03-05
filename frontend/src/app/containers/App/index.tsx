import * as React from 'react';
import { Alert } from '../../components'
import {ErrorData} from "../../types/models"

interface AppProps {
  errorData?: ErrorData
}

const App: React.StatelessComponent<AppProps> = ({
    errorData
 }) => {
    const alert = errorData && <Alert data={errorData} dismissible={true}/>
    return (
      <div className='container justify-content-center mt-3'>
        {alert}
        <h1>Home</h1>
        <div className='p-3 mt-3'>
        </div>
      </div>
    )
}

export default App
