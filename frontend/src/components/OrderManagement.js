import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import styles from '../styles/OrderManagement.module.css';
import { API_BASE_URL } from '../apiConfig';

const OrderManagement = () => {
    const [orders, setOrders] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const ordersPerPage = 10;

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) {
            alert('Please log in first');
            window.location.href = '/login';
            return;
        }

        axios.get(`${API_BASE_URL}/api/admin/tickets`, {
            headers: { 'Authorization': `Bearer ${token}` }
        })
            .then(response => {
                setOrders(response.data);
            })
            .catch(error => {
                console.error('Error fetching orders:', error);
                if (error.response && error.response.status === 401) {
                    alert('Token has expired or is invalid, please log in again');
                    localStorage.removeItem('token');
                    window.location.href = '/login';
                }
            });
    }, []);

    const filteredOrders = orders.filter(order =>
        order.order_id.toString().includes(searchTerm) ||
        order.ticket_id.toString().includes(searchTerm) ||
        order.seat_id.toString().includes(searchTerm) ||
        order.showtime_id.toString().includes(searchTerm)
    );

    const startIndex = (currentPage - 1) * ordersPerPage;
    const paginatedOrders = filteredOrders.slice(startIndex, startIndex + ordersPerPage);

    const handlePageChange = (pageNumber) => {
        setCurrentPage(pageNumber);
    };

    return (
        <div>
        <div className={styles.headerST}>
            <ul>
                <li><Link to="/schedule">Schedule Management</Link></li>
                <li><Link to="/news">News Management</Link></li>
                <li><Link to="/user_management">User Management</Link></li>
                <li><Link to="/order">Order Lookup</Link></li>
            </ul>
            </div>
            <div className={styles.container}>
                <h1>Order Lookup</h1>
                <input
                    type="text"
                    placeholder="Search by order number, ticket number, seat number, or showtime number"
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className={styles.searchBox}
                />
                <table className={styles.table}>
                    <thead>
                        <tr>
                            <th>Order Number</th>
                            <th>Ticket Number</th>
                            <th>Price</th>
                            <th>Purchase Time</th>
                            <th>Seat Number</th>
                            <th>Showtime Number</th>
                        </tr>
                    </thead>
                    <tbody>
                        {paginatedOrders.map(order => (
                            <tr key={order.ticket_id}>
                                <td>{order.order_id}</td>
                                <td>{order.ticket_id}</td>
                                <td>{order.price}</td>
                                <td>{order.purchase_time}</td>
                                <td>{order.seat_id}</td>
                                <td>{order.showtime_id}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                <div className={styles.pagination}>
                    {[...Array(Math.ceil(filteredOrders.length / ordersPerPage)).keys()].map(pageNumber => (
                        <button
                            key={pageNumber + 1}
                            onClick={() => handlePageChange(pageNumber + 1)}
                            className={currentPage === pageNumber + 1 ? styles.activePage : ''}
                        >
                            {pageNumber + 1}
                        </button>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default OrderManagement;
