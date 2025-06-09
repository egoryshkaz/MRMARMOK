// src/main/services/ApiService.js
import axios from 'axios';

// Если указан proxy, можно использовать относительный URL.
const API_URL = '/api';

const ApiService = axios.create({
    baseURL: API_URL,
});

export default {
    // Пользовательский API
    getUsers: () => ApiService.get('/users'),
    createUser: (user) => ApiService.post('/users', user),
    updateUser: (id, user) => ApiService.put(`/users/${id}`, user),
    deleteUser: (id) => ApiService.delete(`/users/${id}`),

    // QR-коды (ManyToMany: User <-> QrEntity)
    getQrCodesByUser: (username) =>
        ApiService.get('/qr/by-user', { params: { username } }),
    generateQr: (text, username) =>
        ApiService.get('/qr', { params: { text, username } })
};
