import React from 'react';
import { Link } from 'react-router-dom';

//nav basica no me pegue 😭😭

const Navbar: React.FC = () => {
  return (
    <nav style={{ 
      padding: '1rem', 
      backgroundColor: '#2c3e50', 
      color: 'white', 
      display: 'flex', 
      gap: '1rem',
      justifyContent: 'center' 
    }}>
      <strong style={{ marginRight: '2rem' }}>SmartLogix</strong>
      <Link to="/inventario" style={{ color: 'white', textDecoration: 'none' }}>Inventario</Link>
      <Link to="/pedidos" style={{ color: 'white', textDecoration: 'none' }}>Pedidos</Link>
    </nav>
  );
};

export default Navbar;
