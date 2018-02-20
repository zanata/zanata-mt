import * as React from 'react';
import { Alert } from '../../components/Alert'

interface AppProps {
  errorData?
}

const App: React.StatelessComponent<AppProps> = ({
    errorData,
     ...props
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
