import * as React from 'react';

interface NoMatchProps {
    // empty
}

const NoMatch: React.StatelessComponent<NoMatchProps> = ({
  ...props
}) => {
  return (
    <div className='d-block mx-auto text-center'>
      <div className='p-3 mb-2 mt-2 bg-danger text-white'>
        This is an invalid URL
      </div>
    </div>
  )
}

NoMatch.propTypes = {
    // empty
}

export default NoMatch
