
import React from 'react';

function QrList({ qrCodes }) {
    if (!qrCodes || qrCodes.length === 0) {
        return <p>No QR codes available.</p>;
    }

    return (
        <div>
            <h2>QR Codes</h2>
            <div style={{ display: 'flex', flexWrap: 'wrap' }}>
                {qrCodes.map(qr => (
                    <div key={qr.id} style={{ margin: '10px', textAlign: 'center' }}>
                        <p>{qr.content}</p>
                        <img
                            src={`data:image/png;base64,${qr.qrCodeBase64}`}
                            alt={qr.content}
                            style={{ width: '150px', height: '150px' }}
                        />
                    </div>
                ))}
            </div>
        </div>
    );
}

export default QrList;
