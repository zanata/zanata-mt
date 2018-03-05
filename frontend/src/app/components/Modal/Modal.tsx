import * as React from 'react';
import {ModalBody} from "./ModalBody";
import {ModalFooter} from "./ModalFooter";

export const enum size {
  big = 'modal-lg',
  small = 'modal-sm'
}

interface Props {
  show: boolean
  title?: string
  body?: React.ReactElement<typeof ModalBody>
  footer?: React.ReactElement<typeof ModalFooter>
  onClose: () => void
  size?: size
}

export const Modal: React.StatelessComponent<Props>  = ({
  show, title, body, footer, onClose, size
  }) => {
  const cssClass = 'modal fade' + (show ? ' show modal-open d-block' : '')
  return (
    <div className={cssClass} tabIndex={-1} role='dialog' aria-hidden='true'>
      <div className={'modal-dialog ' + (size ? size : '')}>
        <div className='modal-content'>
          <div className='modal-header'>
            <h5 className='modal-title'>{title}</h5>
            <button className='close' aria-label='Close'
              onClick={onClose}>
              <span aria-hidden='true'>&times;</span>
            </button>
          </div>
          {body}
          {footer}
        </div>
      </div>
    </div>
  )
}

