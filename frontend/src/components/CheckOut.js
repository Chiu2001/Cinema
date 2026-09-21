import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { CartContext } from '../CartContext';
import styles from '../styles/Checkout.module.css'; // Import CSS module

export default function CheckOut() {
    const { cartItems, removeCartItem } = useContext(CartContext);
    const navigate = useNavigate();
    const cartEmpty = cartItems.length <= 0;
    const grandTotal = cartItems.reduce((total, item) => {
        return total + (item.hall.price * item.quantity);
    }, 0);
    const freeFood = 350;

    // This page exists so a guest can preview their cart before logging in
    // (/CheckOutIn is behind PrivateRoute and would bounce them straight to
    // /login). It used to also create a pending order here and dump the user
    // on /OrderList without ever charging them — the real Stripe checkout
    // only exists on CheckOutIn, so hand off there instead of duplicating
    // (and getting wrong) that logic.
    const placeOrder = () => {
        const token = localStorage.getItem('token');

        if (!token) {
            alert('Please log in first!');
            // Send them straight back to the real checkout page once they log in,
            // instead of the home page.
            navigate('/login', { state: { from: { pathname: '/CheckOutIn' } } });
            return;
        }

        navigate('/CheckOutIn');
    };

    return (
        <div className={styles.pageWrapper}>
            <h1>Your Cart</h1>

            {cartEmpty ? (
                <div className={styles.emptyCartMessage}>
                    <Link to="/MovieList">
                        <a>Your cart is empty</a><br />
                        <a>Go buy tickets</a>
                    </Link>
                </div>
            ) : (
                <div className={styles.cartContainer}>
                    <div id={styles.cartSection}>
                        {/* Product list */}
                        {cartItems.map(item => (
                            <div className={styles.cartItemCard} key={item.cartItemId}>
                                <img className={styles.img} src={item.movie.img} alt={item.movie.title} width={200} />
                                <div className={styles.textContent}>
                                    <p>Movie Title: {item.movie.title}</p>
                                    <p>Showing Date: {item.showDate ? item.showDate : 'Date not specified'}</p>
                                    <p>Showtime: {item.showtime ? item.showtime : 'Time not specified'}</p>
                                    <p>{item.hall.hall_type} Hall {item.hall.hall_number}</p>
                                    <p>Price: {item.hall.price}</p>
                                    <p>Quantity: {item.quantity}</p>
                                    <p>Seats: {item.seatNumbers.join(', ')}</p>
                                </div>
                                <div className={styles.deleteButtonContainer}>
                                    <button
                                        className={styles.deleteButton}
                                        onClick={() => removeCartItem(item.cartItemId)}
                                    >
                                        Delete
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>

                    <div id={styles.checkoutSection}>
                        <div>Grand Total: NT${grandTotal}</div>
                        {grandTotal >= freeFood ? (
                            <div>Spend ${freeFood} and get free popcorn</div>
                        ) : (
                            <div>
                                Spend ${freeFood} and get free popcorn<br />
                                ${freeFood - grandTotal} to go
                            </div>
                        )}
                        <button className={styles.checkoutButton} onClick={placeOrder}>Checkout</button>
                    </div>
                </div>
            )}
        </div>
    );
}