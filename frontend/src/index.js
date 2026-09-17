import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';  // Main app component
import reportWebVitals from './reportWebVitals';  // Measure performance
import './styles/theme.css';  // Shared design tokens (color, type, spacing)
import './styles/styles.css';

// Render the app to the DOM root element
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

// Optionally measure performance
reportWebVitals();