import * as React from 'react';

interface Props {
  children?: React.ReactNode | React.ReactNode[]
}

export const ModalBody: React.StatelessComponent<Props> = ({
   children
 }) => {
  return (
    <div className='modal-body'>
      {children}
    </div>
  )
}