import * as React from 'react';

// interface NoMatchProps {
//     // empty
// }

const NoMatch: React.StatelessComponent<{}> = ({
  ...props
}) => {
  return (
    <div className='d-block justify-content-center'>
      <div className='p-3 mt-3 bg-warning text-white text-center'>
        <p>
          Sorry, we cannot find the page you are looking for
        </p>
        <span className='display-1'>404</span>
      </div>
    </div>
  )
}

NoMatch.propTypes = {
    // empty
}

export default NoMatch
