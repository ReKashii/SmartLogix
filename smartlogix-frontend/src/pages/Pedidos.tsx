import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';

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

  const fetchPedidos = async () => {
    try {
      const response = await api.get('/pedidos');
      setPedidos(response.data);
    } catch (err) {
      setError('Error fetching orders');
    }
  };

  useEffect(() => {
    fetchPedidos();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      // Using query params as requested by the PedidoController implementation
      await api.post(`/pedidos?cliente=${form.cliente}&productId=${form.productId}&quantity=${form.quantity}&amount=${form.amount}&shipType=${form.shipType}`);
      setForm({ cliente: '', productId: '', quantity: 1, amount: 0, shipType: 'STANDARD' });
      fetchPedidos();
    } catch (err: any) {
      setError(err.response?.data || 'Error creating order');
    }
  };

  return (
    <div style={{ padding: '2rem' }}>
      <h1>Gestión de Pedidos</h1>
      
      <form onSubmit={handleSubmit} style={{ marginBottom: '2rem', display: 'flex', flexDirection: 'column', gap: '1rem', maxWidth: '400px' }}>
        <input placeholder="Cliente" value={form.cliente} onChange={e => setForm({...form, cliente: e.target.value})} required />
        <input placeholder="ID Producto" type="number" value={form.productId} onChange={e => setForm({...form, productId: e.target.value})} required />
        <input placeholder="Cantidad" type="number" value={form.quantity} onChange={e => setForm({...form, quantity: parseInt(e.target.value)})} required />
        <input placeholder="Monto Total" type="number" value={form.amount} onChange={e => setForm({...form, amount: parseFloat(e.target.value)})} required />
        <select value={form.shipType} onChange={e => setForm({...form, shipType: e.target.value})}>
          <option value="STANDARD">Standard</option>
          <option value="EXPRESS">Express</option>
          <option value="NEXT_DAY">Next Day</option>
        </select>
        <button type="submit">Crear Pedido</button>
      </form>

      {error && <p style={{ color: 'red' }}>{error}</p>}

      <table border={1} style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Cliente</th>
            <th>Monto</th>
            <th>Despacho</th>
            <th>Estado</th>
          </tr>
        </thead>
        <tbody>
          {pedidos.map(p => (
            <tr key={p.id}>
              <td>{p.id}</td>
              <td>{p.cliente}</td>
              <td>{p.montoTotal}</td>
              <td>{p.tipoDespacho}</td>
              <td>{p.estado}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default Pedidos;
