import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { ShoppingCart, AlertCircle, Loader2, Plus, Pencil, Trash2 } from 'lucide-react';

interface Pedido {
  id: number;
  cliente: string;
  productoId: number;
  cantidad: number;
  montoTotal: number;
  tipoDespacho: string;
}

const Pedidos: React.FC = () => {
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [productos, setProductos] = useState<any[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingPedido, setEditingPedido] = useState<Pedido | null>(null);
  const [formData, setFormData] = useState({
    cliente: '',
    productoId: 0,
    cantidad: 0,
    montoTotal: 0,
    tipoDespacho: 'STANDARD',
  });

  const fetchPedidos = async () => {
    setLoading(true);
    try {
      const response = await api.get('/pedidos');
      setPedidos(response.data);
      setError('');
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.response?.data?.error || err.message || "Ocurrió un error inesperado";
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const fetchProductos = async () => {
    try {
      const res = await api.get('/inventario');
      setProductos(res.data);
    } catch (err) {
      console.error("Error al cargar productos", err);
    }
  };

  useEffect(() => {
    fetchPedidos();
    fetchProductos();
  }, []);

  const handleOpenModal = (pedido: Pedido | null = null) => {
    if (pedido) {
      setEditingPedido(pedido);
      
      let tipoEnum = 'STANDARD';
      if (pedido.tipoDespacho.includes('Express')) tipoEnum = 'EXPRESS';
      else if (pedido.tipoDespacho.includes('Priority') || pedido.tipoDespacho.includes('Next')) tipoEnum = 'NEXT_DAY';

      setFormData({
        cliente: pedido.cliente,
        productoId: pedido.productoId,
        cantidad: pedido.cantidad,
        montoTotal: pedido.montoTotal,
        tipoDespacho: tipoEnum,
      });
    } else {
      setEditingPedido(null);
      setFormData({ cliente: '', productoId: 0, cantidad: 0, montoTotal: 0, tipoDespacho: 'STANDARD' });
    }
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingPedido(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;

    if (name === 'montoTotal') {
      const cleanValue = value.replace(/\D/g, '');
      setFormData(prev => ({
        ...prev,
        montoTotal: cleanValue === '' ? 0 : parseInt(cleanValue, 10),
      }));
      return;
    }

    setFormData(prev => {
      const newData = {
        ...prev,
        [name]: name === 'cliente' || name === 'tipoDespacho' ? value : (value === '' ? 0 : Number(value)),
      };

      // Autocalcular monto total basado en producto y cantidad
      if (name === 'productoId' || name === 'cantidad') {
        const prodId = name === 'productoId' ? Number(value) : prev.productoId;
        const cant = name === 'cantidad' ? Number(value) : prev.cantidad;
        const prod = productos.find(p => p.id === prodId);
        if (prod) {
          newData.montoTotal = prod.precio * cant;
        }
      }

      return newData;
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingPedido) {
        await api.put(`/pedidos/${editingPedido.id}`, formData);
      } else {
        await api.post('/pedidos', formData);
      }
      await fetchPedidos();
      handleCloseModal();
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.response?.data?.error || err.message || "Ocurrió un error inesperado";
      setError(errorMessage);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('¿Seguro que deseas cancelar y eliminar este pedido?')) return;
    try {
      await api.delete(`/pedidos/${id}`);
      await fetchPedidos();
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.response?.data?.error || err.message || "Ocurrió un error inesperado";
      setError(errorMessage);
    }
  };

  return (
    <div className="min-h-screen p-4 md:p-8">
      <div className="max-w-6xl mx-auto">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-4 mb-8">
          <div className="flex items-center gap-3">
            <div className="p-3 bg-indigo-500/20 rounded-xl border border-indigo-500/30">
              <ShoppingCart className="w-6 h-6 text-indigo-400" />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-white tracking-tight">Pedidos</h1>
              <p className="text-slate-400 mt-1 text-sm">Órdenes de clientes y seguimiento de facturación.</p>
            </div>
          </div>
          <button
            onClick={() => handleOpenModal()}
            className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white px-5 py-2.5 rounded-xl font-medium transition-all duration-300 shadow-[0_0_15px_rgba(79,70,229,0.3)] hover:shadow-[0_0_25px_rgba(79,70,229,0.5)] active:scale-[0.98]"
          >
            <Plus className="w-5 h-5" />
            Nuevo Pedido
          </button>
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
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">ID</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">Cliente</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">Prod ID</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">Cant.</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">Monto</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">Despacho</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5">
                {loading ? (
                  <tr>
                    <td colSpan={7} className="px-6 py-12 text-center">
                      <div className="flex flex-col items-center gap-3 text-slate-400">
                        <Loader2 className="w-8 h-8 animate-spin text-indigo-500" />
                        <p>Cargando órdenes...</p>
                      </div>
                    </td>
                  </tr>
                ) : pedidos.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="px-6 py-12 text-center text-slate-500">
                      No se encontraron pedidos en la lista.
                    </td>
                  </tr>
                ) : (
                  pedidos.map(p => (
                    <tr key={p.id} className="hover:bg-white/5 transition-colors duration-150">
                      <td className="px-6 py-4 text-sm text-slate-500 font-mono">{p.id}</td>
                      <td className="px-6 py-4 text-sm font-medium text-slate-200">{p.cliente}</td>
                      <td className="px-6 py-4 text-sm text-slate-400 font-mono">#{p.productoId}</td>
                      <td className="px-6 py-4 text-sm text-slate-300">{p.cantidad}</td>
                      <td className="px-6 py-4 text-sm text-slate-300 font-medium">
                        ${p.montoTotal.toLocaleString('es-CL')}
                      </td>
                      <td className="px-6 py-4 text-sm">
                        <span className={`px-2 py-1 rounded-full text-xs font-semibold border ${
                          p.tipoDespacho === 'EXPRESS' 
                            ? 'bg-purple-500/10 text-purple-400 border-purple-500/20' 
                            : 'bg-white/5 text-slate-300 border-white/10'
                        }`}>
                          {p.tipoDespacho || 'STANDARD'}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-sm text-right">
                        <div className="flex justify-end gap-2">
                          <button
                            onClick={() => handleOpenModal(p)}
                            className="p-2 bg-white/5 hover:bg-indigo-500/20 text-slate-400 hover:text-indigo-400 rounded-lg transition-colors"
                            title="Editar"
                          >
                            <Pencil className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleDelete(p.id)}
                            className="p-2 bg-white/5 hover:bg-red-500/20 text-slate-400 hover:text-red-400 rounded-lg transition-colors"
                            title="Eliminar"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="bg-slate-900 border border-white/10 rounded-2xl shadow-2xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in duration-200">
            <div className="px-6 py-4 border-b border-white/10 flex justify-between items-center bg-white/5">
              <h2 className="text-xl font-bold text-white">
                {editingPedido ? 'Editar Pedido' : 'Nuevo Pedido'}
              </h2>
              <button 
                onClick={handleCloseModal}
                className="text-slate-400 hover:text-white p-1 transition-colors"
              >
                ✕
              </button>
            </div>
            <form onSubmit={handleSubmit} className="p-6 space-y-5">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Nombre del Cliente</label>
                <input
                  type="text"
                  name="cliente"
                  required
                  value={formData.cliente}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2.5 bg-slate-950/50 border border-white/10 rounded-xl focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-400 outline-none transition-all text-white placeholder-slate-600"
                  placeholder="Ej. Wacoldo Soto"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Producto</label>
                  <select
                    name="productoId"
                    required
                    value={formData.productoId === 0 ? '' : formData.productoId}
                    onChange={handleInputChange}
                    className="w-full px-4 py-2.5 bg-slate-950/50 border border-white/10 rounded-xl focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-400 outline-none transition-all text-white [&>option]:bg-slate-900"
                  >
                    <option value="" disabled>Selecciona un producto...</option>
                    {productos.map(p => (
                      <option key={p.id} value={p.id}>
                        {p.nombre} (${p.precio.toLocaleString('es-CL')} - Stock: {p.stock})
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Cantidad</label>
                  <input
                    type="number"
                    name="cantidad"
                    required
                    min="1"
                    step="1"
                    value={formData.cantidad === 0 ? '' : formData.cantidad}
                    onChange={handleInputChange}
                    className="w-full px-4 py-2.5 bg-slate-950/50 border border-white/10 rounded-xl focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-400 outline-none transition-all text-white [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Monto Total</label>
                <input
                  type="text"
                  inputMode="numeric"
                  name="montoTotal"
                  required
                  value={formData.montoTotal === 0 ? '' : new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(formData.montoTotal)}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2.5 bg-slate-950/50 border border-white/10 rounded-xl focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-400 outline-none transition-all text-white"
                  placeholder="$ 0"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Tipo de Despacho</label>
                <select
                  name="tipoDespacho"
                  value={formData.tipoDespacho}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2.5 bg-slate-950/50 border border-white/10 rounded-xl focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-400 outline-none transition-all text-white [&>option]:bg-slate-900"
                >
                  <option value="STANDARD">Standard</option>
                  <option value="EXPRESS">Express</option>
                  <option value="NEXT_DAY">Next Day</option>
                </select>
              </div>
              <div className="pt-6 flex justify-end gap-3 border-t border-white/5">
                <button
                  type="button"
                  onClick={handleCloseModal}
                  className="px-5 py-2.5 text-sm font-medium text-slate-300 hover:bg-white/5 rounded-xl transition-colors"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-5 py-2.5 text-sm font-medium bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl transition-all shadow-[0_0_15px_rgba(79,70,229,0.3)] hover:shadow-[0_0_25px_rgba(79,70,229,0.5)]"
                >
                  {editingPedido ? 'Actualizar' : 'Guardar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Pedidos;
