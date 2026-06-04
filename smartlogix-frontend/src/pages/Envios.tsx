import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { Truck, AlertCircle, Loader2, CheckCircle } from 'lucide-react';

interface Envio {
  id: number;
  pedidoId: number;
  tipoDespacho: string;
  estadoEnvio: string;
  costo: number;
  fechaEstimadaEntrega: string;
}

const Envios: React.FC = () => {
  const [envios, setEnvios] = useState<Envio[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const fetchEnvios = async () => {
    setLoading(true);
    try {
      const response = await api.get('/envios');
      setEnvios(response.data);
      setError('');
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.response?.data?.error || err.message || "Ocurrió un error al cargar los despachos";
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEnvios();
  }, []);

  const handleUpdateEstado = async (id: number, nuevoEstado: string) => {
    if (!window.confirm(`¿Seguro que deseas marcar este envío como ${nuevoEstado}?`)) return;
    try {
      await api.put(`/envios/${id}/estado`, { estado: nuevoEstado });
      await fetchEnvios();
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.response?.data?.error || err.message || "Ocurrió un error al actualizar el estado";
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
      default:
        return <span className="px-2 py-1 bg-white/5 border border-white/10 text-slate-300 rounded-full text-xs font-semibold">{status}</span>;
    }
  };

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

        <div className="bg-slate-900/60 backdrop-blur-xl rounded-2xl shadow-2xl border border-white/10 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-white/5 border-b border-white/10">
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">ID Envío</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">ID Pedido</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">Servicio</th>
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
                ) : envios.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="px-6 py-12 text-center text-slate-500">
                      No hay despachos registrados.
                    </td>
                  </tr>
                ) : (
                  envios.map(envio => (
                    <tr key={envio.id} className="hover:bg-white/5 transition-colors duration-150">
                      <td className="px-6 py-4 text-sm text-slate-500 font-mono">{envio.id}</td>
                      <td className="px-6 py-4 text-sm font-medium text-slate-200">#{envio.pedidoId}</td>
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
                      </td>
                    </tr>
                  ))
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
