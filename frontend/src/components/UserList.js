// src/main/components/UserList.js
import React, { useState, useEffect } from 'react';
import ApiService from '../services/ApiService';

function UserList({ onSelectUser, refreshFlag }) {
    const [users, setUsers] = useState([]);

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const response = await ApiService.getUsers();
                console.log("Response data:", response.data);

                // Если ответ уже является массивом, то:
                if (Array.isArray(response.data)) {
                    setUsers(response.data);
                }
                // Если ответ - объект с массивом пользователей под ключом 'users'
                else if (response.data && Array.isArray(response.data.users)) {
                    setUsers(response.data.users);
                } else {
                    // Иначе установить пустой массив
                    setUsers([]);
                    console.warn("Неверный формат полученных данных");
                }
            } catch (error) {
                console.error('Error fetching users:', error);
            }
        };

        fetchUsers();
    }, [refreshFlag]);

    // Если users не массив, можно отобразить сообщение
    if (!Array.isArray(users)) {
        return <div>Unexpected data format</div>;
    }

    return (
        <div>
            <h2>Users</h2>
            <ul style={{ listStyle: 'none', padding: 0 }}>
                {users.map(user => (
                    <li key={user.id} style={{ margin: '5px 0' }}>
                        <button onClick={() => onSelectUser(user)}>
                            {user.username}
                        </button>
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default UserList;
