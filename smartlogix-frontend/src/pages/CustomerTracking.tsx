import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import api from '../api/axiosConfig';
import { Search, Truck, CheckCircle, Package } from 'lucide-react';

interface Envio {
  id: number;
  pedidoId: number;
  tipoDespacho: string;
  estadoEnvio: string;
  costo: number;
  fechaEstimadaEntrega: string;
  trackingNumber: string;
}

const CustomerTracking: React.FC = () => {
  const [searchParams] = useSearchParams();
  const pedidoId = searchParams.get('pedidoId');
  const [envio, setEnvio] = useState<Envio | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!pedidoId) {
      setError('Enlace de seguimiento inválido.');
      setLoading(false);
      return;
    }

    const fetchTracking = async () => {
      // Intentamos varias veces si el envío no se ha creado todavía (RabbitMQ async)
      let retries = 5;
      while (retries > 0) {
        try {
          const res = await api.get(`/envios/pedido/${pedidoId}`);
          if (res.data) {
            setEnvio(res.data);
            setLoading(false);
            return;
          }
        } catch (err: any) {
          if (err.response?.status !== 404) {
            setError('Error al obtener la información de seguimiento.');
            setLoading(false);
            return;
          }
        }
        await new Promise(resolve => setTimeout(resolve, 2000)); // wait 2 seconds
        retries--;
      }
      setError('No se pudo generar el tracking todavía o el pedido no existe. Intenta actualizar más tarde.');
      setLoading(false);
    };

    fetchTracking();
  }, [pedidoId]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-950">
        <div className="text-center space-y-4">
          <div className="w-16 h-16 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto"></div>
          <p className="text-slate-400 font-medium">Generando información de envío...</p>
        </div>
      </div>
    );
  }

  if (error || !envio) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-950 p-4">
        <div className="bg-slate-900 border border-white/10 p-8 rounded-3xl shadow-2xl max-w-md w-full text-center">
          <Search className="w-16 h-16 text-slate-500 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-white mb-2">Seguimiento no encontrado</h2>
          <p className="text-slate-400">{error}</p>
        </div>
      </div>
    );
  }

  // Lógica de progreso
  const steps = [
    { id: 'PENDING', label: 'Preparando', icon: Package },
    { id: 'DISPATCHED', label: 'En Camino', icon: Truck },
    { id: 'DELIVERED', label: 'Entregado', icon: CheckCircle },
  ];

  const currentStepIndex = steps.findIndex(s => s.id === envio.estadoEnvio);

  return (
    <div className="min-h-screen bg-slate-950 p-4 md:p-8 font-sans">
      <div className="max-w-3xl mx-auto">
        <div className="text-center mb-10 mt-8">
          <h1 className="text-4xl font-bold text-white tracking-tight mb-3">Seguimiento de tu Compra</h1>
          <p className="text-indigo-400 font-mono bg-indigo-500/10 inline-block px-4 py-1.5 rounded-full border border-indigo-500/20">
            {envio.trackingNumber}
          </p>
        </div>

        <div className="bg-slate-900/80 backdrop-blur-xl border border-white/10 rounded-3xl shadow-2xl p-6 md:p-10 mb-8">
          <div className="flex justify-between text-sm text-slate-400 mb-8 pb-6 border-b border-white/5">
            <div>
              <p className="uppercase tracking-wider text-xs font-semibold mb-1">ID Pedido</p>
              <p className="font-mono text-slate-200">#{envio.pedidoId}</p>
            </div>
            <div className="text-right">
              <p className="uppercase tracking-wider text-xs font-semibold mb-1">Fecha Est. de Entrega</p>
              <p className="font-medium text-slate-200">{new Date(envio.fechaEstimadaEntrega).toLocaleDateString('es-CL', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</p>
            </div>
          </div>

          <div className="relative">
            {/* Línea de progreso de fondo */}
            <div className="absolute top-8 left-10 right-10 h-1 bg-slate-800 rounded-full z-0"></div>
            {/* Línea de progreso activa */}
            <div 
              className="absolute top-8 left-10 h-1 bg-indigo-500 rounded-full z-0 transition-all duration-1000 ease-in-out"
              style={{ width: `${currentStepIndex === 0 ? 0 : currentStepIndex === 1 ? 50 : 100}%` }}
            ></div>

            <div className="flex justify-between relative z-10">
              {steps.map((step, index) => {
                const isActive = index <= currentStepIndex;
                const isCurrent = index === currentStepIndex;
                const Icon = step.icon;

                return (
                  <div key={step.id} className="flex flex-col items-center w-1/3">
                    <div className={`w-16 h-16 rounded-2xl flex items-center justify-center mb-4 transition-all duration-500 border-2 ${
                      isActive 
                        ? 'bg-indigo-600 border-indigo-500 shadow-[0_0_20px_rgba(79,70,229,0.4)]' 
                        : 'bg-slate-900 border-slate-700 text-slate-600'
                    } ${isCurrent ? 'scale-110' : 'scale-100'}`}>
                      <Icon className={`w-7 h-7 ${isActive ? 'text-white' : 'text-slate-600'}`} />
                    </div>
                    <p className={`font-semibold ${isActive ? 'text-white' : 'text-slate-500'}`}>{step.label}</p>
                    {isCurrent && (
                      <p className="text-xs text-indigo-400 mt-1 animate-pulse">Estado actual</p>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        </div>
        
        <div className="text-center">
          <p className="text-slate-500 text-sm">
            Si tienes dudas con tu envío, contáctanos indicando tu número de seguimiento <b>{envio.trackingNumber}</b>.
          </p>
        </div>
      </div>
    </div>
  );
};

export default CustomerTracking;
