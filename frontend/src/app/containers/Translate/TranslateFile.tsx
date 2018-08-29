import * as React from 'react';
import { connect, GenericDispatch } from 'react-redux';
import { RootState } from '../../reducers';
import {
  translateFile,
  fetchSupportedLocales
} from '../../actions/translateFile';
import {Alert} from '../../components'
import {ErrorData, Locale} from "../../types/models"
import {Form, Layout, Button, Select, Upload, Icon} from 'antd'
import FormItem from 'antd/es/form/FormItem'
import {FormComponentProps} from 'antd/lib/form'
import {isEmpty} from 'lodash'
import {RcFile, UploadChangeParam} from 'antd/lib/upload/interface'
import {UploadFile} from 'antd/es/upload/interface'

const { Content } = Layout

interface Props {
  uploading: boolean,
  loading: boolean,
  supportedLocales: Locale[],
  handleTranslateFile: typeof translateFile,
  getSupportedLocales: () => void
  errorData?: ErrorData
}

interface State {
  fileList: UploadFile[],
  file: File
  fromLocaleCode: string,
  toLocaleCode: string
}

const SUPPORTED_FILES = ['POT']

class TranslateFile extends React.Component<Props & FormComponentProps, State> {
  constructor(props: Props & FormComponentProps) {
    super(props)
    this.state = {
      fileList: [],
      file: undefined,
      fromLocaleCode: undefined,
      toLocaleCode: undefined
    }
  }

  componentDidMount() {
    this.props.getSupportedLocales()
  }

  private handleUploadFile = () => {
    const ext = this.state.file.name.split('.').pop()
    this.props.handleTranslateFile(this.state.file,
      this.state.fromLocaleCode, this.state.toLocaleCode, ext.toUpperCase())
  }

  private updateFormField = (field: string, value: any) => {
    this.setState(prevState => ({
      ...prevState,
      [field]: value
    }))
  }

  private handleSubmit(e: React.FormEvent<Form>) {
    e.preventDefault()
  }

  private beforeFileUpload(file: RcFile) {
    this.setState({
      file
    })
    return false
  }

  // this is for ui only, the actual file is in beforeFileUpload method
  private onFileChange(info: UploadChangeParam) {
    this.setState({
      fileList: info.fileList.splice(-1)
    })
  }

  public render() {
    const {uploading, errorData, supportedLocales, form} = this.props
    const { getFieldDecorator }  = form
    const alert = errorData && <Alert data={errorData} dismissible={true}/>
    const acceptedExtensions = SUPPORTED_FILES.map((f) => {
      return '.' + f;
    }).join()

    const {fromLocaleCode, toLocaleCode, file} = this.state
    const enableUpload = !uploading && !isEmpty(fromLocaleCode) &&
      !isEmpty(toLocaleCode) && file !== undefined && (fromLocaleCode !== toLocaleCode)

    return (
      <Content style={{padding: 24}}>
        {alert}
        <h1>Translate file</h1>
        <Form onSubmit={this.handleSubmit}>
          <FormItem label={'Source file (supported format: ' + SUPPORTED_FILES.join(',') + ')'}>
            <Upload accept={acceptedExtensions}
              disabled={uploading}
              onChange={(info) => this.onFileChange(info)}
              beforeUpload={(file) => this.beforeFileUpload(file)}
              multiple={false}
              fileList={this.state.fileList}>
              <Button>
                <Icon type="upload" /> Select a file
              </Button>
            </Upload>
          </FormItem>
          <FormItem label='File type'>
            <Select onChange={(e) => this.updateFormField('type', e)}
              defaultValue={SUPPORTED_FILES[0]}
              style={{ width: '100%' }}
              placeholder='file type (ONLY POT file is supported)'
              disabled={true}>
              {SUPPORTED_FILES.map((f) => {
                return (<Select.Option key={f}>
                  {f}
                </Select.Option>)
              })}
            </Select>
          </FormItem>

          <FormItem label='Source language'>
            {getFieldDecorator('fromLocaleCode', {
              rules: [{required: true, message: 'Please select source locale'}],
            })(
              <Select style={{width: '100%'}}
                placeholder='select a locale'
                onChange={(e) => this.updateFormField('fromLocaleCode', e)}>
                {supportedLocales && supportedLocales.map((locale) => {
                  return (<Select.Option
                    key={locale.localeCode}>{locale.name}</Select.Option>)
                })}
              </Select>
            )}
          </FormItem>
          <FormItem label='Target language'>
            {getFieldDecorator('toLocaleCode', {
              rules: [{required: true, message: 'Please select target locale'}],
            })(
              <Select style={{width: '100%'}}
                placeholder='select a locale'
                onChange={(e) => this.updateFormField('toLocaleCode', e)}>
                {supportedLocales && supportedLocales.map((locale) => {
                  return (<Select.Option
                    key={locale.localeCode}>{locale.name}</Select.Option>)
                })}
              </Select>
            )}
          </FormItem>
          <FormItem>
            <Button type='primary' loading={uploading}
              disabled={!enableUpload}
              onClick={this.handleUploadFile}>
              Translate
            </Button>
          </FormItem>
        </Form>
      </Content>
    )
  }
}

function mapStateToProps(state: RootState) {
  const {loading, uploading, supportedLocales, errorData} = state.translateFile;
  return {
    uploading,
    loading,
    supportedLocales,
    errorData
  }
}

function mapDispatchToProps(dispatch: GenericDispatch) {
  return {
    handleTranslateFile: (file: File, fromLocaleCode: string, toLocaleCode: string, type: string) =>
      dispatch(translateFile(file, fromLocaleCode, toLocaleCode, type)),
    getSupportedLocales: () => dispatch(fetchSupportedLocales())
  }
}

export default connect(mapStateToProps, mapDispatchToProps)
(Form.create({})(TranslateFile) as any)
