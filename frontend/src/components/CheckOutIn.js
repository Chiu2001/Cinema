// import React, { useContext } from 'react';
// import { Link, useNavigate } from 'react-router-dom';
// import { CartContext } from '../CartContext';
// import styles from '../styles/Checkout.module.css'; // Import CSS module

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


import React, { useContext, useState } from 'react';
import Titles from './Titles'
import { Link, useNavigate } from 'react-router-dom';
import { CartContext } from '../CartContext';
import styles from '../styles/Checkout.module.css'; // Import CSS module

export default function CheckOutIn() {
    const { cartItems, removeCartItem } = useContext(CartContext);
    const navigate = useNavigate();
    const cartEmpty = cartItems.length <= 0;
    const grandTotal = cartItems.reduce((total, item) => {
        return total + (item.hall.price * item.quantity);
    }, 0);
    const freeFood = 350;
    const orderNumber = `ORD-${new Date().getTime()}`;

    // Use useState to manage savedOrderId and whether the LinePay button is shown
    const [savedOrderId, setSavedOrderId] = useState(null);
    const [showLinePayButton, setShowLinePayButton] = useState(false);

    const handleCheckout = async () => {

        const token = localStorage.getItem('token');

        // Check whether the user is logged in
        if (!token) {
            alert('Please log in first!');
            navigate('/login'); // Redirect to the login page
            return; // Stop the checkout process
        }

        const totalAmount = cartItems.reduce((total, item) => {
            return total + item.hall.price * item.quantity;
        }, 0);

        const packages = cartItems.map(item => ({
            name: item.movie.title,  // Stored as the name field of productPackageForm
            amount: item.hall.price * item.quantity,  // Stored as the amount field of productPackageForm
            products: item.seatNumbers.map(seat => ({
                name: seat,  // Stored as the name field of productForm
                quantity: item.quantity,  // Stored as the quantity field of productForm
                price: item.hall.price  // Stored as the price field of productForm
            }))
        }));

        const checkoutRequest = {
            amount: totalAmount,  // Stored as the amount field of checkoutPaymentRequestForm
            orderId: orderNumber,  // Stored as the orderId field of checkoutPaymentRequestForm
            currency: 'TWD',  // Assumed to be New Taiwan Dollars
            confirmUrl: "https://www.google.com.tw", // Temporarily set to Google
            // confirmUrl: "http://localhost:3000/LinepayPaymentResult",
            packages: packages
        };

        // Send a POST request to the backend
        fetch('http://localhost:8443/movie/checkout/save', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(checkoutRequest),
        })
            .then(response => response.json())
            .then(data => {
                console.log('Order saved successfully:', data);
                setSavedOrderId(data.orderId);  // Update orderId via useState
                setShowLinePayButton(true);  // Show the LinePay button
                alert(`Order created, order number: ${data.orderId}`);
            })
            .catch((error) => {
                console.error('Error:', error);
            });
    };

    // Send data to LinePay
    const LinePayHandleCheckout = () => {
        if (!savedOrderId) {
            alert('Please confirm checkout first!');
            return;
        }

        // Step 1: get checkout details
        fetch(`http://localhost:8443/movie/checkout/details/${savedOrderId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        })
            .then(response => response.json())
            .then(data => {
                console.log('Retrieved checkout details:', data)

                // Step 2: send the retrieved data to LinePay for payment
                return fetch('http://localhost:8443/movie/checkout/payment', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(data),  // Send the retrieved checkout data to the payment API
                });
            })
            .then(response => response.json())
            .then(paymentData => {
                console.log('LinePay payment processing result:', paymentData);

                // Parse the response string into a JSON object
                const responseInfo = JSON.parse(paymentData.response);

                // Redirect or perform another action based on the parsed data
                if (responseInfo.info && responseInfo.info.paymentUrl && responseInfo.info.paymentUrl.web) {
                    window.location.href = responseInfo.info.paymentUrl.web;  // Redirect to the LinePay payment page
                } else {
                    alert('Payment failed');
                }
            })
            .catch(error => {
                console.error('Error:', error);
            });

    }

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
                            <div className={styles.checkoutCartItemCard} key={item.movie.movie_id}>
                                <img className={styles.checkoutImg} src={process.env.PUBLIC_URL + "/image/" + item.movie.img} alt={item.movie.title} width={200} />
                                <div className={styles.checkoutTextContent}>
                                    <p>Movie title: {item.movie.title}</p>
                                    {/* Show the date only after confirming showDate exists */}
                                    <p>Showing date: {item.showDate ? item.showDate : 'Not specified'}</p>
                                    <p>Showtime: {item.showtime ? item.showtime : 'Not specified'}</p> {/* Shown if the showtime needs to be displayed */}
                                    <p>{item.hall.hall_type} Hall {item.hall.hall_number}</p>
                                    <p>Price: {item.hall.price}</p>
                                    <p>Quantity: {item.quantity}</p>
                                    <p>Seats: {item.seatNumbers.join(', ')}</p> {/* Show seat numbers */}
                                </div>
                            </div>
                        ))}
                    </div>

                    <div id={styles.checkoutCheckoutSection}>
                        {/* Total price */}
                        <div>Grand total: {grandTotal}</div>
                        {
                            /* Free popcorn */
                            grandTotal >= freeFood ?
                                <div>Spend ${freeFood} to get free popcorn</div> :
                                <div>
                                    Spend ${freeFood} to get free popcorn<br />
                                    ${freeFood - grandTotal} to go</div>
                        }
                        <button className={styles.checkoutCheckoutButton} onClick={handleCheckout}>Confirm Checkout</button>

                        {/* Only show the LinePay button when showLinePayButton is true */}
                        {showLinePayButton && (
                            <button className={styles.checkoutLinePaycheckoutButton} onClick={LinePayHandleCheckout}>LinePay Checkout</button>
                        )}
                    </div>
                </div>
            }
        </div>
    );

}