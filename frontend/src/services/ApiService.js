
import axios from 'axios';


const API_URL = '/api';

const ApiService = axios.create({
    baseURL: API_URL,
});

export default {

    getUsers: () => ApiService.get('/users'),
    createUser: (user) => ApiService.post('/users', user),
    updateUser: (id, user) => ApiService.put(`/users/${id}`, user),
    deleteUser: (id) => ApiService.delete(`/users/${id}`),


    getQrCodesByUser: (username) =>
        ApiService.get('/qr/by-user', { params: { username } }),
    generateQr: (text, username) =>
        ApiService.get('/qr', { params: { text, username } })
};
