import React, { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

const EcpayPage = () => {
  const location = useLocation();
  const { ecpayHTML } = location.state || {}; // Receive the HTML form from the route state

  useEffect(() => {
    if (ecpayHTML) {
      console.log('Received ecpayHTML:', ecpayHTML); // Inspect the returned HTML
      const tempDiv = document.createElement('div');
      tempDiv.innerHTML = ecpayHTML; // Insert the HTML into the DOM
      document.body.appendChild(tempDiv);

      // Ensure the HTML is displayed immediately after insertion
      const form = tempDiv.querySelector('form'); // Find the form
      console.log('Form:', form); // Verify the form element
      if (form) {
        form.style.display = 'block'; // Ensure the form is visible
        form.submit(); // Automatically submit the form
      } else {
        console.error('Form not found in the ecpayHTML');
      }
    }
  }, [ecpayHTML]); // Triggered when ecpayHTML updates

  return (
    <div>
      {ecpayHTML ? (
        <p>Processing payment, please wait...</p>
      ) : (
        <p>Failed to load the payment page, please try again.</p>
      )}
    </div>
  );
};

export default EcpayPage;
