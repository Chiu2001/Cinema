// import React from 'react';
// import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';
// import jwt_decode from 'jwt-decode';  // Used to decode the JWT token returned by Google
// import { useNavigate } from 'react-router-dom';  // Import useNavigate for navigation

// const clientId = process.env.REACT_APP_GOOGLE_CLIENT_ID;  // Use an environment variable

// const GoogleLoginComponent = () => {
//     const navigate = useNavigate();  // Use useNavigate for navigation

//     const handleLoginSuccess = async (credentialResponse) => {
//         console.log('Google Login Success:', credentialResponse);

//         // Check whether a credential was received
//         if (credentialResponse && credentialResponse.credential) {
//             try {
//                 // Get the JWT token from the credentialResponse returned by Google
//                 const { credential } = credentialResponse;

//                 // Use jwt_decode to decode the JWT token and get the user info
//                 const decodedToken = jwt_decode(credential);
//                 console.log('Decoded Token:', decodedToken);

//                 const { name, email } = decodedToken;  // Extract user info from the token

//                 // Send the token and user info to the backend for verification
//                 const res = await fetch('https://localhost:8443/movie/api/movie/google-login', {
//                     method: 'POST',
//                     headers: {
//                         'Content-Type': 'application/json',
//                     },
//                     body: JSON.stringify({ token: credential }),  // Send the credential returned by Google to the backend
//                 });

//                 const data = await res.json();

//                 if (res.ok) {
//                     // Assume the backend returns a JWT token and user info, store them in localStorage
//                     const { token, roles } = data;
//                     localStorage.setItem('token', token);  // Save the JWT token returned by the backend
//                     localStorage.setItem('roles', JSON.stringify(roles));  // Save roles
//                     localStorage.setItem('name', name);  // Save the user's name
//                     localStorage.setItem('email', email);  // Save the user's email

//                     console.log('Login Success: Token and roles stored');

//                     // Redirect to the home page on success
//                     navigate('/home');
//                 } else {
//                     console.error('Backend login failed:', data.message);
//                     alert('Google login failed: ' + data.message);
//                 }
//             } catch (error) {
//                 console.error('Error during Google login:', error);
//                 alert('Error during Google login: ' + error.message);
//             }
//         } else {
//             console.error('No credential found in response');
//             alert('Login failed, please try again.');
//         }
//     };

//     const handleLoginFailure = (error) => {
//         console.log('Google Login Failed:', error);
//         alert('Google login failed, please try again.');
//     };

//     return (
//         <GoogleOAuthProvider clientId={clientId}>
//             <div className="App">
//                 <GoogleLogin
//                     onSuccess={handleLoginSuccess}
//                     onError={handleLoginFailure}
//                 />
//             </div>
//         </GoogleOAuthProvider>
//     );
// };

// export default GoogleLoginComponent;

