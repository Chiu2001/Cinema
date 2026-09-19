// CartContext.js
//
// Only the context object lives here. The actual provider is App.js's own
// <CartContext.Provider> (with localStorage persistence), not a CartProvider
// component — an earlier, unused CartProvider export used to live here too.
import { createContext } from 'react';

export const CartContext = createContext();
