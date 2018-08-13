// @ts-nocheck
import React from 'react'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'

/**
 * Text input that can switch between text field and label
 * by using attribute `editing`
 */

export interface Props {
    children?: string,
    editable?: boolean,
    editing?: boolean,
    maxLength?: number,
    placeholder?: string,
    emptyReadOnlyText?: string,
    title?: string,
    className?: string
}

export class EditableText extends React.Component<Props, {}> {

    render () {
        const {
            children = '',
            editable = false,
            editing = false,
            emptyReadOnlyText = '',
            placeholder = '',
            title,
            maxLength,
            ...props
        } = this.props

        if (editable && editing) {
            const cssClass = 'textInput w-100 tl ' +
                    (editable ? 'editable' : 'text') + (children ? '' : ' txt-muted')
            return (
                <Input className={cssClass}
                   {...props}
                   maxLength={maxLength}
                   placeholder={placeholder}
                   value={children}>
                    test text
                </Input>
            )
        }
        const emptyText = editable ? placeholder : emptyReadOnlyText
        const content = children ||
                <span className='txt-muted'>{emptyText}</span>
        return (
                <Row className='textInput w-100 tl text ellipsis'
                    title={title}>
                    {content}
                </Row>
        ) }
}

export default EditableText
