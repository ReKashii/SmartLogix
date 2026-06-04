import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Package, ShoppingCart, Activity, Truck, ArrowRight, Loader2, BarChart3, TrendingUp } from 'lucide-react';
import api from '../api/axiosConfig';

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [totalProductos, setTotalProductos] = useState(0);
  const [pedidosActivos, setPedidosActivos] = useState(0);
  const [enviosPendientes, setEnviosPendientes] = useState(0);
  const [systemStatus, setSystemStatus] = useState('Operacional');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      setLoading(true);
      let isOk = true;

      try {
        const invRes = await api.get('/inventario');
        setTotalProductos(invRes.data.length);
      } catch (err) {
        console.error('Error fetching inventory', err);
        isOk = false;
      }

      try {
        const pedRes = await api.get('/pedidos');
        setPedidosActivos(pedRes.data.length);
      } catch (err) {
        console.error('Error fetching pedidos', err);
        isOk = false;
      }

      try {
        const envRes = await api.get('/envios');
        const pendientes = envRes.data.filter((e: any) => e.estadoEnvio === 'PENDING').length;
        setEnviosPendientes(pendientes);
      } catch (err) {
        console.error('Error fetching envios', err);
      }

      setSystemStatus(isOk ? 'Operacional' : 'Intermitente');
      setLoading(false);
    };

    fetchStats();
  }, []);

  const stats = [
    { 
      label: 'Productos Stock', 
      value: loading ? null : totalProductos.toLocaleString(), 
      icon: Package, 
      color: 'text-indigo-400',
      bgColor: 'bg-indigo-500/10',
      borderColor: 'border-indigo-500/20'
    },
    { 
      label: 'Pedidos Activos', 
      value: loading ? null : pedidosActivos.toLocaleString(), 
      icon: ShoppingCart, 
      color: 'text-emerald-400',
      bgColor: 'bg-emerald-500/10',
      borderColor: 'border-emerald-500/20'
    },
    { 
      label: 'Envíos Pendientes', 
      value: loading ? null : enviosPendientes.toLocaleString(), 
      icon: Truck, 
      color: 'text-amber-400',
      bgColor: 'bg-amber-500/10',
      borderColor: 'border-amber-500/20'
    },
    { 
      label: 'Estado Sistema', 
      value: loading ? null : systemStatus, 
      icon: Activity, 
      color: systemStatus === 'Operacional' ? 'text-green-400' : 'text-red-400',
      bgColor: systemStatus === 'Operacional' ? 'bg-green-500/10' : 'bg-red-500/10',
      borderColor: systemStatus === 'Operacional' ? 'border-green-500/20' : 'border-red-500/20'
    },
  ];

  const quickActions = [
    { title: 'Inventario', desc: 'Gestionar catálogo', icon: Package, route: '/inventario', colorClasses: 'bg-indigo-500/10 text-indigo-400' },
    { title: 'Pedidos', desc: 'Órdenes de clientes', icon: ShoppingCart, route: '/pedidos', colorClasses: 'bg-emerald-500/10 text-emerald-400' },
    { title: 'Despachos', desc: 'Control de logística', icon: Truck, route: '/envios', colorClasses: 'bg-amber-500/10 text-amber-400' },
  ];

  return (
    <div className="min-h-screen bg-slate-950 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-indigo-900/40 via-slate-950 to-slate-950 p-4 md:p-8">
      <div className="max-w-6xl mx-auto">
        <header className="mb-10 flex flex-col md:flex-row md:items-end justify-between gap-4 border-b border-white/10 pb-6">
          <div>
            <h1 className="text-3xl font-bold text-white tracking-tight">Panel de Control</h1>
            <p className="text-slate-400 mt-2">Visión general del sistema y métricas en tiempo real.</p>
          </div>
          <div className="flex items-center gap-2 bg-white/5 border border-white/10 px-4 py-2 rounded-lg backdrop-blur-sm">
            <span className="relative flex h-3 w-3">
              <span className={`animate-ping absolute inline-flex h-full w-full rounded-full opacity-75 ${systemStatus === 'Operacional' ? 'bg-emerald-400' : 'bg-red-400'}`}></span>
              <span className={`relative inline-flex rounded-full h-3 w-3 ${systemStatus === 'Operacional' ? 'bg-emerald-500' : 'bg-red-500'}`}></span>
            </span>
            <span className="text-sm font-medium text-slate-300">Sistemas Online</span>
          </div>
        </header>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
          {stats.map((stat, idx) => (
            <div key={idx} className={`bg-white/5 backdrop-blur-md p-6 rounded-2xl border ${stat.borderColor} flex items-start gap-4 transition-all hover:bg-white/10`}>
                <div className={`${stat.bgColor} p-3 rounded-xl`}>
                  <stat.icon className={`w-6 h-6 ${stat.color}`} />
                </div>
                <div>
                  <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">{stat.label}</p>
                  {loading ? (
                    <Loader2 className="w-5 h-5 animate-spin text-slate-500" />
                  ) : (
                    <p className="text-2xl font-bold text-white">{stat.value ?? '0'}</p>
                  )}
                </div>
            </div>
          ))}
        </div>

        {/* Quick Actions */}
        <div className="flex items-center gap-2 mb-6">
          <BarChart3 className="w-5 h-5 text-indigo-400" />
          <h2 className="text-xl font-bold text-white">Accesos Rápidos</h2>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {quickActions.map((action, idx) => (
            <button
              key={idx}
              onClick={() => navigate(action.route)}
              className="group relative overflow-hidden bg-slate-900/60 backdrop-blur-md p-6 rounded-2xl border border-white/10 flex items-center justify-between transition-all hover:-translate-y-1 hover:border-indigo-500/30 hover:shadow-[0_8px_30px_rgb(0,0,0,0.12)] text-left"
            >
              <div className="absolute inset-0 bg-gradient-to-br from-indigo-500/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
              
              <div className="relative z-10 flex items-center gap-4">
                <div className={`p-3 rounded-xl group-hover:scale-110 transition-transform duration-300 ${action.colorClasses}`}>
                  <action.icon className="w-6 h-6" />
                </div>
                <div>
                  <h3 className="font-bold text-white text-lg">{action.title}</h3>
                  <p className="text-slate-400 text-sm">{action.desc}</p>
                </div>
              </div>
              <div className="relative z-10 bg-white/5 p-2 rounded-full group-hover:bg-indigo-500 transition-colors">
                <ArrowRight className="w-4 h-4 text-slate-400 group-hover:text-white" />
              </div>
            </button>
          ))}
        </div>

      </div>
    </div>
  );
};

export default Dashboard;
