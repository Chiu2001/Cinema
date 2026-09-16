// import React, { useContext } from 'react';
// import { Link, useNavigate } from 'react-router-dom';
// import { CartContext } from '../CartContext';
// import styles from '../styles/Checkout.module.css'; // 引入 CSS 模組

// export default function CheckOutIn() {
//     const { cartItems, removeCartItem } = useContext(CartContext);
//     const navigate = useNavigate();
//     const cartEmpty = cartItems.length <= 0;
//     const grandTotal = cartItems.reduce((total, item) => {
//         return total + (item.hall.price * item.quantity);
//     }, 0);
//     const freeFood = 350;

//     const payment = async () => {
//         try {
//             const response = await fetch(`${API_BASE_URL}/ecpay/checkout`, {
//                 method: 'POST',
//                 headers: {
//                     'Content-Type': 'application/json',
//                 },
//                 body: JSON.stringify({
//                     userId: localStorage.getItem('userid'),
//                     amount: grandTotal,
//                     description: cartItems.map(item => item.seatNumbers.join(', ')).join('; '),
//                     itemName: cartItems.map(item => item.movie.title).join('; '),
//                 }),
//             });

//             if (!response.ok) {
//                 throw new Error('網路響應錯誤');
//             }

//             const formHtml = await response.text();
//             console.log('Received ECPay HTML:', formHtml); // 檢查返回的 HTML
//             navigate('/ecpay', { state: { ecpayHTML: formHtml } });
//         } catch (error) {
//             console.error('結帳時發生錯誤：', error);
//         }
//     };

//     return (
//         <div className={styles.pageWrapper}>
//             <h1>您的購物車</h1>

//             {cartEmpty ? (
//                 <div className={styles.emptyCartMessage}>
//                     <Link to="/MovieList">
//                         <a>購物車為空</a><br />
//                         <a>前往購票吧</a>
//                     </Link>
//                 </div>
//             ) : (
//                 <div className={styles.cartContainer}>
//                     <div id={styles.cartSection}>
//                         {/* 產品列表 */}
//                         {cartItems.map(item => (
//                             <div className={styles.cartItemCard} key={item.movie.id}>
//                                 <img className={styles.img} src={item.movie.img} alt={item.movie.title} width={200} />
//                                 <div className={styles.textContent}>
//                                     <p>電影名稱: {item.movie.title}</p>
//                                     <p>放映日期: {item.showDate ? item.showDate : '未指定日期'}</p>
//                                     <p>放映時間: {item.showtime ? item.showtime : '未指定時間'}</p>
//                                     <p>{item.hall.hall_type} {item.hall.hall_number}廳</p>
//                                     <p>價格: {item.hall.price}</p>
//                                     <p>數量: {item.quantity}</p>
//                                     <p>座位: {item.seatNumbers.join(', ')}</p>
//                                 </div>
//                                 <div className={styles.deleteButtonContainer}>
//                                     <button
//                                         className={styles.deleteButton}
//                                         onClick={() => removeCartItem(item.movie.id)} // 調用 removeCartItem 函數
//                                     >
//                                         刪除
//                                     </button>
//                                 </div>
//                             </div>
//                         ))}
//                     </div>

//                     <div id={styles.checkoutSection}>
//                         <div>總價一共：{grandTotal}元</div>
//                         {grandTotal >= freeFood ? (
//                             <div>滿${freeFood}贈送免費爆米花</div>
//                         ) : (
//                             <div>
//                                 滿${freeFood}贈送免費爆米花<br />
//                                 還差${freeFood - grandTotal}
//                             </div>
//                         )}
//                         <button className={styles.checkoutButton} onClick={payment}>結帳</button>
//                     </div>
//                 </div>
//             )}
//         </div>
//     );
// }


import React, { useContext, useState } from 'react';
import Titles from './Titles'
import { Link, useNavigate } from 'react-router-dom';
import { CartContext } from '../CartContext';
import styles from '../styles/Checkout.module.css'; // 引入 CSS 模組
import { API_BASE_URL } from '../apiConfig';

