import React, { useContext } from 'react';
import { CartContext } from '../CartContext';
import { useNavigate } from 'react-router-dom';
import styles from '../styles/QuantityBtn.module.css'; // Import CSS module

export default function QuantityBtn({ showtimeInfo, selectedSeats }) {
    // Read the CartContext
    const { cartItems, setCartItems } = useContext(CartContext);

    const navigate = useNavigate();
    // Whether this showtime is already in the cart
    let showtimeIndexInCart = cartItems.findIndex((element) => {
        return element.showtime_id === showtimeInfo.showtime_id;  // Compare showtime_id
    });

    // Update the cart or proceed to checkout when the button is clicked
    const handleButtonClick = () => {
        console.log('Selected seats:', selectedSeats);
        console.log('Current cart contents:', cartItems);
        console.log('Showtime info:', showtimeInfo);
        if (selectedSeats.length > 0) {
            let mergedSeatNumbers = selectedSeats;

            if (showtimeIndexInCart !== -1) {
                // This showtime is already in the cart, so merge the newly selected seats in,
                // using a Set to dedupe and avoid counting the same seat twice.
                const existingSeats = cartItems[showtimeIndexInCart].seatNumbers;
                mergedSeatNumbers = [...new Set([...existingSeats, ...selectedSeats])];
            }

            const updatedCart = {
                cartItemId: showtimeIndexInCart !== -1
                    ? cartItems[showtimeIndexInCart].cartItemId
                    : crypto.randomUUID(),
                showtime_id: showtimeInfo.showtime_id,
                showtime: showtimeInfo.show_time,
                seatNumbers: mergedSeatNumbers,
                quantity: mergedSeatNumbers.length,
                showDate: showtimeInfo.showDate.show_date,
                hall: showtimeInfo.hall,
                movie: showtimeInfo.movie,
            };

            console.log('Updated cart item:', updatedCart);

            if (showtimeIndexInCart === -1) {
                const newCart = [...cartItems, updatedCart];
                setCartItems(newCart);
                console.log('Cart contents after adding item:', newCart);
            } else {
                const newCartArray = [...cartItems];
                newCartArray[showtimeIndexInCart] = updatedCart;
                setCartItems(newCartArray);
                console.log("Updated cart: ", newCartArray);
            }

            const totalAmount = calculateTotalAmount(mergedSeatNumbers);
            console.log('Total amount:', totalAmount);
            alert(`Seats confirmed! The seats for this showtime are: ${mergedSeatNumbers.join(', ')}\nTotal amount: NT$${totalAmount}`);

            const token = localStorage.getItem('token');
            if (token) {
                navigate('/CheckOutIn');
            } else {
                navigate('/CheckOut');
            }
        } else {
            alert('You have not selected any seats yet, please select again.');
        }
    };

    // Calculate the total amount
    const calculateTotalAmount = (seats) => {
        // Define the seat price lookup table, adjust as needed
        const seatPrices = {
            A: showtimeInfo.hall ? showtimeInfo.hall.price : 0,
            B: showtimeInfo.hall ? showtimeInfo.hall.price : 0,
            C: showtimeInfo.hall ? showtimeInfo.hall.price : 0,
            D: showtimeInfo.hall ? showtimeInfo.hall.price : 0,
            E: showtimeInfo.hall ? showtimeInfo.hall.price : 0
        };

        const totalAmount = seats.reduce((total, seatId) => {
            const section = seatId[0];
            return total + (seatPrices[section] || 0);
        }, 0);

        return totalAmount;
    };



    return (
        <div className={styles.quantityBtnContainer}>
            <button className={styles.button} onClick={handleButtonClick}>
                Add to Cart
            </button>
        </div>
    );
}
