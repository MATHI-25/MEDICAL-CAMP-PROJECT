import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  Users,
  Building2,
  UserPlus,
  Stethoscope,
  Activity,
  Pill,
  FileSpreadsheet,
  BarChart3,
  Search,
} from 'lucide-react';

export const Sidebar = () => {
  const { user } = useAuth();
  const role = user?.role;

  const links = [
    {
      name: 'Admin Dashboard',
      path: '/admin',
      roles: ['SYSTEM_ADMIN'],
      icon: LayoutDashboard,
    },
    {
      name: 'User Management',
      path: '/admin/users',
      roles: ['SYSTEM_ADMIN'],
      icon: Users,
    },
    {
      name: 'Organizer Dashboard',
      path: '/organizer',
      roles: ['ORGANIZER', 'SYSTEM_ADMIN'],
      icon: Building2,
    },
    {
      name: 'Patient Registration',
      path: '/volunteer',
      roles: ['REGISTRATION_VOLUNTEER', 'SYSTEM_ADMIN'],
      icon: UserPlus,
    },
    {
      name: 'Nurse Vitals Recording',
      path: '/nurse',
      roles: ['NURSE', 'SYSTEM_ADMIN'],
      icon: Activity,
    },
    {
      name: 'Doctor Consultations',
      path: '/doctor',
      roles: ['DOCTOR', 'SYSTEM_ADMIN'],
      icon: Stethoscope,
    },
    {
      name: 'Pharmacy & Stock',
      path: '/pharmacy',
      roles: ['PHARMACY', 'SYSTEM_ADMIN'],
      icon: Pill,
    },
    {
      name: 'Reports & Analytics',
      path: '/reports',
      roles: ['SYSTEM_ADMIN', 'ORGANIZER', 'DOCTOR', 'PHARMACY'],
      icon: BarChart3,
    },
  ];

  const allowedLinks = links.filter((link) => link.roles.includes(role));

  return (
    <aside className="w-64 bg-slate-900 text-slate-300 min-h-[calc(100vh-4rem)] p-4 flex flex-col justify-between">
      <div className="space-y-1">
        <div className="px-3 py-2 text-xs font-bold text-slate-400 uppercase tracking-wider">
          Main Navigation
        </div>
        {allowedLinks.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `flex items-center space-x-3 px-3 py-2.5 rounded-xl text-sm font-semibold transition-all ${
                  isActive
                    ? 'bg-teal-600 text-white shadow-md shadow-teal-600/30'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800'
                }`
              }
            >
              <Icon className="w-5 h-5" />
              <span>{item.name}</span>
            </NavLink>
          );
        })}
      </div>

      <div className="p-3 bg-slate-800/60 rounded-xl border border-slate-800 text-xs text-slate-400 space-y-1">
        <div className="font-bold text-slate-200">MediQ Enterprise v1.0</div>
        <div>Member ID: <span className="text-teal-400 font-mono">{user?.memberId}</span></div>
      </div>
    </aside>
  );
};

export default Sidebar;
