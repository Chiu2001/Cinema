import React, { useContext } from 'react';
import { CartContext } from '../CartContext';
import { useNavigate } from 'react-router-dom';
import styles from '../styles/QuantityBtn.module.css'; // 導入 CSS 模組

export default function QuantityBtn({ showtimeInfo, selectedSeats }) {
    // 讀取 CartContext
    const { cartItems, setCartItems } = useContext(CartContext);

    const navigate = useNavigate();
    // 購物車內有無該場次
    let showtimeIndexInCart = cartItems.findIndex((element) => {
        return element.showtime_id === showtimeInfo.showtime_id;  // 比較 showtime_id
    });

    // 點擊按鈕時更新購物車或結帳
    const handleButtonClick = () => {
        console.log('已選擇的座位:', selectedSeats);
        console.log('當前購物車內容:', cartItems);
        console.log('場次信息:', showtimeInfo);
        if (selectedSeats.length > 0) {
            let mergedSeatNumbers = selectedSeats;

            if (showtimeIndexInCart !== -1) {
                // 同一場次已經在購物車裡，把這次選的座位「合併」進去，
                // 用 Set 去重，避免同一個座位被重複計算。
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

            console.log('更新後的購物車項目:', updatedCart);

            if (showtimeIndexInCart === -1) {
                const newCart = [...cartItems, updatedCart];
                setCartItems(newCart);
                console.log('加入購物車後的內容:', newCart);
            } else {
                const newCartArray = [...cartItems];
                newCartArray[showtimeIndexInCart] = updatedCart;
                setCartItems(newCartArray);
                console.log("更新購物車: ", newCartArray);
            }

            const totalAmount = calculateTotalAmount(mergedSeatNumbers);
            console.log('總金額:', totalAmount);
            alert(`座位確認成功！這個場次目前的座位為: ${mergedSeatNumbers.join(' 、 ')}\n總金額為: ${totalAmount} 元`);

            const token = localStorage.getItem('token');
            if (token) {
                navigate('/CheckOutIn');
            } else {
                navigate('/CheckOut');
            }
        } else {
            alert('您尚未選擇座位，請重新選擇。');
        }
    };

    // 計算總金額
    const calculateTotalAmount = (seats) => {
        // 定義座位價格對照表，根據實際情況修改
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
                加入購物車
            </button>
        </div>
    );
}
