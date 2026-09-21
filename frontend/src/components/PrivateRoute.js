import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';

const PrivateRoute = ({ element }) => {
    const token = localStorage.getItem('token'); // Assume the token is used to check login status
    const location = useLocation();

    // Remember where the user was headed, so LoginForm can send them back
    // here instead of always landing on the home page after logging in.
    return token ? element : <Navigate to="/login" state={{ from: location }} />;
};

export default PrivateRoute;