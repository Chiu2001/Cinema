import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { GoogleOAuthProvider } from '@react-oauth/google';
import HomePage from './pages/HomePage';
import HomePageIn from './pages/HomePageIn';
import AboutPage from './pages/AboutPage';
import AboutPageIn from './pages/AboutPageIn';
import OrderListPage from './pages/OrderListPage';
import OrderListPageIn from './pages/OrderListPageIn';
import UserListPage from './pages/UserListPage';
import UserListPageIn from './pages/UserListPageIn';
import SignupPage from './pages/SignupPage';
import LoginFormPage from './pages/LoginFormPage';
import ScheduleManagementPage from './pages/ScheduleManagementPage';
import UserManagementPage from './pages/UserManagementPage';
import OrderManagementPage from './pages/OrderManagementPage';
import NewsManagementPage from './pages/NewsManagementPage';
import MovieDetailPage from './pages/MovieDetailPage';
import MovieDetailPageIn from './pages/MovieDetailPageIn';
import MovieListPage from './pages/MovieListPage';
import MovieListPageIn from './pages/MovieListPageIn';
import CheckOutPage from './pages/CheckOutPage';
import CheckOutPageIn from './pages/CheckOutPageIn';
import Area from './components/Area';
import PrivateRoute from './components/PrivateRoute';
import EcpayPage from './pages/EcpayPage';
import PaymentResultPage from './pages/PaymentResultPage';
import { CartContext } from './CartContext';
import { API_BASE_URL } from './apiConfig';

const clientId = '817410459835-mgi4raiakq80l828g3nd2vhn791urcdd.apps.googleusercontent.com';

function App() {
    // On first load, try to restore the cart contents previously saved in localStorage
    const [cartItems, setCartItems] = useState(() => {
        try {
            const stored = localStorage.getItem('cartItems');
            return stored ? JSON.parse(stored) : [];
        } catch (e) {
            return [];
        }
    });

    // Whenever the cart contents change, sync them back to localStorage
    useEffect(() => {
        localStorage.setItem('cartItems', JSON.stringify(cartItems));
    }, [cartItems]);

    // releaseSeats defaults to true (the user is abandoning these seats), but
    // is passed false when this is called right before redirecting to Stripe
    // — the seats must stay held through the actual payment, not be freed
    // the moment checkout starts.
    const removeCartItem = (cartItemId, releaseSeats = true) => {
        console.log('Attempting to remove cartItemId:', cartItemId);
        console.log('Current cart contents:', cartItems);

        // Release the seats this item held back to available, so other users
        // can select them again. Best-effort: the item still leaves the cart
        // even if a release call fails (e.g. it was already booked/paid for).
        const removedItem = cartItems.find(item => item.cartItemId === cartItemId);
        if (removedItem && releaseSeats) {
            removedItem.seatNumbers.forEach(seatNumber => {
                fetch(`${API_BASE_URL}/api/movie/save/${removedItem.showtime_id}/${seatNumber}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ seatAvailability: true }),
                }).catch(error => console.error(`Failed to release seat ${seatNumber}:`, error));
            });
        }

        setCartItems(prevItems => prevItems.filter(item => item.cartItemId !== cartItemId));
    };

    const clearCart = () => {
        setCartItems([]);
    };

    return (
        <GoogleOAuthProvider clientId={clientId}>

            <Router>
                <CartContext.Provider value={{ cartItems, setCartItems, removeCartItem, clearCart }}>
                    <Routes>
                        <Route path="/home" element={<HomePage />} />
                        <Route path="/HomePageIn" element={<PrivateRoute element={<HomePageIn />} />} />
                        <Route path="/about" element={<AboutPage />} />
                        <Route path="/AboutPageIn" element={<PrivateRoute element={<AboutPageIn />} />} />
                        <Route path="/OrderList" element={<OrderListPage />} />
                        <Route path="/OrderListIn" element={<PrivateRoute element={<OrderListPageIn />} />} />
                        <Route path="/UserList" element={<UserListPage />} />
                        <Route path="/UserListIn" element={<PrivateRoute element={<UserListPageIn />} />} />
                        <Route path="/login" element={<LoginFormPage />} />
                        <Route path="/signup" element={<SignupPage />} />
                        <Route path="/schedule" element={<ScheduleManagementPage />} />
                        <Route path="/user_management" element={<UserManagementPage />} />
                        <Route path="/order" element={<OrderManagementPage />} />
                        <Route path="/news" element={<NewsManagementPage />} />
                        <Route path="/MovieList" element={<MovieListPage />} />
                        <Route path="/MovieListIn" element={<PrivateRoute element={<MovieListPageIn />} />} />
                        <Route path="/MovieDetail/:id" element={<MovieDetailPage />} />
                        <Route path="/MovieDetailIn/:id" element={<PrivateRoute element={<MovieDetailPageIn />} />} />
                        <Route path="/CheckOut" element={<CheckOutPage />} />
                        <Route path="/CheckOutIn" element={<PrivateRoute element={<CheckOutPageIn />} />} />
                        <Route path="/area/:showtime_id" element={<Area />} />
                        <Route path="/EcpayPage" element={<PrivateRoute element={<EcpayPage />} />} />
                        <Route path="/PaymentResultPage" element={<PrivateRoute element={<PaymentResultPage />} />} />
                        <Route path="*" element={<h1>Page Not Found</h1>} />
                    </Routes>
                </CartContext.Provider>
            </Router>
        </GoogleOAuthProvider>
    );
}

export default App;
