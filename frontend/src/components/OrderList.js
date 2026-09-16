import React, { useState, useEffect } from 'react';
import axios from 'axios';
import styles from '../styles/OrderList.module.css'; // 引入 CSS 模組
import { API_BASE_URL } from '../apiConfig';


const OrderList = () => {
    const [orders, setOrders] = useState([]);
    const [selectedOrder, setSelectedOrder] = useState(null); // 用於存儲選中的訂單詳情
    const userId = localStorage.getItem('userid'); // 從 localStorage 中獲取用戶ID

    // 查詢歷史訂單
    const fetchOrders = async () => {
        try {
            const response = await axios.get(`${API_BASE_URL}/api/orders/user?userId=${userId}`);
            setOrders(response.data);
        } catch (error) {
            console.error('無法獲取訂單', error);
        }
    };

    // 查詢單筆訂單詳情
    const fetchOrderDetail = async (orderNumber) => {
        try {
            const response = await axios.get(`${API_BASE_URL}/api/orders/details?orderNumber=${orderNumber}`);
            setSelectedOrder(response.data); // 保存訂單詳情
        } catch (error) {
            console.error('無法獲取訂單詳情', error);
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
                        unitAmount: Math.round(order.amount * 100), // Stripe 金額單位是分
                        quantity: 1,
                    }
                ],
            });

            if (response.data.url) {
                window.location.href = response.data.url;
            } else {
                alert('無法建立付款，請稍後再試');
            }
        } catch (error) {
            console.error('建立付款失敗:', error);
            alert('無法建立付款，請稍後再試');
        }
    };

    // 當組件加載時自動查詢歷史訂單
    useEffect(() => {
        if (userId) {
            fetchOrders(); // 當用戶ID存在時，自動查詢歷史訂單
        } else {
            console.error('用戶未登入，無法查詢訂單');
        }
    }, [userId]);

    return (
        <div className={styles.pageContainer}>
            <h2 className={styles.heading}>歷史訂單查詢</h2>

            {/* 顯示歷史訂單 */}
            {orders.length > 0 ? (
                <ul className={styles.orderList}>
                    {orders.map((order) => (
                        <li className={styles.orderItem} key={order.orderNumber}>
                            <div className={styles.orderDetails}>
                                <p><strong>訂單號碼:</strong> {order.orderNumber}</p>
                                <p><strong>金額:</strong> {order.amount} 元</p>
                                <p><strong>描述:</strong> {order.description}</p>
                                <p><strong>物品名稱:</strong> {order.itemName}</p>
                                <p><strong>支付狀態:</strong> {order.isPaid ? '已支付' : '未支付'}</p>
                                <button
                                    className={styles.detailsButton}
                                    onClick={() => fetchOrderDetail(order.orderNumber)}>
                                    查看詳情
                                </button>
                                {!order.isPaid && (
                                    <button onClick={() => payForOrder(order)}>
                                        去付款
                                    </button>
                                )}
                            </div>
                        </li>
                    ))}
                </ul>
            ) : (
                <p className={styles.noOrdersMessage}>無歷史訂單記錄。</p>
            )}

            {/* 顯示選中的訂單詳情 */}
            {selectedOrder && (
                <div className={styles.selectedOrderDetails}>
                    <h3>訂單詳情</h3>
                    <p><strong>訂單號碼:</strong> {selectedOrder.orderNumber}</p>
                    <p><strong>用戶ID:</strong> {selectedOrder.userId}</p>
                    <p><strong>金額:</strong> {selectedOrder.amount} 元</p>
                    <p><strong>描述:</strong> {selectedOrder.description}</p>
                    <p><strong>物品名稱:</strong> {selectedOrder.itemName}</p>
                    <p><strong>創建時間:</strong> {selectedOrder.createdDate}</p>
                    <p><strong>支付狀態:</strong> {selectedOrder.isPaid ? '已支付' : '未支付'}</p>
                </div>
            )}
        </div>
    );
};

export default OrderList;
