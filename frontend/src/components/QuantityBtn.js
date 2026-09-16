import React, { useContext } from 'react';
import { CartContext } from '../CartContext';
import { useNavigate } from 'react-router-dom';
import styles from '../styles/QuantityBtn.module.css'; // Import CSS module

export default function QuantityBtn({ showtimeInfo, selectedSeats }) {
    // Read CartContext
    const { cartItems, setCartItems } = useContext(CartContext);

    const navigate = useNavigate();
    // Whether this showtime is already in the cart
    let showtimeIndexInCart = cartItems.findIndex((element) => {
        return element.showtime_id === showtimeInfo.showtime_id;  // Compare showtime_id
    });

    // Update the cart or check out when the button is clicked
    const handleButtonClick = () => {
        console.log('Selected seats:', selectedSeats);
        console.log('Current cart contents:', cartItems);
        console.log('Showtime info:', showtimeInfo);
        if (selectedSeats.length > 0) {
            const updatedCart = {
                showtime: showtimeInfo.show_time,
                seatNumbers: selectedSeats,
                quantity: selectedSeats.length,
                showDate: showtimeInfo.showDate.show_date,
                hall: showtimeInfo.hall,
                movie: showtimeInfo.movie,
            };

            console.log('Updated cart item:', updatedCart); // Log the item about to be added to the cart

            if (showtimeIndexInCart === -1) {
                // If this showtime isn't in the cart yet, add it
                const newCart = [...cartItems, updatedCart];
                setCartItems(newCart);
                console.log('Cart contents after adding:', newCart); // Log the new cart contents
            } else {
                // If this showtime is already in the cart, update the quantity and seat info
                const newCartArray = [...cartItems];
                newCartArray[showtimeIndexInCart] = updatedCart;
                setCartItems(newCartArray);
                console.log("Updated cart: ", newCartArray);
            }

            // Calculate the total amount and show a success message
            const totalAmount = calculateTotalAmount();
            console.log('Total amount:', totalAmount);  // Log the total amount
            alert(`Seat selection confirmed! Your seats: ${selectedSeats.join(', ')}\nTotal amount: ${totalAmount}`);

            // Decide the navigation path based on whether a token exists in localStorage
            const token = localStorage.getItem('token');
            if (token) {
                navigate('/CheckOutIn');
            } else {
                navigate('/CheckOut');
            }
        } else {
            alert('You have not selected any seats yet. Please select again.');
        }
    };

    // Calculate the total amount
    const calculateTotalAmount = () => {
        // Define the seat price lookup table, adjust as needed
        const seatPrices = {
            A: showtimeInfo.hall ? showtimeInfo.hall.price : 0,
            B: showtimeInfo.hall ? showtimeInfo.hall.price : 0,
            C: showtimeInfo.hall ? showtimeInfo.hall.price : 0,
            D: showtimeInfo.hall ? showtimeInfo.hall.price : 0,
            E: showtimeInfo.hall ? showtimeInfo.hall.price : 0
        };

        const totalAmount = selectedSeats.reduce((total, seatId) => {
            const section = seatId[0]; // The first letter of the seat ID represents the section
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
