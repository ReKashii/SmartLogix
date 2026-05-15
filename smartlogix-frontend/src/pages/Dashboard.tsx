import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Package, ShoppingCart, Activity, ArrowRight, Loader2 } from 'lucide-react';
import api from '../api/axiosConfig';

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [totalProductos, setTotalProductos] = useState(0);
  const [pedidosActivos, setPedidosActivos] = useState(0);
  const [systemStatus, setSystemStatus] = useState('Operacional');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      setLoading(true);
      let inventoryOk = false;
      let pedidosOk = false;

      try {
        const invRes = await api.get('/inventario');
        setTotalProductos(invRes.data.length);
        inventoryOk = true;
      } catch (err) {
        console.error('Error fetching inventory', err);
      }

      try {
        const pedRes = await api.get('/pedidos');
        setPedidosActivos(pedRes.data.length);
        pedidosOk = true;
      } catch (err) {
        console.error('Error fetching pedidos', err);
      }

      setSystemStatus(inventoryOk && pedidosOk ? 'Operacional' : 'Intermitente');
      setLoading(false);
    };

    fetchStats();
  }, []);

  const stats = [
    { 
      label: 'Productos Totales', 
      value: loading ? null : totalProductos.toLocaleString(), 
      icon: Package, 
      color: 'bg-blue-500', 
      textColor: 'text-blue-600',
      bgColor: 'bg-blue-50' 
    },
    { 
      label: 'Pedidos Activos', 
      value: loading ? null : pedidosActivos.toLocaleString(), 
      icon: ShoppingCart, 
      color: 'bg-emerald-500', 
      textColor: 'text-emerald-600',
      bgColor: 'bg-emerald-50' 
    },
    { 
      label: 'Estado del Sistema', 
      value: loading ? null : systemStatus, 
      icon: Activity, 
      color: 'bg-amber-500', 
      textColor: 'text-amber-600',
      bgColor: 'bg-amber-50' 
    },
  ];

  return (
    <div className="min-h-screen bg-slate-50 p-4 md:p-8">
      <div className="max-w-6xl mx-auto">
        <header className="mb-10">
          <h1 className="text-3xl font-bold text-slate-800">Panel de Control</h1>
          <p className="text-slate-500 mt-1">Bienvenido al centro de mando de SmartLogix Enterprise.</p>
        </header>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
          {stats.map((stat, idx) => (
            <div key={idx} className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex items-center gap-5 transition-all hover:shadow-md">
                <div className={`${stat.bgColor} p-4 rounded-xl`}>
                  <stat.icon className={`w-6 h-6 ${stat.textColor}`} />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-500 uppercase tracking-wider">{stat.label}</p>
                  {loading ? (
                    <Loader2 className="w-6 h-6 animate-spin text-slate-400 mt-1" />
                  ) : (
                    <p className="text-2xl font-bold text-slate-800">{stat.value ?? '0'}</p>
                  )}
                </div>
            </div>
          ))}
        </div>

        <h2 className="text-xl font-bold text-slate-800 mb-6">Acciones Rápidas</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <button
            onClick={() => navigate('/inventario')}
            className="group bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex items-center justify-between transition-all hover:-translate-y-1 hover:shadow-lg text-left"
          >
            <div className="flex items-center gap-4">
              <div className="p-3 bg-blue-100 text-blue-600 rounded-lg group-hover:bg-blue-600 group-hover:text-white transition-colors">
                <Package className="w-6 h-6" />
              </div>
              <div>
                <h3 className="font-bold text-slate-800 text-lg">Gestión de Inventario</h3>
                <p className="text-slate-500 text-sm">Control de existencias, precios y stock.</p>
              </div>
            </div>
            <ArrowRight className="w-5 h-5 text-slate-300 group-hover:text-blue-600 transition-colors" />
          </button>

          <button
            onClick={() => navigate('/pedidos')}
            className="group bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex items-center justify-between transition-all hover:-translate-y-1 hover:shadow-lg text-left"
          >
            <div className="flex items-center gap-4">
              <div className="p-3 bg-emerald-100 text-emerald-600 rounded-lg group-hover:bg-emerald-600 group-hover:text-white transition-colors">
                <ShoppingCart className="w-6 h-6" />
              </div>
              <div>
                <h3 className="font-bold text-slate-800 text-lg">Gestión de Pedidos</h3>
                <p className="text-slate-500 text-sm">Seguimiento y procesamiento de órdenes.</p>
              </div>
            </div>
            <ArrowRight className="w-5 h-5 text-slate-300 group-hover:text-emerald-600 transition-colors" />
          </button>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;

