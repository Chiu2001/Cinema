import React, { useState } from 'react';
import styles from '../styles/Signup.module.css'; // Import your CSS file using namespacing

const RegisterForm = () => {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        birthDate: '',
        gender: '',
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        // Check that the password and confirm password match
        if (formData.password !== formData.confirmPassword) {
            alert('Password and confirm password do not match');
            return;
        }

        fetch('http://localhost:8443/movie/api/movie/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                alert(data.error);
            } else {
                alert('Registration complete');
                setFormData({
                    username: '',
                    email: '',
                    password: '',
                    confirmPassword: '',
                    birthDate: '', 
                    gender: '',
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Registration failed');
        });
    };

    return (
        <div className={styles.body}> {/* Use CSS module namespacing */}
            <div className={styles.formContainer}> {/* Use CSS module namespacing */}
                <h2>Create Account</h2>
                <form onSubmit={handleSubmit}>
                    <input
                        type="text"
                        name="username"
                        placeholder="Username"
                        value={formData.username}
                        onChange={handleChange}
                        required
                    />
                    <input
                        type="email"
                        name="email"
                        placeholder="Email"
                        value={formData.email}
                        onChange={handleChange}
                        required
                    />
                    <input
                        type="password"
                        name="password"
                        placeholder="Password"
                        value={formData.password}
                        onChange={handleChange}
                        required
                    />
                    <input
                        type="password"
                        name="confirmPassword"
                        placeholder="Confirm Password"
                        value={formData.confirmPassword}
                        onChange={handleChange}
                        required
                    />
                    <input
                        type="date"
                        name="birthDate"
                        placeholder="Date of Birth"
                        value={formData.birthDate}
                        onChange={handleChange}
                        required
                    />
                    <select
                        name="gender"
                        value={formData.gender}
                        onChange={handleChange}
                        required
                    >
                        <option value="" disabled>Gender</option>
                        <option value="Male">Male</option>
                        <option value="Female">Female</option>
                        <option value="Other">Other</option>
                    </select>

                    <button type="submit">Sign Up</button>
                </form>
            </div>
        </div>
    );
};

export default RegisterForm;

