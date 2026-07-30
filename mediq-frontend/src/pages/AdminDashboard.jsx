import React, { useState, useEffect } from 'react';
import userService from '../services/userService';
import campService from '../services/campService';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import { Users, Building2, UserPlus, KeyRound, Shield, Search } from 'lucide-react';
import AnimatedHeroBanner from '../components/AnimatedHeroBanner';

export const AdminDashboard = () => {
  const [users, setUsers] = useState([]);
  const [camps, setCamps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  // Modals
  const [isAddUserOpen, setIsAddUserOpen] = useState(false);
  const [isResetPassOpen, setIsResetPassOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);

  // New User Form
  const [newUser, setNewUser] = useState({
    memberId: '',
    password: 'Camp@2026',
    fullName: '',
    email: '',
    phone: '',
    role: 'DOCTOR',
    specialization: 'General Medicine',
    department: 'Outpatient Department',
  });

  const [newPassword, setNewPassword] = useState('Camp@2026');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const uRes = await userService.searchUsers(null, search, 0, 50);
      const uList = uRes.data?.content || uRes.content || uRes.data || [];
      setUsers(Array.isArray(uList) ? uList : []);
      const cRes = await campService.searchCamps(null, '', 0, 10);
      const cList = cRes.data?.content || cRes.content || cRes.data || [];
      setCamps(Array.isArray(cList) ? cList : []);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateUser = async (e) => {
    e.preventDefault();
    try {
      await userService.createUser(newUser);
      setIsAddUserOpen(false);
      fetchData();
      alert('System Staff User created successfully!');
    } catch (e) {
      alert(e?.message || e?.response?.data?.message || 'Failed to create user');
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    if (!selectedUser) return;
    try {
      await userService.resetPassword(selectedUser.id, newPassword);
      setIsResetPassOpen(false);
      alert('Password reset successfully!');
    } catch (e) {
      alert(e?.message || e?.response?.data?.message || 'Failed to reset password');
    }
  };

  const handleToggleUserStatus = async (user) => {
    try {
      if (user.isActive) {
        await userService.deactivateUser(user.id);
      } else {
        await userService.activateUser(user.id);
      }
      fetchData();
    } catch (e) {
      alert(e?.message || e?.response?.data?.message || 'Failed to update user status');
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Action Bar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-4 rounded-2xl border border-slate-100 shadow-xs">
        <div>
          <h2 className="text-sm font-bold text-slate-700">System Admin Security Hub</h2>
          <p className="text-slate-400 text-xs">Provision accounts & configure medical camp access</p>
        </div>
        <button
          onClick={() => setIsAddUserOpen(true)}
          className="px-4 py-2 bg-gradient-to-r from-blue-600 to-indigo-600 hover:opacity-95 text-white font-bold text-xs rounded-xl shadow-md shadow-blue-600/20 flex items-center space-x-2 transition-all"
        >
          <UserPlus className="w-4 h-4" />
          <span>Add System Staff User</span>
        </button>
      </div>

      {/* Hero Animated Banner */}
      <AnimatedHeroBanner
        type="admin"
        stats={[
          { label: 'Total Registered Staff', value: `${users.length} Users` },
          { label: 'System Camps', value: `${camps.length} Camps` }
        ]}
      />

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-xs flex items-center space-x-4">
          <div className="p-3 bg-teal-50 text-teal-600 rounded-xl">
            <Users className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-black text-slate-800">{users.length}</div>
            <div className="text-xs font-semibold text-slate-500">Total Staff Users</div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-xs flex items-center space-x-4">
          <div className="p-3 bg-emerald-50 text-emerald-600 rounded-xl">
            <Building2 className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-black text-slate-800">{camps.length}</div>
            <div className="text-xs font-semibold text-slate-500">Registered Medical Camps</div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-xs flex items-center space-x-4">
          <div className="p-3 bg-amber-50 text-amber-600 rounded-xl">
            <Shield className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-black text-slate-800">
              {users.filter((u) => u.role === 'DOCTOR').length}
            </div>
            <div className="text-xs font-semibold text-slate-500">Active Doctors</div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-xs flex items-center space-x-4">
          <div className="p-3 bg-sky-50 text-sky-600 rounded-xl">
            <KeyRound className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-black text-slate-800">6 Roles</div>
            <div className="text-xs font-semibold text-slate-500">Role Permissions</div>
          </div>
        </div>
      </div>

      {/* User Management Table */}
      <div className="bg-white rounded-2xl border border-slate-100 shadow-xs overflow-hidden">
        <div className="p-6 border-b border-slate-100 flex justify-between items-center">
          <h2 className="text-lg font-bold text-slate-800">System User Directory</h2>
          <div className="relative w-64">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              placeholder="Filter by name or member ID..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-teal-500"
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-100 text-xs font-bold text-slate-400 uppercase tracking-wider">
                <th className="py-3 px-6">Member ID</th>
                <th className="py-3 px-6">Full Name</th>
                <th className="py-3 px-6">Role</th>
                <th className="py-3 px-6">Department</th>
                <th className="py-3 px-6">Status</th>
                <th className="py-3 px-6 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-sm">
              {users.map((u) => (
                <tr key={u.id} className="hover:bg-slate-50/50">
                  <td className="py-4 px-6 font-mono font-bold text-teal-700">{u.memberId}</td>
                  <td className="py-4 px-6 font-bold text-slate-800">{u.fullName}</td>
                  <td className="py-4 px-6">
                    <span className="px-2 py-0.5 rounded-full text-xs font-bold bg-slate-100 text-slate-700">
                      {u.role}
                    </span>
                  </td>
                  <td className="py-4 px-6 text-slate-600">{u.department || 'N/A'}</td>
                  <td className="py-4 px-6">
                    <StatusBadge status={u.isActive ? 'ACTIVE' : 'DEACTIVATED'} />
                  </td>
                  <td className="py-4 px-6 text-right space-x-2">
                    <button
                      onClick={() => {
                        setSelectedUser(u);
                        setIsResetPassOpen(true);
                      }}
                      className="px-2.5 py-1 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold text-xs rounded-lg transition-colors"
                    >
                      Reset Pass
                    </button>
                    <button
                      onClick={() => handleToggleUserStatus(u)}
                      className={`px-2.5 py-1 font-semibold text-xs rounded-lg transition-colors ${
                        u.isActive
                          ? 'bg-rose-50 text-rose-700 hover:bg-rose-100'
                          : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100'
                      }`}
                    >
                      {u.isActive ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add User Modal */}
      <Modal isOpen={isAddUserOpen} onClose={() => setIsAddUserOpen(false)} title="Create New System User">
        <form onSubmit={handleCreateUser} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Member ID</label>
              <input
                type="text"
                required
                value={newUser.memberId}
                onChange={(e) => setNewUser({ ...newUser, memberId: e.target.value })}
                placeholder="e.g. MC-DOC-002"
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Password</label>
              <input
                type="password"
                required
                value={newUser.password}
                onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">Full Name</label>
            <input
              type="text"
              required
              value={newUser.fullName}
              onChange={(e) => setNewUser({ ...newUser, fullName: e.target.value })}
              placeholder="Dr. John Doe"
              className="w-full px-3 py-2 text-sm border rounded-xl"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Email</label>
              <input
                type="email"
                value={newUser.email}
                onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Phone</label>
              <input
                type="text"
                value={newUser.phone}
                onChange={(e) => setNewUser({ ...newUser, phone: e.target.value })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">User Role</label>
            <select
              value={newUser.role}
              onChange={(e) => setNewUser({ ...newUser, role: e.target.value })}
              className="w-full px-3 py-2 text-sm border rounded-xl"
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
            className="w-full py-3 bg-teal-600 text-white font-bold rounded-xl shadow-md hover:bg-teal-700 mt-2"
          >
            Create Staff User
          </button>
        </form>
      </Modal>

      {/* Reset Password Modal */}
      <Modal isOpen={isResetPassOpen} onClose={() => setIsResetPassOpen(false)} title="Reset User Password">
        <form onSubmit={handleResetPassword} className="space-y-4">
          <p className="text-xs text-slate-600 font-medium">
            Reset password for user <span className="font-bold text-slate-800">{selectedUser?.fullName}</span> ({selectedUser?.memberId})
          </p>
          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">New Password</label>
            <input
              type="password"
              required
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              className="w-full px-3 py-2 text-sm border rounded-xl"
            />
          </div>
          <button
            type="submit"
            className="w-full py-2.5 bg-teal-600 text-white font-bold text-sm rounded-xl shadow-md hover:bg-teal-700"
          >
            Confirm Password Reset
          </button>
        </form>
      </Modal>
    </div>
  );
};

export default AdminDashboard;
