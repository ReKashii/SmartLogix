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

interface Inventario {
  id: number;
  nombreProducto: string;
  stock: number;
  precio: number;
}

const Pedidos: React.FC = () => {
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [productos, setProductos] = useState<Inventario[]>([]);
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

  useEffect(() => {
    fetchPedidos();
    fetchProductos();
  }, []);

  const fetchProductos = async () => {
    try {
      const response = await api.get('/inventario');
      setProductos(response.data);
    } catch (err: any) {
      console.error("Error fetching productos", err);
    }
  };

  const handleOpenModal = (pedido: Pedido | null = null) => {
    if (pedido) {
      setEditingPedido(pedido);
      
      let mappedDespacho = 'STANDARD';
      if (pedido.tipoDespacho?.includes('Express')) mappedDespacho = 'EXPRESS';
      if (pedido.tipoDespacho?.includes('Next Day')) mappedDespacho = 'NEXT_DAY';

      setFormData({
        cliente: pedido.cliente,
        productoId: pedido.productoId,
        cantidad: pedido.cantidad,
        montoTotal: pedido.montoTotal,
        tipoDespacho: mappedDespacho,
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
      const parsedValue = name === 'cliente' || name === 'tipoDespacho' ? value : (value === '' ? 0 : Number(value));
      const newFormData = { ...prev, [name]: parsedValue };
      
      if (name === 'productoId' || name === 'cantidad') {
        const prodId = name === 'productoId' ? parsedValue as number : prev.productoId;
        const cant = name === 'cantidad' ? parsedValue as number : prev.cantidad;
        const productoSel = productos.find(p => p.id === prodId);
        if (productoSel) {
          newFormData.montoTotal = productoSel.precio * cant;
        }
      }

      return newFormData;
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
    if (!window.confirm('Are you sure you want to delete this order?')) return;
    try {
      await api.delete(`/pedidos/${id}`);
      await fetchPedidos();
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.response?.data?.error || err.message || "Ocurrió un error inesperado";
      setError(errorMessage);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 p-4 md:p-8">
      <div className="max-w-6xl mx-auto">
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-indigo-600 rounded-lg text-white">
              <ShoppingCart className="w-6 h-6" />
            </div>
            <h1 className="text-3xl font-bold text-slate-800">Gestión de Pedidos</h1>
          </div>
          <button
            onClick={() => handleOpenModal()}
            className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg font-medium transition-colors duration-200 shadow-sm"
          >
            <Plus className="w-5 h-5" />
            Nuevo Pedido
          </button>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-50 border-l-4 border-red-500 text-red-700 flex items-center gap-3 rounded-r-lg shadow-sm">
            <AlertCircle className="w-5 h-5" />
            <p className="font-medium">{error}</p>
          </div>
        )}

        <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-100 border-b border-slate-200">
                  <th className="px-6 py-4 text-sm font-semibold text-slate-600 uppercase tracking-wider">ID</th>
                  <th className="px-6 py-4 text-sm font-semibold text-slate-600 uppercase tracking-wider">Cliente</th>
                  <th className="px-6 py-4 text-sm font-semibold text-slate-600 uppercase tracking-wider">Prod ID</th>
                  <th className="px-6 py-4 text-sm font-semibold text-slate-600 uppercase tracking-wider">Cant.</th>
                  <th className="px-6 py-4 text-sm font-semibold text-slate-600 uppercase tracking-wider">Monto</th>
                  <th className="px-6 py-4 text-sm font-semibold text-slate-600 uppercase tracking-wider">Despacho</th>
                  <th className="px-6 py-4 text-sm font-semibold text-slate-600 uppercase tracking-wider text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {loading ? (
                  <tr>
                    <td colSpan={7} className="px-6 py-12 text-center">
                      <div className="flex flex-col items-center gap-2 text-slate-500">
                        <Loader2 className="w-8 h-8 animate-spin" />
                        <p>Cargando pedidos...</p>
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
                    <tr key={p.id} className="hover:bg-slate-50 transition-colors duration-150">
                      <td className="px-6 py-4 text-sm text-slate-500 font-mono">{p.id}</td>
                      <td className="px-6 py-4 text-sm font-medium text-slate-800">{p.cliente}</td>
                      <td className="px-6 py-4 text-sm text-slate-600">{p.productoId}</td>
                      <td className="px-6 py-4 text-sm text-slate-600">{p.cantidad}</td>
                      <td className="px-6 py-4 text-sm text-slate-600 font-medium">
                        ${p.montoTotal.toLocaleString('es-CL')}
                      </td>
                      <td className="px-6 py-4 text-sm">
                        <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                          p.tipoDespacho === 'EXPRESS' ? 'bg-green-100 text-green-700' : 'bg-slate-100 text-slate-700'
                        }`}>
                          {p.tipoDespacho || 'STANDARD'}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-sm text-right">
                        <div className="flex justify-end gap-3">
                          <button
                            onClick={() => handleOpenModal(p)}
                            className="p-1 text-slate-400 hover:text-blue-600 transition-colors"
                            title="Editar"
                          >
                            <Pencil className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleDelete(p.id)}
                            className="p-1 text-slate-400 hover:text-red-600 transition-colors"
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
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in duration-200">
            <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center">
              <h2 className="text-xl font-bold text-slate-800">
                {editingPedido ? 'Editar Pedido' : 'Nuevo Pedido'}
              </h2>
              <button 
                onClick={handleCloseModal}
                className="text-slate-400 hover:text-slate-600 p-1"
              >
                ✕
              </button>
            </div>
            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Nombre del Cliente</label>
                <input
                  type="text"
                  name="cliente"
                  required
                  value={formData.cliente}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all text-sm"
                  placeholder="Ej. Wacoldo Soto"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Producto</label>
                  <select
                    name="productoId"
                    required
                    value={formData.productoId === 0 ? '' : formData.productoId}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all text-sm bg-white"
                  >
                    <option value="" disabled>Seleccione un producto</option>
                    {productos.map(p => (
                      <option key={p.id} value={p.id}>
                        {p.nombreProducto} - ${p.precio.toLocaleString('es-CL')} (Stock: {p.stock})
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Cantidad</label>
                  <input
                    type="number"
                    name="cantidad"
                    required
                    min="1"
                    step="1"
                    value={formData.cantidad === 0 ? '' : formData.cantidad}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all text-sm [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Monto Total</label>
                <input
                  type="text"
                  inputMode="numeric"
                  name="montoTotal"
                  required
                  value={formData.montoTotal === 0 ? '' : new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(formData.montoTotal)}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all text-sm"
                  placeholder="$ 0"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Tipo de Despacho</label>
                <select
                  name="tipoDespacho"
                  value={formData.tipoDespacho}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all text-sm bg-white"
                >
                  <option value="STANDARD">Standard</option>
                  <option value="EXPRESS">Express</option>
                  <option value="NEXT_DAY">Next Day</option>
                </select>
              </div>
              <div className="pt-4 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={handleCloseModal}
                  className="px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 text-sm font-medium bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition-colors shadow-sm"
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
