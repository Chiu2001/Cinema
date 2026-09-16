import React from 'react';
import { Navigate } from 'react-router-dom';

const PrivateRoute = ({ element }) => {
    const token = localStorage.getItem('token'); // Assume the token is used to check login status

    return token ? element : <Navigate to="/" />;
};

export default PrivateRoute;