import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Pedidos from './pages/Pedidos';
import Inventario from './pages/Inventario';

const App: React.FC = () => {
  return (
    <Router>
      <Navbar />
      <Routes>
        <Route path="/pedidos" element={<Pedidos />} />
        <Route path="/inventario" element={<Inventario />} />
        <Route path="/" element={<div style={{ padding: '2rem' }}><h1>Welcome to SmartLogix</h1><p>Use the navbar to navigate.</p></div>} />
      </Routes>
    </Router>
  );
};

export default App;
