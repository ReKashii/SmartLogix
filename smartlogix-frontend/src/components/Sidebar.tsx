import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Package, ShoppingCart, Truck, LayoutDashboard, LogOut } from 'lucide-react';

const Sidebar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  if (location.pathname === '/login') return null;

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  const navItems = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'Inventario', path: '/inventario', icon: Package },
    { name: 'Pedidos', path: '/pedidos', icon: ShoppingCart },
    { name: 'Envíos', path: '/envios', icon: Truck },
  ];

  return (
    <aside className="w-64 min-h-screen bg-slate-900/80 backdrop-blur-xl border-r border-white/10 flex flex-col transition-all">
      <div 
        className="px-6 py-8 flex items-center gap-3 cursor-pointer group" 
        onClick={() => navigate('/dashboard')}
      >
        <div className="p-2 bg-indigo-500/20 rounded-xl group-hover:bg-indigo-500/30 transition-colors">
          <Package className="w-8 h-8 text-indigo-400" />
        </div>
        <div>
          <h1 className="text-xl font-bold text-white tracking-tight">SmartLogix</h1>
          <p className="text-xs text-indigo-300 font-medium">Enterprise</p>
        </div>
      </div>

      <div className="flex-1 px-4 py-4 space-y-2">
        <p className="px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider mb-4">Menú Principal</p>
        {navItems.map((item) => {
          const isActive = location.pathname === item.path;
          return (
            <Link
              key={item.path}
              to={item.path}
              className={`flex items-center gap-3 px-4 py-3 rounded-xl font-medium transition-all duration-200 ${
                isActive
                  ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-500/30'
                  : 'text-slate-400 hover:bg-white/5 hover:text-white'
              }`}
            >
              <item.icon className={`w-5 h-5 ${isActive ? 'text-white' : 'text-slate-400'}`} />
              {item.name}
            </Link>
          );
        })}
      </div>

      <div className="p-4 border-t border-white/10">
        <button 
          onClick={handleLogout}
          className="flex items-center gap-3 w-full px-4 py-3 rounded-xl text-slate-400 hover:bg-red-500/10 hover:text-red-400 font-medium transition-colors duration-200"
        >
          <LogOut className="w-5 h-5" />
          <span>Cerrar Sesión</span>
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
