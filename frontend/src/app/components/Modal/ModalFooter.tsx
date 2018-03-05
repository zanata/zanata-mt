import * as React from 'react';

interface Props {
  children?: React.ReactNode | React.ReactNode[]
}

export const ModalFooter: React.StatelessComponent<Props> = ({
   children
}) => {
  return (
    <div className='modal-footer'>
      {children}
    </div>
  )
}