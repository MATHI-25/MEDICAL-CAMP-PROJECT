import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import authService from '../services/authService';
import { Activity, ShieldCheck, Lock, User as UserIcon, Sparkles } from 'lucide-react';
import AnimatedHeroBanner from '../components/AnimatedHeroBanner';

export const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [memberId, setMemberId] = useState('MC-ADM-001');
  const [password, setPassword] = useState('Camp@2026');
  const [role, setRole] = useState('SYSTEM_ADMIN');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const demoCredentials = [
    { role: 'SYSTEM_ADMIN', label: 'Admin', id: 'MC-ADM-001' },
    { role: 'ORGANIZER', label: 'Organizer', id: 'MC-ORG-001' },
    { role: 'DOCTOR', label: 'Doctor', id: 'MC-DOC-001' },
    { role: 'NURSE', label: 'Nurse', id: 'MC-NUR-001' },
    { role: 'PHARMACY', label: 'Pharmacy', id: 'MC-PHA-001' },
    { role: 'REGISTRATION_VOLUNTEER', label: 'Volunteer', id: 'MC-REG-001' },
  ];

  const handleSelectDemo = (item) => {
    setMemberId(item.id);
    setPassword('Camp@2026');
    setRole(item.role);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const res = await authService.login(memberId, password, role);
      setLoading(false);

      const authPayload = res?.data || res;
      if (authPayload && authPayload.accessToken) {
        login(authPayload.accessToken, authPayload);
        navigate('/');
      } else {
        setError(res?.message || 'Login failed: invalid response token');
      }
    } catch (err) {
      setLoading(false);
      setError(err?.message || 'Invalid member ID, password, or role');
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-center p-4 md:p-8">
      <div className="max-w-6xl w-full mx-auto grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
        
        {/* Left Side Animated Banner */}
        <div className="lg:col-span-7">
          <AnimatedHeroBanner
            type="login"
            title="MediQ Healthcare & Medical Camp System"
            subtitle="An end-to-end digital prescription, queue token management, and pharmacy inventory management platform tailored for medical relief camps."
            badgeText="Enterprise Portal 2026"
            stats={[
              { label: 'Active Camps', value: '12 Live' },
              { label: 'Consultations Today', value: '450+' },
              { label: 'System Uptime', value: '99.9%' }
            ]}
          />
        </div>

        {/* Right Side Login Card */}
        <div className="lg:col-span-5">
          <div className="bg-white rounded-3xl shadow-2xl p-8 border border-slate-800/20 backdrop-blur">
            
            {/* Header */}
            <div className="text-center mb-6">
              <div className="w-14 h-14 bg-gradient-to-tr from-teal-600 to-emerald-500 rounded-2xl flex items-center justify-center text-white mx-auto mb-3 shadow-lg shadow-teal-500/30">
                <Activity className="w-8 h-8 stroke-[2.5]" />
              </div>
              <h2 className="text-2xl font-black text-slate-800 tracking-tight">Sign In to MediQ</h2>
              <p className="text-slate-500 text-xs mt-1">Select your account role and enter credentials</p>
            </div>

            {/* Quick Demo Presets */}
            <div className="mb-5">
              <div className="flex items-center justify-between mb-2">
                <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">
                  Quick Demo Preset
                </label>
                <span className="text-[10px] bg-teal-100 text-teal-800 font-bold px-2 py-0.5 rounded-full flex items-center gap-1">
                  <Sparkles className="w-2.5 h-2.5" /> 1-Click Fill
                </span>
              </div>
              <div className="grid grid-cols-3 gap-2">
                {demoCredentials.map((item) => (
                  <button
                    key={item.role}
                    type="button"
                    onClick={() => handleSelectDemo(item)}
                    className={`py-2 px-2 text-xs font-bold rounded-xl border transition-all ${
                      role === item.role
                        ? 'bg-teal-50 text-teal-700 border-teal-500 ring-2 ring-teal-500/20 shadow-sm'
                        : 'bg-slate-50 text-slate-600 border-slate-200 hover:bg-slate-100'
                    }`}
                  >
                    {item.label}
                  </button>
                ))}
              </div>
            </div>

            {error && (
              <div className="mb-4 p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs font-semibold rounded-xl text-center animate-shake">
                {error}
              </div>
            )}

            {/* Form */}
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Member ID</label>
                <div className="relative">
                  <UserIcon className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
                  <input
                    type="text"
                    required
                    value={memberId}
                    onChange={(e) => setMemberId(e.target.value)}
                    placeholder="e.g. MC-ADM-001"
                    className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm font-semibold text-slate-800 focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Password</label>
                <div className="relative">
                  <Lock className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
                  <input
                    type="password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm font-semibold text-slate-800 focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Select Role Context</label>
                <select
                  value={role}
                  onChange={(e) => setRole(e.target.value)}
                  className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm font-semibold text-slate-800 focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500"
                >
                  <option value="SYSTEM_ADMIN">SYSTEM ADMINISTRATOR</option>
                  <option value="ORGANIZER">CAMP ORGANIZER</option>
                  <option value="DOCTOR">DOCTOR</option>
                  <option value="NURSE">NURSE</option>
                  <option value="PHARMACY">PHARMACIST</option>
                  <option value="REGISTRATION_VOLUNTEER">REGISTRATION VOLUNTEER</option>
                </select>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 bg-gradient-to-r from-teal-600 to-emerald-600 text-white font-bold rounded-xl shadow-lg shadow-teal-600/30 hover:opacity-95 transition-all flex items-center justify-center space-x-2 disabled:opacity-50 mt-2"
              >
                {loading ? (
                  <span className="text-sm">Authenticating...</span>
                ) : (
                  <>
                    <ShieldCheck className="w-5 h-5" />
                    <span>Sign In to Dashboard</span>
                  </>
                )}
              </button>
            </form>
          </div>
        </div>

      </div>
    </div>
  );
};

export default LoginPage;
