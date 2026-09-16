import React from 'react';
import { Route, Navigate } from 'react-router-dom';

const AdminRoute = ({ element: Component, ...rest }) => {
    const isAuthenticated = true; // Check the admin's login status here
    return (
        <Route
            {...rest}
            element={isAuthenticated ? <Component /> : <Navigate to="/login" />}
        />
    );
};

export default AdminRoute;