export default function CheckOutIn() {
    const { cartItems, removeCartItem, clearCart } = useContext(CartContext);
    const navigate = useNavigate();
    const cartEmpty = cartItems.length <= 0;
    const grandTotal = cartItems.reduce((total, item) => {
        return total + (item.hall.price * item.quantity);
    }, 0);
    const freeFood = 350;
    const orderNumber = `ORD-${new Date().getTime()}`;

    // 使用 useState 管理 savedOrderId 和顯示 LinePay 按鈕的狀態
    const [savedOrderId, setSavedOrderId] = useState(null);
    const [showLinePayButton, setShowLinePayButton] = useState(false);

    // 建立一筆「未付款」的訂單，回傳真正的訂單編號。
    // LinePay 跟 Stripe 都要用，抽出來共用，不要各寫一次。
    const createPendingOrder = async (items = cartItems) => {
        const totalAmount = items.reduce((total, item) => {
            return total + item.hall.price * item.quantity;
        }, 0);

        const response = await fetch(`${API_BASE_URL}/api/orders/create-pending`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                userId: parseInt(localStorage.getItem('userid'), 10),
                amount: totalAmount,
                description: items.map(item => item.seatNumbers.join(', ')).join('; '),
                itemName: items.map(item => item.movie.title).join('; '),
            }),
        });
        const pendingOrder = await response.json();
        return pendingOrder.orderNumber;
    };

    const LinePayHandleCheckout = async (items = cartItems) => {
        const token = localStorage.getItem('token');
        if (!token) {
            alert('請先登入!');
            navigate('/login');
            return;
        }

        const realOrderNumber = await createPendingOrder(items);

        const totalAmount = items.reduce((total, item) => {
            return total + item.hall.price * item.quantity;
        }, 0);

        const packages = items.map(item => ({
            name: item.movie.title,
            amount: item.hall.price * item.quantity,
            products: item.seatNumbers.map(seat => ({
                name: seat,
                quantity: item.quantity,
                price: item.hall.price
            }))
        }));

        const checkoutRequest = {
            amount: totalAmount,
            orderId: String(realOrderNumber),
            currency: 'TWD',
            confirmUrl: "https://www.google.com.tw",
            packages: packages
        };

        fetch(`${API_BASE_URL}/checkout/save`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(checkoutRequest),
        })
            .then(response => response.json())
            .then(data => {
                console.log('訂單保存成功:', data);
                return fetch(`${API_BASE_URL}/checkout/details/${realOrderNumber}`, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                    }
                });
            })
            .then(response => response.json())
            .then(detailData => {
                console.log('取得結帳細節:', detailData);
                return fetch(`${API_BASE_URL}/checkout/payment`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(detailData),
                });
            })
            .then(response => response.json())
            .then(paymentData => {
                console.log('LinePay 付款處理結果:', paymentData);
                const responseInfo = JSON.parse(paymentData.response);

                if (responseInfo.info && responseInfo.info.paymentUrl && responseInfo.info.paymentUrl.web) {
                    // 只移除這次真正拿去結帳的品項，不是清空整個購物車
                    items.forEach(item => removeCartItem(item.movie.id));
                    window.location.href = responseInfo.info.paymentUrl.web;
                } else {
                    alert('付款失敗');
                }
            })
            .catch(error => {
                console.error('錯誤:', error);
            });
    };

    const StripeHandleCheckout = async (items = cartItems) => {
        const token = localStorage.getItem('token');
        if (!token) {
            alert('請先登入!');
            navigate('/login');
            return;
        }

        const realOrderNumber = await createPendingOrder(items);

        const lineItems = items.map(item => ({
            name: `${item.movie.title}（${item.seatNumbers.join(', ')}）`,
            unitAmount: Math.round(item.hall.price * 100),
            quantity: item.quantity,
        }));

        const requestBody = {
            userId: parseInt(localStorage.getItem('userid'), 10),
            orderNumber: realOrderNumber,
            description: items.map(item => item.seatNumbers.join(', ')).join('; '),
            itemName: items.map(item => item.movie.title).join('; '),
            items: lineItems,
        };

        fetch(`${API_BASE_URL}/api/stripe/create-checkout-session`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        })
            .then(response => response.json())
            .then(data => {
                if (data.url) {
                    items.forEach(item => removeCartItem(item.movie.id));
                    window.location.href = data.url;
                } else {
                    alert('無法建立 Stripe 付款，請稍後再試');
                }
            })
            .catch(error => console.error('Stripe 付款錯誤:', error));
    };

    return (
        <div className={styles.checkoutPageWrapper}>
            <Titles mainTitle={"您的購物車"} />

            {
                cartEmpty &&
                <div className={styles.checkoutEmptyCartMessage}>
                    <Link to="/">
                        <a>購物車為空</a><br />
                        <a>前往購票吧</a>
                    </Link>
                </div>
            }

            {
                !cartEmpty &&
                <div className={styles.checkoutCartContainer}>
                    <div id={styles.checkoutCartSection}>
                        {/* 產品列表 */}
                        {cartItems.map(item => (
                            <div className={styles.checkoutCartItemCard} key={item.cartItemId}>
                                <img className={styles.checkoutImg} src={process.env.PUBLIC_URL + "/image/" + item.movie.img} alt={item.movie.title} width={200} />
                                <div className={styles.checkoutTextContent}>
                                    <p>電影名稱: {item.movie.title}</p>
                                    <p>放映日期: {item.showDate ? item.showDate : '未指定日期'}</p>
                                    <p>放映時間: {item.showtime ? item.showtime : '未指定時間'}</p>
                                    <p>{item.hall.hall_type} {item.hall.hall_number}廳</p>
                                    <p>價格: {item.hall.price}</p>
                                    <p>數量: {item.quantity}</p>
                                    <p>座位: {item.seatNumbers.join(', ')}</p>
                                </div>
                                <div>
                                    <button onClick={() => removeCartItem(item.cartItemId)}>移除</button>
                                    <button onClick={() => LinePayHandleCheckout([item])}>單獨用 LinePay 結帳</button>
                                    <button onClick={() => StripeHandleCheckout([item])}>單獨用 Stripe 結帳</button>
                                </div>
                            </div>
                        ))}
                    </div>

                    <div id={styles.checkoutCheckoutSection}>
                        {/* 價錢總數 */}
                        <div>總價一共：{grandTotal}元</div>
                        {
                            /* 免費送爆米花 */
                            grandTotal >= freeFood ?
                                <div>滿${freeFood}贈送免費爆米花</div> :
                                <div>
                                    滿${freeFood}贈送免費爆米花<br />
                                    還差${freeFood - grandTotal}</div>
                        }
                        <button className={styles.checkoutLinePaycheckoutButton} onClick={LinePayHandleCheckout}>LinePay結帳</button>
                        <button className={styles.checkoutLinePaycheckoutButton} onClick={StripeHandleCheckout}>使用信用卡付款（Stripe）</button>
                    </div>
                </div>
            }
        </div>
    );

}