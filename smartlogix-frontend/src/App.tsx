import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Pedidos from './pages/Pedidos';
import Inventario from './pages/Inventario';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Envios from './pages/Envios';
import CustomerTracking from './pages/CustomerTracking';

const App: React.FC = () => {
  return (
    <Router>
      <div className="flex min-h-screen bg-slate-950 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-indigo-900/20 via-slate-950 to-slate-950 text-slate-300">
        <Sidebar />
        <main className="flex-1 w-full overflow-y-auto">
          <Routes>
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/login" element={<Login />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/inventario" element={<Inventario />} />
            <Route path="/pedidos" element={<Pedidos />} />
            <Route path="/envios" element={<Envios />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
};

export default App;
