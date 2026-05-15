import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { ShoppingCart, PlusCircle, AlertCircle, CheckCircle2, Loader2, Package } from 'lucide-react';

interface Pedido {
  id: number;
  cliente: string;
  montoTotal: number;
  tipoDespacho: string;
  estado: string;
}

const Pedidos: React.FC = () => {
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [form, setForm] = useState({ cliente: '', productId: '', quantity: 1, amount: 0, shipType: 'STANDARD' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(true);

  const fetchPedidos = async () => {
    setLoading(true);
    try {
      const response = await api.get('/pedidos');
      setPedidos(response.data);
      setError('');
    } catch (err) {
      setError('Error fetching orders');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPedidos();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess(false);
    try {
      await api.post(`/pedidos?cliente=${form.cliente}&productId=${form.productId}&quantity=${form.quantity}&amount=${form.amount}&shipType=${form.shipType}`);
      setForm({ cliente: '', productId: '', quantity: 1, amount: 0, shipType: 'STANDARD' });
      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
      fetchPedidos();
    } catch (err: any) {
      setError(err.response?.data || 'Error creating order');
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 p-4 md:p-8">
      <div className="max-w-7xl mx-auto">
        <div className="flex items-center gap-3 mb-8">
          <div className="p-2 bg-indigo-600 rounded-lg text-white">
            <ShoppingCart className="w-6 h-6" />
          </div>
          <h1 className="text-3xl font-bold text-slate-800">Gestión de Pedidos</h1>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* New Order Form */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 sticky top-8">
              <div className="flex items-center gap-2 mb-6">
                <PlusCircle className="w-5 h-5 text-indigo-600" />
                <h2 className="text-xl font-semibold text-slate-800">Nuevo Pedido</h2>
              </div>

              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Cliente</label>
                  <input 
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all"
                    placeholder="Nombre del cliente" 
                    value={form.cliente} 
                    onChange={e => setForm({...form, cliente: e.target.value})} 
                    required 
                  />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">ID Producto</label>
                    <input 
                      className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all"
                      type="number" 
                      value={form.productId} 
                      onChange={e => setForm({...form, productId: e.target.value})} 
                      required 
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Cantidad</label>
                    <input 
                      className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all"
                      type="number" 
                      value={form.quantity} 
                      onChange={e => setForm({...form, quantity: parseInt(e.target.value)})} 
                      required 
                    />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Monto Total ($)</label>
                  <input 
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all"
                    type="number" 
                    value={form.amount} 
                    onChange={e => setForm({...form, amount: parseFloat(e.target.value)})} 
                    required 
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Tipo de Despacho</label>
                  <select 
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all bg-white"
                    value={form.shipType} 
                    onChange={e => setForm({...form, shipType: e.target.value})}
                  >
                    <option value="STANDARD">Standard</option>
                    <option value="EXPRESS">Express</option>
                    <option value="NEXT_DAY">Next Day</option>
                  </select>
                </div>
                
                <button 
                  type="submit" 
                  className="w-full bg-indigo-600 text-white py-2 rounded-lg font-semibold hover:bg-indigo-700 active:scale-95 transition-all duration-200 shadow-sm"
                >
                  Crear Pedido
                </button>
              </form>

              {error && (
                <div className="mt-4 p-3 bg-red-50 border-l-4 border-red-500 text-red-700 flex items-center gap-2 text-sm rounded-r-lg">
                  <AlertCircle className="w-4 h-4" />
                  <p>{error}</p>
                </div>
              )}
              {success && (
                <div className="mt-4 p-3 bg-green-50 border-l-4 border-green-500 text-green-700 flex items-center gap-2 text-sm rounded-r-lg">
                  <CheckCircle2 className="w-4 h-4" />
                  <p>Pedido creado exitosamente</p>
                </div>
              )}
            </div>
          </div>

          {/* Orders Table */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
              <div className="px-6 py-4 border-b border-slate-200 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Package className="w-5 h-5 text-slate-500" />
                  <h2 className="text-lg font-semibold text-slate-800">Lista de Pedidos</h2>
                </div>
                <span className="text-xs font-medium px-2 py-1 bg-slate-100 text-slate-600 rounded-md">
                  {pedidos.length} Pedidos
                </span>
              </div>
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-slate-50 border-b border-slate-200">
                      <th className="px-6 py-4 text-sm font-semibold text-slate-600 uppercase tracking-wider">ID</th>
                      <th className="px-6 py-4 text-sm font-semibold text-slate-600 uppercase tracking-wider">Cliente</th>
                      <th className="px-6 py-4 text-sm font-semibold text-slate-600 uppercase tracking-wider">Monto</th>
                      <th className="px-6 py-4 text-sm font-semibold text-slate-600 uppercase tracking-wider">Despacho</th>
                      <th className="px-6 py-4 text-sm font-semibold text-slate-600 uppercase tracking-wider">Estado</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-200">
                    {loading ? (
                      <tr>
                        <td colSpan={5} className="px-6 py-12 text-center">
                          <div className="flex flex-col items-center gap-2 text-slate-500">
                            <Loader2 className="w-8 h-8 animate-spin" />
                            <p>Cargando pedidos...</p>
                          </div>
                        </td>
                      </tr>
                    ) : pedidos.length === 0 ? (
                      <tr>
                        <td colSpan={5} className="px-6 py-12 text-center text-slate-500">
                          No hay pedidos registrados.
                        </td>
                      </tr>
                    ) : (
                      pedidos.map(p => (
                        <tr key={p.id} className="hover:bg-slate-50 transition-colors duration-150">
                          <td className="px-6 py-4 text-sm text-slate-500 font-mono">{p.id}</td>
                          <td className="px-6 py-4 text-sm font-medium text-slate-800">{p.cliente}</td>
                          <td className="px-6 py-4 text-sm text-slate-600 font-medium">${p.montoTotal.toLocaleString()}</td>
                          <td className="px-6 py-4 text-sm">
                            <span className="px-2 py-1 rounded-md text-xs font-medium bg-slate-100 text-slate-600 border border-slate-200">
                              {p.tipoDespacho}
                            </span>
                          </td>
                          <td className="px-6 py-4 text-sm">
                            <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                              p.estado === 'DELIVERED' ? 'bg-green-100 text-green-700' : 'bg-blue-100 text-blue-700'
                            }`}>
                              {p.estado}
                            </span>
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
      </div>
    </div>
  );
};

export default Pedidos;
