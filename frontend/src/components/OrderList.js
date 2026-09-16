import React, { useState, useEffect } from 'react';
import axios from 'axios';
import styles from '../styles/OrderList.module.css'; // Import CSS module

const OrderList = () => {
    const [orders, setOrders] = useState([]);
    const [selectedOrder, setSelectedOrder] = useState(null); // Stores the selected order's details
    const userId = localStorage.getItem('userid'); // Get the user ID from localStorage

    // Fetch order history
    const fetchOrders = async () => {
        try {
            const response = await axios.get(`http://localhost:8443/movie/api/orders/user?userId=${userId}`);
            setOrders(response.data);
        } catch (error) {
            console.error('Failed to fetch orders', error);
        }
    };

    // Fetch a single order's details
    const fetchOrderDetail = async (orderNumber) => {
        try {
            const response = await axios.get(`http://localhost:8443/movie/api/orders/details?orderNumber=${orderNumber}`);
            setSelectedOrder(response.data); // Save the order details
        } catch (error) {
            console.error('Failed to fetch order details', error);
        }
    };

    // Automatically fetch order history when the component mounts
    useEffect(() => {
        if (userId) {
            fetchOrders(); // Automatically fetch order history when the user ID is available
        } else {
            console.error('User is not logged in, cannot fetch orders');
        }
    }, [userId]);

    return (
        <div className={styles.pageContainer}>
            <h2 className={styles.heading}>Order History</h2>

            {/* Display order history */}
            {orders.length > 0 ? (
                <ul className={styles.orderList}>
                    {orders.map((order) => (
                        <li className={styles.orderItem} key={order.orderNumber}>
                            <div className={styles.orderDetails}>
                                <p><strong>Order Number:</strong> {order.orderNumber}</p>
                                <p><strong>Amount:</strong> NT${order.amount}</p>
                                <p><strong>Description:</strong> {order.description}</p>
                                <p><strong>Item Name:</strong> {order.itemName}</p>
                                <p><strong>Payment Status:</strong> {order.isPaid ? 'Paid' : 'Unpaid'}</p>
                                <button
                                    className={styles.detailsButton}
                                    onClick={() => fetchOrderDetail(order.orderNumber)}>
                                    View Details
                                </button>
                            </div>
                        </li>
                    ))}
                </ul>
            ) : (
                <p className={styles.noOrdersMessage}>No order history found.</p>
            )}

            {/* Display the selected order's details */}
            {selectedOrder && (
                <div className={styles.selectedOrderDetails}>
                    <h3>Order Details</h3>
                    <p><strong>Order Number:</strong> {selectedOrder.orderNumber}</p>
                    <p><strong>User ID:</strong> {selectedOrder.userId}</p>
                    <p><strong>Amount:</strong> {selectedOrder.amount}</p>
                    <p><strong>Description:</strong> {selectedOrder.description}</p>
                    <p><strong>Item Name:</strong> {selectedOrder.itemName}</p>
                    <p><strong>Created Time:</strong> {selectedOrder.createdDate}</p>
                    <p><strong>Payment Status:</strong> {selectedOrder.isPaid ? 'Paid' : 'Unpaid'}</p>
                </div>
            )}
        </div>
    );
};

export default OrderList;
