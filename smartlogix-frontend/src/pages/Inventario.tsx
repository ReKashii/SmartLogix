import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { PackageSearch, AlertCircle, Loader2, Plus, Pencil, Trash } from 'lucide-react';

interface Producto {
  id: number;
  nombreProducto: string;
  stock: number;
  precio: number;
}

const Inventario: React.FC = () => {
  const [productos, setProductos] = useState<Producto[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Producto | null>(null);
  const [formData, setFormData] = useState({
    nombreProducto: '',
    stock: 0,
    precio: 0,
  });

  const fetchInventario = async () => {
    setLoading(true);
    try {
      const response = await api.get('/inventario');
      setProductos(response.data);
      setError('');
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.response?.data?.error || err.message || "Ocurrió un error inesperado";
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInventario();
  }, []);

  const handleOpenModal = (product: Producto | null = null) => {
    if (product) {
      setEditingProduct(product);
      setFormData({
        nombreProducto: product.nombreProducto,
        stock: product.stock,
        precio: product.precio,
      });
    } else {
      setEditingProduct(null);
      setFormData({ nombreProducto: '', stock: 0, precio: 0 });
    }
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingProduct(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    
    if (name === 'precio') {
      const cleanValue = value.replace(/\D/g, '');
      setFormData(prev => ({
        ...prev,
        precio: cleanValue === '' ? 0 : parseInt(cleanValue, 10),
      }));
      return;
    }

    setFormData(prev => ({
      ...prev,
      [name]: name === 'nombreProducto' ? value : (value === '' ? 0 : Number(value)),
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingProduct) {
        await api.put(`/inventario/${editingProduct.id}`, formData);
      } else {
        await api.post('/inventario', formData);
      }
      await fetchInventario();
      handleCloseModal();
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.response?.data?.error || err.message || "Ocurrió un error inesperado";
      setError(errorMessage);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('¿Seguro que deseas eliminar este producto?')) return;
    try {
      await api.delete(`/inventario/${id}`);
      await fetchInventario();
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
              <PackageSearch className="w-6 h-6 text-indigo-400" />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-white tracking-tight">Inventario</h1>
              <p className="text-slate-400 mt-1 text-sm">Gestiona el catálogo y control de existencias.</p>
            </div>
          </div>
          <button
            onClick={() => handleOpenModal()}
            className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white px-5 py-2.5 rounded-xl font-medium transition-all duration-300 shadow-[0_0_15px_rgba(79,70,229,0.3)] hover:shadow-[0_0_25px_rgba(79,70,229,0.5)] active:scale-[0.98]"
          >
            <Plus className="w-5 h-5" />
            Nuevo Producto
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
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">Producto</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">Stock</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">Precio</th>
                  <th className="px-6 py-4 text-xs font-semibold text-slate-400 uppercase tracking-wider text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5">
                {loading ? (
                  <tr>
                    <td colSpan={5} className="px-6 py-12 text-center">
                      <div className="flex flex-col items-center gap-3 text-slate-400">
                        <Loader2 className="w-8 h-8 animate-spin text-indigo-500" />
                        <p>Cargando catálogo...</p>
                      </div>
                    </td>
                  </tr>
                ) : productos.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-6 py-12 text-center text-slate-500">
                      No hay productos registrados en el sistema.
                    </td>
                  </tr>
                ) : (
                  productos.map(p => (
                    <tr key={p.id} className="hover:bg-white/5 transition-colors duration-150">
                      <td className="px-6 py-4 text-sm text-slate-500 font-mono">{p.id}</td>
                      <td className="px-6 py-4 text-sm font-medium text-slate-200">{p.nombreProducto}</td>
                      <td className="px-6 py-4 text-sm">
                        <span className={`px-2 py-1 rounded-full text-xs font-semibold border ${
                          p.stock > 10 
                            ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' 
                            : 'bg-amber-500/10 text-amber-400 border-amber-500/20'
                        }`}>
                          {p.stock} unidades
                        </span>
                      </td>
                      <td className="px-6 py-4 text-sm text-slate-300 font-medium">
                        ${p.precio.toLocaleString()}
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
                            <Trash className="w-4 h-4" />
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
                {editingProduct ? 'Editar Producto' : 'Nuevo Producto'}
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
                <label className="block text-sm font-medium text-slate-300 mb-2">Nombre del Producto</label>
                <input
                  type="text"
                  name="nombreProducto"
                  required
                  value={formData.nombreProducto}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2.5 bg-slate-950/50 border border-white/10 rounded-xl focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-400 outline-none transition-all text-white placeholder-slate-600"
                  placeholder="Ej. Laptop Dell XPS"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Stock</label>
                  <input
                    type="number"
                    name="stock"
                    required
                    min="0"
                    step="1"
                    value={formData.stock === 0 ? '' : formData.stock}
                    onChange={handleInputChange}
                    className="w-full px-4 py-2.5 bg-slate-950/50 border border-white/10 rounded-xl focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-400 outline-none transition-all text-white [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Precio ($)</label>
                  <input
                    type="text"
                    inputMode="numeric"
                    name="precio"
                    required
                    value={formData.precio === 0 ? '' : new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(formData.precio)}
                    onChange={handleInputChange}
                    className="w-full px-4 py-2.5 bg-slate-950/50 border border-white/10 rounded-xl focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-400 outline-none transition-all text-white [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                  />
                </div>
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
                  {editingProduct ? 'Actualizar' : 'Guardar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Inventario;
