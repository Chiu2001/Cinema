import React from "react";
import { useNavigate } from 'react-router-dom';
import styles from '../styles/LoginForm.module.css';
import { FaUser, FaLock } from "react-icons/fa";
import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';
import { Link } from 'react-router-dom';
import { API_BASE_URL } from '../apiConfig';

const clientId = "817410459835-mgi4raiakq80l828g3nd2vhn791urcdd.apps.googleusercontent.com";

const LoginForm = () => {
    const navigate = useNavigate();

    const responseGoogle = async (credentialResponse) => {
        console.log('Google Login Success:', credentialResponse);
    
        if (credentialResponse && credentialResponse.credential) {
            try {
                const { credential } = credentialResponse;

                // No longer decoding the token client-side; send the credential straight to the backend
                const res = await fetch(`${API_BASE_URL}/api/movie/google-login`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ token: credential }),  // Send the credential returned by Google to the backend
                });

                const data = await res.json();
                console.log('Received data:', data); // Confirm the data received is in the expected JSON format

                if (res.ok) {
                    const { token, roles, name, email, id } = data;

                    // Store the token and user data returned by the backend
                    localStorage.setItem('token', token);
                    localStorage.setItem('roles', JSON.stringify(roles));
                    localStorage.setItem('name', name);
                    localStorage.setItem('email', email);
                    localStorage.setItem('userid', id);  // Store the id

                    console.log('Login Success: Token and roles stored');
                    navigate('/HomePageIn');
                } else {
                    console.error('Backend login failed:', data.error);
                    alert('Google login failed: ' + data.error);
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
            const response = await fetch(`${API_BASE_URL}/api/movie/login`, {
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
                localStorage.setItem('userid', id); // Store the id

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
                            <p>Don't have an account? <Link to="/signup">Register</Link></p>
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

