import * as React from 'react';

export class SourceTrans extends React.Component<{}> {
    public render() {
        const sourceText = 'Good morning'
        return (
                <div className="TransUnit-panel">
                    <div className="TransUnit-item">
                        <pre className='TransUnit-text TransUnit-source'>{sourceText}</pre>
                    </div>
                </div>
        )
    }
}
