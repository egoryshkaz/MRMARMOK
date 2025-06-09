// src/main/components/UserForm.js
import React, { useState, useEffect } from 'react';

function UserForm({ user, onSubmit, onCancel }) {
    const [username, setUsername] = useState('');

    useEffect(() => {
        if (user) {
            setUsername(user.username);
        }
    }, [user]);

    const handleSubmit = (e) => {
        e.preventDefault();
        if (username.trim() === '') return;
        onSubmit({ username: username.trim() });
        setUsername('');
    };

    return (
        <form onSubmit={handleSubmit} style={{ border: '1px solid #ccc', padding: '10px', marginTop: '10px' }}>
            <h3>{user ? 'Edit User' : 'Create User'}</h3>
            <div>
                <label>Username: </label>
                <input
                    type="text"
                    value={username}
                    onChange={e => setUsername(e.target.value)}
                    required
                />
            </div>
            <div style={{ marginTop: '10px' }}>
                <button type="submit">{user ? 'Update' : 'Create'}</button>
                {onCancel && (
                    <button type="button" onClick={onCancel} style={{ marginLeft: '10px' }}>
                        Cancel
                    </button>
                )}
            </div>
        </form>
    );
}

export default UserForm;
