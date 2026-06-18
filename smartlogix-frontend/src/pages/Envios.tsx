import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { Truck, AlertCircle, Loader2, CheckCircle, Trash2 } from 'lucide-react';

interface Envio {
  id: number;
  pedidoId: number;
  tipoDespacho: string;
  estadoEnvio: string;
  costo: number;
  fechaEstimadaEntrega: string;
  trackingNumber?: string;
}

const Envios: React.FC = () => {
  const [envios, setEnvios] = useState<Envio[]>([]);
  const [pedidos, setPedidos] = useState<any[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<string>('ALL');

  const fetchData = async () => {
    setLoading(true);
    try {
      const [enviosRes, pedidosRes] = await Promise.all([
        api.get('/envios'),
        api.get('/pedidos').catch(() => ({ data: [] })) // Fallback si ms-pedidos falla
      ]);
      setEnvios(enviosRes.data);
      setPedidos(pedidosRes.data);
      setError('');
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.response?.data?.error || err.message || "Ocurrió un error al cargar los datos";
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleUpdateEstado = async (id: number, nuevoEstado: string) => {
    if (!window.confirm(`¿Seguro que deseas marcar este envío como ${nuevoEstado}?`)) return;
    try {
      await api.put(`/envios/${id}/estado`, { estado: nuevoEstado });
      await fetchData();
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.response?.data?.error || err.message || "Ocurrió un error al actualizar el estado";
      setError(errorMessage);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm(`¿Seguro que deseas eliminar este envío? Esto también cancelará el pedido asociado y restaurará el inventario.`)) return;
    try {
      await api.delete(`/envios/${id}`);
      await fetchData();
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.response?.data?.error || err.message || "Ocurrió un error al eliminar";
      setError(errorMessage);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <span className="px-2 py-1 bg-amber-500/10 border border-amber-500/20 text-amber-400 rounded-full text-xs font-semibold">Pendiente</span>;
      case 'DISPATCHED':
        return <span className="px-2 py-1 bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 rounded-full text-xs font-semibold">Despachado</span>;
      case 'DELIVERED':
        return <span className="px-2 py-1 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 rounded-full text-xs font-semibold">Entregado</span>;
      case 'CANCELLED':
        return <span className="px-2 py-1 bg-red-500/10 border border-red-500/20 text-red-400 rounded-full text-xs font-semibold">Cancelado</span>;
      default:
        return <span className="px-2 py-1 bg-white/5 border border-white/10 text-slate-300 rounded-full text-xs font-semibold">{status}</span>;
    }
  };

  const filteredEnvios = envios.filter(envio => activeTab === 'ALL' || envio.estadoEnvio === activeTab);

  return (
    <div className="min-h-screen p-4 md:p-8">
      <div className="max-w-6xl mx-auto">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-4 mb-8">
          <div className="flex items-center gap-3">
            <div className="p-3 bg-indigo-500/20 rounded-xl border border-indigo-500/30">
              <Truck className="w-6 h-6 text-indigo-400" />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-white tracking-tight">Despachos</h1>
              <p className="text-slate-400 mt-1 text-sm">Logística y seguimiento de entregas.</p>
            </div>
          </div>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-500/10 border border-red-500/50 rounded-xl flex items-center gap-3 animate-in slide-in-from-top-2 duration-300">
            <AlertCircle className="w-5 h-5 text-red-400" />
            <p className="font-medium text-red-200">{error}</p>
          </div>
        )}

        <div className="flex gap-2 mb-6 overflow-x-auto pb-2 custom-scrollbar">
          {['ALL', 'PENDING', 'DISPATCHED', 'DELIVERED', 'CANCELLED'].map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-4 py-2 rounded-xl text-sm font-medium transition-all whitespace-nowrap ${
                activeTab === tab 
                  ? 'bg-indigo-600 text-white shadow-[0_0_15px_rgba(79,70,229,0.3)]' 
                  : 'bg-slate-800/50 text-slate-400 hover:bg-slate-800 hover:text-slate-200 border border-white/5'
              }`}
            >
              {tab === 'ALL' ? 'Todos los Envíos' : 
               tab === 'PENDING' ? 'Pendientes' : 
               tab === 'DISPATCHED' ? 'En Camino' : 
               tab === 'DELIVERED' ? 'Entregados' : 'Cancelados'}
            </button>
          ))}
        </div>

        <div className="bg-slate-900/60 backdrop-blur-xl rounded-2xl shadow-2xl border border-white/10 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-white/5 border-b border-white/10">
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">Tracking / ID</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">Cliente</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">Tipo</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">Costo</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">Fecha Est.</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">Estado</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider text-right">Acción</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5">
                {loading ? (
                  <tr>
                    <td colSpan={7} className="px-6 py-12 text-center">
                      <div className="flex flex-col items-center gap-3 text-slate-400">
                        <Loader2 className="w-8 h-8 animate-spin text-indigo-500" />
                        <p>Cargando despachos...</p>
                      </div>
                    </td>
                  </tr>
                ) : filteredEnvios.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="px-6 py-12 text-center text-slate-500">
                      No hay despachos registrados para esta categoría.
                    </td>
                  </tr>
                ) : (
                  filteredEnvios.map(envio => {
                    const pedido = pedidos.find(p => p.id === envio.pedidoId);
                    return (
                    <tr key={envio.id} className="hover:bg-white/5 transition-colors duration-150">
                      <td className="px-6 py-4">
                        <div className="text-sm font-bold text-indigo-400 font-mono">
                          {envio.trackingNumber || `N/A`}
                        </div>
                        <div className="text-xs text-slate-500">
                          ID: {envio.id}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm font-medium text-slate-200">
                          {pedido ? pedido.cliente : `Cliente Desconocido`}
                        </div>
                        <div className="text-xs text-slate-500 font-mono">
                          Pedido #{envio.pedidoId}
                        </div>
                      </td>
                      <td className="px-6 py-4 text-sm text-slate-300 font-medium">{envio.tipoDespacho}</td>
                      <td className="px-6 py-4 text-sm text-slate-300">
                        ${envio.costo.toLocaleString('es-CL')}
                      </td>
                      <td className="px-6 py-4 text-sm text-slate-300">
                        {new Date(envio.fechaEstimadaEntrega).toLocaleDateString('es-CL')}
                      </td>
                      <td className="px-6 py-4 text-sm">
                        {getStatusBadge(envio.estadoEnvio)}
                      </td>
                      <td className="px-6 py-4 text-sm text-right">
                        <div className="flex items-center justify-end gap-2">
                          {envio.estadoEnvio === 'PENDING' && (
                            <button
                              onClick={() => handleUpdateEstado(envio.id, 'DISPATCHED')}
                              className="inline-flex items-center gap-2 px-3 py-1.5 bg-indigo-600/20 hover:bg-indigo-600/40 border border-indigo-500/30 text-indigo-400 hover:text-indigo-300 text-xs font-medium rounded-lg transition-colors"
                            >
                              <Truck className="w-3.5 h-3.5" />
                              Despachar
                            </button>
                          )}
                          {envio.estadoEnvio === 'DISPATCHED' && (
                            <button
                              onClick={() => handleUpdateEstado(envio.id, 'DELIVERED')}
                              className="inline-flex items-center gap-2 px-3 py-1.5 bg-emerald-600/20 hover:bg-emerald-600/40 border border-emerald-500/30 text-emerald-400 hover:text-emerald-300 text-xs font-medium rounded-lg transition-colors"
                            >
                              <CheckCircle className="w-3.5 h-3.5" />
                              Entregar
                            </button>
                          )}
                          {envio.estadoEnvio === 'CANCELLED' && (
                            <span className="text-xs text-red-500/50 font-medium">Cancelado</span>
                          )}
                          
                          <button
                            onClick={() => handleDelete(envio.id)}
                            className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-400/10 rounded-lg transition-colors"
                            title="Eliminar Envío"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Envios;
