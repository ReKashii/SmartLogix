import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Lock, Mail, AlertCircle, Package } from 'lucide-react';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (email === 'admin@smartlogix.cl' && password === 'admin') {
      localStorage.setItem('token', 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBzbWFydGxvZ2l4LmNsIiwibmFtZSI6IlJlbmF0byIsInJvbGUiOiJBRE1JTiIsImV4cCI6MTg5MzQ1NjAwMH0.i3WTqIDcdmdsFb-IGOItm_pvQpRhdL_l3I1eQ9DddPU');
      navigate('/dashboard');
    } else {
      setError('Credenciales incorrectas. Intente nuevamente.');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-slate-950 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-indigo-900 via-slate-950 to-slate-950">
      
      {/* Decorative background blurs */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-indigo-500/20 rounded-full blur-[120px] pointer-events-none"></div>
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-purple-500/20 rounded-full blur-[120px] pointer-events-none"></div>

      <div className="max-w-md w-full relative z-10">
        
        {/* Logo Area */}
        <div className="text-center mb-8 flex flex-col items-center">
          <div className="inline-flex p-4 bg-white/5 backdrop-blur-md rounded-2xl border border-white/10 shadow-2xl mb-4">
            <Package className="w-10 h-10 text-indigo-400" />
          </div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight">SmartLogix</h1>
          <p className="text-indigo-200 mt-2 font-medium tracking-wide">Enterprise Logistics</p>
        </div>

        {/* Login Card (Glassmorphism) */}
        <div className="bg-slate-900/60 backdrop-blur-xl rounded-3xl shadow-2xl overflow-hidden border border-white/10">
          <div className="p-8">
            <h2 className="text-xl font-bold text-white mb-6">Iniciar Sesión</h2>

            {error && (
              <div className="mb-6 p-4 bg-red-500/10 border border-red-500/50 rounded-xl flex items-center gap-3 animate-in slide-in-from-top-2 duration-300">
                <AlertCircle className="w-5 h-5 text-red-400" />
                <p className="text-sm font-medium text-red-200">{error}</p>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Correo Electrónico</label>
                <div className="relative group">
                  <span className="absolute inset-y-0 left-0 pl-4 flex items-center text-slate-500 group-focus-within:text-indigo-400 transition-colors">
                    <Mail className="w-5 h-5" />
                  </span>
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full pl-12 pr-4 py-3 bg-slate-950/50 border border-white/10 rounded-xl focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-400 outline-none transition-all text-white placeholder-slate-500"
                    placeholder="admin@smartlogix.cl"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Contraseña</label>
                <div className="relative group">
                  <span className="absolute inset-y-0 left-0 pl-4 flex items-center text-slate-500 group-focus-within:text-indigo-400 transition-colors">
                    <Lock className="w-5 h-5" />
                  </span>
                  <input
                    type="password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="w-full pl-12 pr-4 py-3 bg-slate-950/50 border border-white/10 rounded-xl focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-400 outline-none transition-all text-white placeholder-slate-500"
                    placeholder="••••••••"
                  />
                </div>
              </div>

              <button
                type="submit"
                className="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-semibold py-3 rounded-xl transition-all duration-300 shadow-[0_0_20px_rgba(79,70,229,0.3)] hover:shadow-[0_0_30px_rgba(79,70,229,0.5)] active:scale-[0.98]"
              >
                Acceder al Panel
              </button>
            </form>
          </div>
        </div>
        
        <p className="text-center mt-8 text-sm text-slate-500">
          SmartLogix v2.0 &copy; {new Date().getFullYear()}
        </p>
      </div>
    </div>
  );
};

export default Login;
