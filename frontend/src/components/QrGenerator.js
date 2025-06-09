
import React, { useState } from 'react';
import ApiService from '../services/ApiService';

function QrGenerator({ username, onQrGenerated }) {
    const [text, setText] = useState('');
    const [loading, setLoading] = useState(false);

    const handleGenerate = async () => {
        if (!username || !text.trim()) return;
        setLoading(true);
        try {
            const response = await ApiService.generateQr(text, username);
            if (onQrGenerated) {
                onQrGenerated(response.data.qrCode);
            }
        } catch (error) {
            console.error('Error generating QR code:', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ marginTop: '10px' }}>
            <h3>Generate New QR</h3>
            <input
                type="text"
                value={text}
                onChange={e => setText(e.target.value)}
                placeholder="Enter text for QR"
            />
            <button onClick={handleGenerate} disabled={loading || !text.trim()} style={{ marginLeft: '10px' }}>
                {loading ? 'Generating...' : 'Generate'}
            </button>
        </div>
    );
}

export default QrGenerator;
