import React from "react";
import { useNavigate } from 'react-router-dom';
import styles from '../styles/LoginForm.module.css';
import { FaUser, FaLock } from "react-icons/fa";
import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';
import { Link } from 'react-router-dom';

const clientId = "210567720043-255mub33p83168jpgsoh5icunnppn1nu.apps.googleusercontent.com";

const LoginForm = () => {
    const navigate = useNavigate();

    const responseGoogle = async (credentialResponse) => {
        console.log('Google Login Success:', credentialResponse);
    
        if (credentialResponse && credentialResponse.credential) {
            try {
                const { credential } = credentialResponse;
    
                // No longer decoding the token; send the credential directly to the backend
                const res = await fetch('http://localhost:8443/movie/api/movie/google-login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ token: credential }),  // Send the credential returned by Google to the backend
                });

                const data = await res.json();
                console.log('Received data:', data); // Confirm the received data is in the expected JSON format

                if (res.ok) {
                    const { token, roles, name, email, id } = data;

                    // Save the token and user data returned by the backend
                    localStorage.setItem('token', token);
                    localStorage.setItem('roles', JSON.stringify(roles));
                    localStorage.setItem('name', name);
                    localStorage.setItem('email', email);
                    localStorage.setItem('userid', id);  // Save id

                    console.log('Login Success: Token and roles stored');
                    navigate('/HomePageIn');
                } else {
                    console.error('Backend login failed:', data.message);
                    alert('Google login failed: ' + data.message);
                }
            } catch (error) {
                console.error('Error during Google login:', error);
                alert('Error during Google login: ' + error.message);
            }
        } else {
            console.error('No credential found in response');
            alert('Login failed, please try again.');
        }
    };

    const responseGoogleError = (error) => {
        console.error('Login Failed:', error);
        alert('Google login failed, please try again.');
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        const email = document.getElementById('username').value;
        const password = document.getElementById('password').value;

        try {
            const response = await fetch('http://localhost:8443/movie/api/movie/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });

            const data = await response.json();

            if (response.ok) {
                const { token, roles, id } = data;

                localStorage.setItem('token', token);
                localStorage.setItem('email', email);
                localStorage.setItem('roles', JSON.stringify(roles));
                localStorage.setItem('userid', id); // Save id

                navigate('/HomePageIn');
            } else {
                alert('Login failed: ' + data.message);
            }
        } catch (error) {
            alert('Error during login: ' + error.message);
        }
    };

    return (
        <GoogleOAuthProvider clientId={clientId}>
            <div className={styles.container}>
                <div className={styles.wrapper}>
                    <form onSubmit={handleSubmit}>
                        <h1>Login</h1>
                        <div className={styles.inputBox}>
                            <input type="email" name="email" placeholder="Email" id="username" required />
                            <FaUser className={styles.icon} />
                        </div>
                        <div className={styles.inputBox}>
                            <input type="password" name="password" placeholder="Password" id="password" required />
                            <FaLock className={styles.icon} />
                        </div>
                        <button type="submit">Login</button>
                        <div className={styles.registerLink}>
                            <p>Don't have an account? <Link to="/signup" style={{ color: 'blue' }}>Register</Link></p>
                        </div>
                        <div className="google-login">
                            <GoogleLogin
                                onSuccess={responseGoogle}
                                onError={responseGoogleError}
                            />
                        </div>
                    </form>
                </div>
            </div>
        </GoogleOAuthProvider>
    );
};

export default LoginForm;

