// @ts-nocheck
import React from 'react'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import {TextInput} from "../TextInput";

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
    public render () {
        const {
            children = '',
            editable = true,
            editing = true,
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
                    <TextInput
                               {...props}
                               maxLength={maxLength}
                               placeholder={placeholder}
                               value={children}
                               className={cssClass}>
                        Test
                    </TextInput>
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
