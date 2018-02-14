import * as React from 'react';

interface AppProps {
    // empty
}

const App: React.StatelessComponent<AppProps> = ({
     ...props
 }) => {
    return (
    <div className='d-block mx-auto text-center'>
        <div className='p-3 mb-2 mt-2 bg-info text-white'>
           Machine Translations API service
        </div>
    </div>
    )
}

App.propTypes = {
    // empty
}

export default App
