// src/main/index.js
import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css'; // можно поместить ваши глобальные стили здесь
import App from './App';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);
