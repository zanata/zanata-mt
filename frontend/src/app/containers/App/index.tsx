import * as React from 'react';

const App: React.StatelessComponent<{}> = ({
     ...props
 }) => {
    return (
      <div className='container justify-content-center'>
        <h1>Home</h1>
      </div>
    )
}

App.propTypes = {
    // empty
}

export default App
