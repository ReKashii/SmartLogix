import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Package, LogOut } from 'lucide-react';

const Navbar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  if (location.pathname === '/login') return null;

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <nav className="bg-slate-900 text-white px-6 py-4 flex items-center justify-between shadow-lg">
      <div className="flex items-center gap-2 group cursor-pointer" onClick={() => navigate('/dashboard')}>
        <Package className="w-6 h-6 text-blue-400 group-hover:text-blue-300 transition-colors" />
        <span className="text-xl font-bold tracking-tight">SmartLogix</span>
      </div>
      <div className="flex items-center gap-6">
        <div className="flex gap-6">
          <Link 
            to="/inventario" 
            className="text-slate-300 hover:text-white font-medium transition-colors duration-200"
          >
            Inventario
          </Link>
          <Link 
            to="/pedidos" 
            className="text-slate-300 hover:text-white font-medium transition-colors duration-200"
          >
            Pedidos
          </Link>
        </div>
        <div className="h-6 w-px bg-slate-700" />
        <button 
          onClick={handleLogout}
          className="flex items-center gap-2 text-slate-400 hover:text-red-400 font-medium transition-colors duration-200"
        >
          <LogOut className="w-4 h-4" />
          <span className="text-sm">Salir</span>
        </button>
      </div>
    </nav>
  );
};

export default Navbar;
