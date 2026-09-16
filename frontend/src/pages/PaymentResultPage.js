import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';

const PaymentResultPage = () => {
  const [merchantTradeNo, setMerchantTradeNo] = useState('');
  const [merchantTradeDate, setMerchantTradeDate] = useState('');

  const location = useLocation();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    setMerchantTradeNo(params.get('MerchantTradeNo') || '');
    setMerchantTradeDate(params.get('MerchantTradeDate') || '');
  }, [location]);

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <h1>Payment Result</h1>
      <div>
        <p><strong>Transaction Number (MerchantTradeNo):</strong> {merchantTradeNo}</p>
        <p><strong>Transaction Date (MerchantTradeDate):</strong> {merchantTradeDate}</p>
      </div>
    </div>
  );
};

export default PaymentResultPage;
