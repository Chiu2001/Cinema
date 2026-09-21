import React, { useContext, useState } from 'react';
import Titles from './Titles'
import { Link, useNavigate } from 'react-router-dom';
import { CartContext } from '../CartContext';
import styles from '../styles/Checkout.module.css'; // Import CSS module
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

    // Create an "unpaid" order and return its real order number.
    const createPendingOrder = async (items = cartItems) => {
        const totalAmount = items.reduce((total, item) => {
            return total + item.hall.price * item.quantity;
        }, 0);

        const response = await fetch(`${API_BASE_URL}/api/orders/create-pending`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
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

    const StripeHandleCheckout = async (items = cartItems) => {
        const token = localStorage.getItem('token');
        if (!token) {
            alert('Please log in first!');
            navigate('/login', { state: { from: { pathname: '/CheckOutIn' } } });
            return;
        }

        const realOrderNumber = await createPendingOrder(items);

        const lineItems = items.map(item => ({
            name: `${item.movie.title}（${item.seatNumbers.join(', ')}）`,
            unitAmount: Math.round(item.hall.price * 100),
            quantity: item.quantity,
            showtimeId: item.showtime_id,
            seatNumbers: item.seatNumbers,
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
                    // Don't release seats here either — the user is headed to Stripe to
                    // actually pay, and the seats must stay held through that.
                    items.forEach(item => removeCartItem(item.cartItemId, false));
                    window.location.href = data.url;
                } else {
                    alert('Unable to create Stripe payment, please try again later');
                }
            })
            .catch(error => console.error('Stripe payment error:', error));
    };

    return (
        <div className={styles.checkoutPageWrapper}>
            <Titles mainTitle={"Your Cart"} />

            {
                cartEmpty &&
                <div className={styles.checkoutEmptyCartMessage}>
                    <Link to="/">
                        <a>Your cart is empty</a><br />
                        <a>Go buy tickets</a>
                    </Link>
                </div>
            }

            {
                !cartEmpty &&
                <div className={styles.checkoutCartContainer}>
                    <div id={styles.checkoutCartSection}>
                        {/* Product list */}
                        {cartItems.map(item => (
                            <div className={styles.checkoutCartItemCard} key={item.cartItemId}>
                                <img className={styles.checkoutImg} src={item.movie.img} alt={item.movie.title} width={200} />
                                <div className={styles.checkoutTextContent}>
                                    <p>Movie Title: {item.movie.title}</p>
                                    <p>Showing Date: {item.showDate ? item.showDate : 'Date not specified'}</p>
                                    <p>Showtime: {item.showtime ? item.showtime : 'Time not specified'}</p>
                                    <p>{item.hall.hall_type} Hall {item.hall.hall_number}</p>
                                    <p>Price: {item.hall.price}</p>
                                    <p>Quantity: {item.quantity}</p>
                                    <p>Seats: {item.seatNumbers.join(', ')}</p>
                                </div>
                                <div className={styles.checkoutItemActions}>
                                    <button className={styles.deleteButton} onClick={() => removeCartItem(item.cartItemId)}>Remove</button>
                                    <button className={styles.checkoutPayButton} onClick={() => StripeHandleCheckout([item])}>Checkout</button>
                                </div>
                            </div>
                        ))}
                    </div>

                    <div id={styles.checkoutCheckoutSection}>
                        {/* Total price */}
                        <div>Grand Total: NT${grandTotal}</div>
                        {
                            /* Free popcorn */
                            grandTotal >= freeFood ?
                                <div>Spend ${freeFood} and get free popcorn</div> :
                                <div>
                                    Spend ${freeFood} and get free popcorn<br />
                                    ${freeFood - grandTotal} to go</div>
                        }
                        <button className={styles.checkoutPayButton} onClick={() => StripeHandleCheckout()}>Pay with Credit Card (Stripe)</button>
                    </div>
                </div>
            }
        </div>
    );

}