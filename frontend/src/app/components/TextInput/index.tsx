// @ts-nocheck
import React from 'react'
import * as ReactDOM from 'react-dom'
import TextareaAutosize from 'react-textarea-autosize'

export interface Props {
    id?:string,
    accessibilityLabel?:string,
    autoComplete?:boolean,
    autoFocus?:boolean,
    clearTextOnFocus?:boolean,
    className?:string,
    defaultValue?:string,
    editable?:boolean,
    keyboard?:['default', 'email-address', 'numeric',
            'phone-pad', 'url'],
    keyboardType?:string,
    maxLength?:number,
    maxNumberOfLines?:number,
    multiline?:boolean,
    numberOfLines?:number,
    onBlur?:Function,
    onKeyDown?:Function,
    onChange?:Function,
    onChangeText?:Function,
    onFocus?:Function,
    onSelectionChange?:Function,
    placeholder?:string,
    secureTextEntry?:boolean,
    selectTextOnFocus?:boolean,
    value?:string
}
/**
 * TextInput component <input> or <textArea> depending on property 'multiline'.
 */
export class TextInput extends React.Component<Props, {}> {
    _onBlur = (e:any) => {
        const {onBlur} = this.props
        if (onBlur) {
            onBlur(e)
        }
    }

    _onChange = (e:any) => {
        const {onChange, onChangeText} = this.props
        if (onChangeText) onChangeText(e.target.value)
        if (onChange) onChange(e)
    }

    _onFocus = (e:any) => {
        const {clearTextOnFocus, onFocus, selectTextOnFocus} = this.props
        const node = ReactDOM.findDOMNode(this)
        if (clearTextOnFocus) node.value = ''
        if (selectTextOnFocus) node.select()
        if (onFocus) onFocus(e)
    }

    _onSelectionChange = (e:any) => {
        const {onSelectionChange} = this.props
        const {selectionDirection, selectionEnd, selectionStart} = e.target
        if (onSelectionChange) {
            const event = {
                selectionDirection,
                selectionEnd,
                selectionStart,
                nativeEvent: e.nativeEvent
            }
            onSelectionChange(event)
        }
    }

    _onKeyDown = (e:any) => {
        const {onKeyDown} = this.props
        if (onKeyDown) onKeyDown(e)
    }

    public render () {
        const {
            id,
            accessibilityLabel,
            autoComplete,
            autoFocus,
            editable = true,
            keyboardType = 'default',
            maxLength,
            maxNumberOfLines,
            multiline = false,
            numberOfLines = 2,
            onBlur,
            onChange,
            onChangeText,
            onKeyDown,
            onSelectionChange,
            placeholder,
            secureTextEntry = false,
            value
        } = this.props

        let type

        switch (keyboardType) {
            case 'default':
                break
            case 'email-address':
                type = 'email'
                break
            case 'numeric':
                type = 'number'
                break
            case 'phone-pad':
                type = 'tel'
                break
            case 'url':
                type = 'url'
                break
            default:
                console.error('Unsupported keyboardType.', keyboardType)
                break
        }

        if (secureTextEntry) {
            type = 'password'
        }

        const propsCommon = {
            id: id,
            'aria-label': accessibilityLabel,
            autoComplete: autoComplete && 'on',
            autoFocus,
            className: 'textInput',
            maxLength,
            onBlur: onBlur && this._onBlur,
            onChange: (onChange || onChangeText) && this._onChange,
            onFocus: this._onFocus,
            onSelect: onSelectionChange && this._onSelectionChange,
            onKeyDown: (onKeyDown) && this._onKeyDown,
            placeholder,
            readOnly: !editable,
            value
        }

        if (multiline) {
            const propsMultiline = {
                ...propsCommon,
                maxRows: maxNumberOfLines || numberOfLines,
                minRows: numberOfLines
            }
            return <TextareaAutosize {...propsMultiline} />
        } else {
            const propsSingleline = {
                ...propsCommon,
                type
            }
            return <input {...propsSingleline} />
        }
    }
}
