import React, { useState, useEffect } from 'react';
import axios from 'axios';
import styles from '../styles/OrderList.module.css'; // Import CSS module
import { API_BASE_URL } from '../apiConfig';


const OrderList = () => {
    const [orders, setOrders] = useState([]);
    const [selectedOrder, setSelectedOrder] = useState(null); // Used to store the selected order's details
    const userId = localStorage.getItem('userid'); // Get the user ID from localStorage

    // Fetch order history
    const fetchOrders = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`${API_BASE_URL}/api/orders/user?userId=${userId}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            setOrders(response.data);
        } catch (error) {
            console.error('Unable to fetch orders', error);
        }
    };

    // Fetch a single order's details
    const fetchOrderDetail = async (orderNumber) => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`${API_BASE_URL}/api/orders/details?orderNumber=${orderNumber}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            setSelectedOrder(response.data); // Save the order details
        } catch (error) {
            console.error('Unable to fetch order details', error);
        }
    };

    const payForOrder = async (order) => {
        try {
            const response = await axios.post(`${API_BASE_URL}/api/stripe/create-checkout-session`, {
                userId: parseInt(userId, 10),
                orderNumber: order.orderNumber,
                description: order.description,
                itemName: order.itemName,
                items: [
                    {
                        name: order.itemName,
                        unitAmount: Math.round(order.amount * 100), // Stripe amounts are in cents
                        quantity: 1,
                    }
                ],
            });

            if (response.data.url) {
                window.location.href = response.data.url;
            } else {
                alert('Unable to create payment, please try again later');
            }
        } catch (error) {
            console.error('Failed to create payment:', error);
            alert('Unable to create payment, please try again later');
        }
    };

    // Automatically fetch order history when the component mounts
    useEffect(() => {
        if (userId) {
            fetchOrders(); // Automatically fetch order history once the user ID is available
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
                                {!order.isPaid && (
                                    <button onClick={() => payForOrder(order)}>
                                        Pay Now
                                    </button>
                                )}
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
                    <p><strong>Amount:</strong> NT${selectedOrder.amount}</p>
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
