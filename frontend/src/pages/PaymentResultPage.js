import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';

const PaymentResultPage = () => {
  const [merchantTradeNo, setMerchantTradeNo] = useState('');
  const [merchantTradeDate, setMerchantTradeDate] = useState('');
  const [status, setStatus] = useState('');

  const location = useLocation();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    setMerchantTradeNo(params.get('MerchantTradeNo') || '');
    setMerchantTradeDate(params.get('MerchantTradeDate') || '');
    // Only the LINE Pay confirm redirect sends this; ECPay's redirect doesn't.
    setStatus(params.get('status') || '');
  }, [location]);

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <h1>Payment Result</h1>
      <div>
        {status && (
          <p><strong>Status:</strong> {status === 'success' ? 'Payment successful' : 'Payment failed'}</p>
        )}
        <p><strong>Order Number:</strong> {merchantTradeNo}</p>
        {merchantTradeDate && <p><strong>Transaction Date:</strong> {merchantTradeDate}</p>}
      </div>
    </div>
  );
};

export default PaymentResultPage;
