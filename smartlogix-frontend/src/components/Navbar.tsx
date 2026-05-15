import React from 'react';
import { Link } from 'react-router-dom';
import { Package } from 'lucide-react';

const Navbar: React.FC = () => {
  return (
    <nav className="bg-slate-900 text-white px-6 py-4 flex items-center justify-between shadow-lg">
      <div className="flex items-center gap-2 group cursor-pointer">
        <Package className="w-6 h-6 text-blue-400 group-hover:text-blue-300 transition-colors" />
        <span className="text-xl font-bold tracking-tight">SmartLogix</span>
      </div>
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
    </nav>
  );
};

export default Navbar;
