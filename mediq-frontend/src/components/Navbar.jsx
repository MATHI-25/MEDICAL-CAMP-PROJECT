import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Search, LogOut, User as UserIcon, Shield, Activity, Bell } from 'lucide-react';
import reportService from '../services/reportService';

export const Navbar = ({ onSearch }) => {
  const { user, logout } = useAuth();
  const [query, setQuery] = useState('');

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (onSearch && query.trim()) {
      onSearch(query.trim());
    }
  };

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-40 shadow-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        
        {/* Left: Brand Logo */}
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-teal-600 to-emerald-500 flex items-center justify-center text-white shadow-md shadow-teal-500/20">
            <Activity className="w-6 h-6 stroke-[2.5]" />
          </div>
          <div>
            <span className="text-xl font-black bg-gradient-to-r from-teal-700 to-emerald-600 bg-clip-text text-transparent tracking-tight">
              MediQ
            </span>
            <span className="hidden sm:inline-block ml-2 text-xs font-semibold text-teal-600 bg-teal-50 px-2 py-0.5 rounded-full border border-teal-100">
              Medical Camp Portal
            </span>
          </div>
        </div>

        {/* Center: Global Search Bar */}
        <div className="flex-1 max-w-md mx-6 hidden md:block">
          <form onSubmit={handleSearchSubmit} className="relative">
            <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              placeholder="Search Patient ID, Token, Phone, Member ID..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500 transition-all placeholder:text-slate-400"
            />
          </form>
        </div>

        {/* Right: User Profile & Actions */}
        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-3 pl-3 border-l border-slate-200">
            <div className="hidden sm:block text-right">
              <div className="text-sm font-bold text-slate-800 leading-none mb-1">{user?.fullName || 'Health Specialist'}</div>
              <div className="text-xs font-medium text-teal-600 flex items-center justify-end">
                <Shield className="w-3 h-3 mr-1" />
                {user?.role ? user.role.replace(/_/g, ' ') : 'USER'}
              </div>
            </div>

            <div className="w-9 h-9 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center text-slate-600 font-bold text-sm">
              {user?.fullName ? user.fullName.charAt(0) : 'U'}
            </div>

            <button
              onClick={logout}
              title="Logout"
              className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-xl transition-colors"
            >
              <LogOut className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
