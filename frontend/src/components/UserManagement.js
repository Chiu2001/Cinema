import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';
import styles from '../styles/UserManagement.module.css';

const UserManagement = () => {
    const [users, setUsers] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [role, setRole] = useState(() => {
        const storedRoles = JSON.parse(localStorage.getItem('roles'));
        return (storedRoles && storedRoles.includes('ROLE_MANAGER')) ? 'ROLE_MANAGER' : 'USER';
    });
    const [currentPage, setCurrentPage] = useState(1);
    const usersPerPage = 10;
    const navigate = useNavigate(); // Use useNavigate for page navigation

    // Fetch user data
    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const response = await axios.get('http://localhost:8443/movie/api/admin/getusers', {
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    }
                });
                setUsers(response.data);
            } catch (error) {
                console.error('Error fetching users:', error);
            }
        };

        fetchUsers();
    }, []);

    // Toggle a user's role
    const toggleRole = async (id, newRole) => {
        console.log('Current role:', role);
        if (role === 'ROLE_MANAGER') {
            try {
                await axios.put(`http://localhost:8443/movie/api/manager/${id}/role`, null, {
                    params: { role: newRole }, // Pass the role as a query parameter
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    }
                });
                setUsers(users.map(user =>
                    user.user_id === id ? { ...user, role: newRole } : user
                ));
            } catch (error) {
                console.error('Error updating role:', error);
            }
        } else {
            console.error('Unauthorized attempt to change role');
        }
    };

    // Filter by search term
    const filteredUsers = users.filter(user =>
        user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(searchTerm.toLowerCase())
    );

    // Pagination logic
    const startIndex = (currentPage - 1) * usersPerPage;
    const paginatedUsers = filteredUsers.slice(startIndex, startIndex + usersPerPage);

    const handlePageChange = (pageNumber) => {
        setCurrentPage(pageNumber);
    };

    // Post-login processing
    const handleLoginResponse = (response) => {
        const token = response.token; // Extract the token from the backend response
        const roles = response.roles; // Extract the roles from the backend response

        // Store the JWT in localStorage
        localStorage.setItem('token', token);

        // Store the role information in localStorage
        localStorage.setItem('roles', JSON.stringify(roles));
        console.log(roles);
    };

    return (
        <div>
            <div className={styles.headerST}>
                <ul>
                    <li><Link to="/schedule">Schedule Management</Link></li>
                    <li><Link to="/news">News Management</Link></li>
                    <li><Link to="/user_management">User Management</Link></li>
                    <li><Link to="/order">Search Orders</Link></li>
                </ul>
            </div>
            <div className={styles.container}>
                <h1>User Management</h1>
                <input
                    type="text"
                    placeholder="Search by username or email"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className={styles.searchBox}
                />
                <table className={styles.table}>
                    <thead>
                        <tr>
                            <th>id</th>
                            <th>Username</th>
                            <th>Email</th>
                            <th>Birth Date</th>
                            <th>Gender</th>
                            <th>Created Time</th>
                            <th>Role</th>
                        </tr>
                    </thead>
                    <tbody>
                        {paginatedUsers.map(user => (
                            <tr key={user.user_id}>
                                <td>{user.user_id}</td>
                                <td>{user.username}</td>
                                <td>{user.email}</td>
                                <td>{user.birthDate}</td>
                                <td>{user.gender}</td>
                                <td>{user.createdTime}</td>
                                <td>
                                    {role === 'ROLE_MANAGER' ? (
                                        <select
                                            value={user.role}
                                            onChange={(e) => toggleRole(user.user_id, e.target.value)}
                                            className={styles.roleDropdown}
                                        >
                                            <option value="USER">USER</option>
                                            <option value="ADMIN">ADMIN</option>
                                            <option value="MANAGER">MANAGER</option>
                                        </select>
                                    ) : (
                                        <span>{user.role}</span>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                <div className={styles.pagination}>
                    {[...Array(Math.ceil(filteredUsers.length / usersPerPage)).keys()].map(pageNumber => (
                        <button
                            key={pageNumber + 1}
                            onClick={() => handlePageChange(pageNumber + 1)}
                            className={`${styles.pageButton} ${currentPage === pageNumber + 1 ? styles.activePage : ''}`}
                        >
                            {pageNumber + 1}
                        </button>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default UserManagement;

