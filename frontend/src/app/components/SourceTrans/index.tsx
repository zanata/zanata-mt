import * as React from 'react';

export class SourceTrans extends React.Component<{}> {
    public render() {
        const sourceText = 'ant-layout-content ant-layout-contentant-layout-contentant-layout-contentant-layout-contentant-layout-contentant-layout-contentant-layout-contentant-layout-contentant-layout-contentant-layout-content'
        return (
                <div className="TransUnit-panel">
                    <div className="TransUnit-item">
                        <pre className='TransUnit-text TransUnit-source'>{sourceText}</pre>
                    </div>
                </div>
        )
    }
}
