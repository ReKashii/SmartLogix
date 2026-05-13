import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';

interface Producto {
  id: number;
  nombreProducto: string;
  stock: number;
  precio: number;
}

const Inventario: React.FC = () => {
  const [productos, setProductos] = useState<Producto[]>([]);
  const [error, setError] = useState('');

  const fetchInventario = async () => {
    try {
      const response = await api.get('/inventario');
      setProductos(response.data);
    } catch (err) {
      setError('Error fetching inventory');
    }
  };

  useEffect(() => {
    fetchInventario();
  }, []);

  return (
    <div style={{ padding: '2rem' }}>
      <h1>Inventario de Productos</h1>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <table border={1} style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Producto</th>
            <th>Stock</th>
            <th>Precio</th>
          </tr>
        </thead>
        <tbody>
          {productos.map(p => (
            <tr key={p.id}>
              <td>{p.id}</td>
              <td>{p.nombreProducto}</td>
              <td>{p.stock}</td>
              <td>${p.precio}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default Inventario;
