
import React, { useState } from 'react';
import UserList from './components/UserList';
import UserForm from './components/UserForm';
import QrList from './components/QrList';
import QrGenerator from './components/QrGenerator';
import ApiService from './services/ApiService';

function App() {
    const [selectedUser, setSelectedUser] = useState(null);
    const [qrCodes, setQrCodes] = useState([]);
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [editUser, setEditUser] = useState(null);
    const [refreshFlag, setRefreshFlag] = useState(false);

    const loadQrCodes = async (user) => {
        try {
            const response = await ApiService.getQrCodesByUser(user.username);
            setQrCodes(response.data);
        } catch (error) {
            console.error('Error loading QR codes:', error);
        }
    };

    const handleSelectUser = (user) => {
        setSelectedUser(user);
        setEditUser(null);
        loadQrCodes(user);
    };

    const handleCreateUser = async (userData) => {
        try {
            await ApiService.createUser(userData);
            setShowCreateForm(false);
            setRefreshFlag(!refreshFlag);
        } catch (error) {
            console.error('Error creating user:', error);
        }
    };

    const handleUpdateUser = async (userData) => {
        try {
            await ApiService.updateUser(selectedUser.id, userData);
            setEditUser(null);
            setSelectedUser({ ...selectedUser, ...userData });
            setRefreshFlag(!refreshFlag);
        } catch (error) {
            console.error('Error updating user:', error);
        }
    };

    const handleDeleteUser = async (user) => {
        if (window.confirm(`Are you sure you want to delete user ${user.username}?`)) {
            try {
                await ApiService.deleteUser(user.id);
                setSelectedUser(null);
                setQrCodes([]);
                setRefreshFlag(!refreshFlag);
            } catch (error) {
                console.error('Error deleting user:', error);
            }
        }
    };

    return (
        <div style={{ display: 'flex', padding: '20px' }}>
            {/* Сайдбар: пользователи и форма создания */}
            <div style={{ width: '300px', marginRight: '20px' }}>
                <UserList onSelectUser={handleSelectUser} refreshFlag={refreshFlag} />
                <button onClick={() => setShowCreateForm(true)} style={{ marginTop: '10px' }}>
                    Create New User
                </button>
                {showCreateForm && (
                    <UserForm
                        onSubmit={handleCreateUser}
                        onCancel={() => setShowCreateForm(false)}
                    />
                )}
            </div>

            <div style={{ flexGrow: 1 }}>
                {selectedUser ? (
                    <>
                        <h2>User: {selectedUser.username}</h2>
                        <button onClick={() => setEditUser(selectedUser)}>Edit User</button>
                        <button onClick={() => handleDeleteUser(selectedUser)} style={{ marginLeft: '10px' }}>
                            Delete User
                        </button>
                        {editUser && (
                            <UserForm
                                user={selectedUser}
                                onSubmit={handleUpdateUser}
                                onCancel={() => setEditUser(null)}
                            />
                        )}
                        <QrGenerator
                            username={selectedUser.username}
                            onQrGenerated={(qrCode) => loadQrCodes(selectedUser)}
                        />
                        <QrList qrCodes={qrCodes} />
                    </>
                ) : (
                    <h2>Please select a user to view details</h2>
                )}
            </div>
        </div>
    );
}

export default App;
