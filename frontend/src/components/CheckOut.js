// import React, { useContext } from 'react';
// import { Link, useNavigate } from 'react-router-dom';
// import { CartContext } from '../CartContext';
// import styles from '../styles/Checkout.module.css'; // Import CSS module

// export default function CheckOut() {
//     const { cartItems, removeCartItem } = useContext(CartContext);
//     const navigate = useNavigate();
//     const cartEmpty = cartItems.length <= 0;
//     const grandTotal = cartItems.reduce((total, item) => {
//         return total + (item.hall.price * item.quantity);
//     }, 0);
//     const freeFood = 350;

//     const payment = async () => {
//         try {
//             const response = await fetch('http://localhost:8443/movie/ecpay/checkout', {
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
//                 throw new Error('Network response error');
//             }

//             const formHtml = await response.text();
//             console.log('Received ECPay HTML:', formHtml); // Check the returned HTML
//             navigate('/ecpay', { state: { ecpayHTML: formHtml } });
//         } catch (error) {
//             console.error('Error during checkout:', error);
//         }
//     };

//     return (
//         <div className={styles.pageWrapper}>
//             <h1>Your Cart</h1>

//             {cartEmpty ? (
//                 <div className={styles.emptyCartMessage}>
//                     <Link to="/MovieList">
//                         <a>Your cart is empty</a><br />
//                         <a>Go buy tickets</a>
//                     </Link>
//                 </div>
//             ) : (
//                 <div className={styles.cartContainer}>
//                     <div id={styles.cartSection}>
//                         {/* Product list */}
//                         {cartItems.map(item => (
//                             <div className={styles.cartItemCard} key={item.movie.id}>
//                                 <img className={styles.img} src={item.movie.img} alt={item.movie.title} width={200} />
//                                 <div className={styles.textContent}>
//                                     <p>Movie title: {item.movie.title}</p>
//                                     <p>Showing date: {item.showDate ? item.showDate : 'Not specified'}</p>
//                                     <p>Showtime: {item.showtime ? item.showtime : 'Not specified'}</p>
//                                     <p>{item.hall.hall_type} Hall {item.hall.hall_number}</p>
//                                     <p>Price: {item.hall.price}</p>
//                                     <p>Quantity: {item.quantity}</p>
//                                     <p>Seats: {item.seatNumbers.join(', ')}</p>
//                                 </div>
//                                 <div className={styles.deleteButtonContainer}>
//                                     <button
//                                         className={styles.deleteButton}
//                                         onClick={() => removeCartItem(item.movie.id)} // Call removeCartItem function
//                                     >
//                                         Delete
//                                     </button>
//                                 </div>
//                             </div>
//                         ))}
//                     </div>

//                     <div id={styles.checkoutSection}>
//                         <div>Grand total: {grandTotal}</div>
//                         {grandTotal >= freeFood ? (
//                             <div>Spend ${freeFood} to get free popcorn</div>
//                         ) : (
//                             <div>
//                                 Spend ${freeFood} to get free popcorn<br />
//                                 ${freeFood - grandTotal} to go
//                             </div>
//                         )}
//                         <button className={styles.checkoutButton} onClick={payment}>Checkout</button>
//                     </div>
//                 </div>
//             )}
//         </div>
//     );
// }


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

    const placeOrder = async () => {
        const token = localStorage.getItem('token');

        // Check whether the user is logged in
        if (!token) {
            alert('Please log in first!');
            navigate('/login'); // Redirect to the login page
            return; // Stop the checkout process
        }
    
        try {
            const response = await fetch('http://localhost:8443/movie/api/orders/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId: localStorage.getItem('userid'),
                    amount: grandTotal,
                    description: cartItems.map(item => item.seatNumbers.join(', ')).join('; '),
                    itemName: cartItems.map(item => item.movie.title).join('; '),
                }),
            });
    
            if (!response.ok) {
                throw new Error('Network response error');
            }

            const orderDetails = await response.json();
            console.log('Order details:', orderDetails);

            // Navigate to the OrderList page after checkout completes
            navigate('/OrderList');
        } catch (error) {
            console.error('Error during checkout:', error);
        }
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
                            <div className={styles.cartItemCard} key={item.movie.id}>
                                <img className={styles.img} src={item.movie.img} alt={item.movie.title} width={200} />
                                <div className={styles.textContent}>
                                    <p>Movie title: {item.movie.title}</p>
                                    <p>Showing date: {item.showDate ? item.showDate : 'Not specified'}</p>
                                    <p>Showtime: {item.showtime ? item.showtime : 'Not specified'}</p>
                                    <p>{item.hall.hall_type} Hall {item.hall.hall_number}</p>
                                    <p>Price: {item.hall.price}</p>
                                    <p>Quantity: {item.quantity}</p>
                                    <p>Seats: {item.seatNumbers.join(', ')}</p>
                                </div>
                                {/* <div className={styles.deleteButtonContainer}>
                                    <button
                                        className={styles.deleteButton}
                                        onClick={() => removeCartItem(item.movie.id)} // Call removeCartItem function
                                    >
                                        Delete
                                    </button>
                                </div> */}
                            </div>
                        ))}
                    </div>

                    <div id={styles.checkoutSection}>
                        <div>Grand total: {grandTotal}</div>
                        {grandTotal >= freeFood ? (
                            <div>Spend ${freeFood} to get free popcorn</div>
                        ) : (
                            <div>
                                Spend ${freeFood} to get free popcorn<br />
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