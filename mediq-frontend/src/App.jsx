import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import MainLayout from './layouts/MainLayout';

import LoginPage from './pages/LoginPage';
import AdminDashboard from './pages/AdminDashboard';
import OrganizerDashboard from './pages/OrganizerDashboard';
import VolunteerDashboard from './pages/VolunteerDashboard';
import NurseDashboard from './pages/NurseDashboard';
import DoctorDashboard from './pages/DoctorDashboard';
import PharmacyDashboard from './pages/PharmacyDashboard';
import ReportsPage from './pages/ReportsPage';

const RoleBasedRedirect = () => {
  const { user, isAuthenticated } = useAuth();

  if (!isAuthenticated) return <Navigate to="/login" replace />;

  switch (user?.role) {
    case 'SYSTEM_ADMIN':
      return <Navigate to="/admin" replace />;
    case 'ORGANIZER':
      return <Navigate to="/organizer" replace />;
    case 'REGISTRATION_VOLUNTEER':
      return <Navigate to="/volunteer" replace />;
    case 'NURSE':
      return <Navigate to="/nurse" replace />;
    case 'DOCTOR':
      return <Navigate to="/doctor" replace />;
    case 'PHARMACY':
      return <Navigate to="/pharmacy" replace />;
    default:
      return <Navigate to="/login" replace />;
  }
};

export function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Login Route */}
          <Route path="/login" element={<LoginPage />} />

          {/* Protected Enterprise Workspace Routes */}
          <Route element={<ProtectedRoute />}>
            <Route element={<MainLayout />}>
              <Route path="/" element={<RoleBasedRedirect />} />

              {/* System Admin Routes */}
              <Route element={<ProtectedRoute allowedRoles={['SYSTEM_ADMIN']} />}>
                <Route path="/admin" element={<AdminDashboard />} />
                <Route path="/admin/users" element={<AdminDashboard />} />
              </Route>

              {/* Organizer Routes */}
              <Route element={<ProtectedRoute allowedRoles={['ORGANIZER', 'SYSTEM_ADMIN']} />}>
                <Route path="/organizer" element={<OrganizerDashboard />} />
              </Route>

              {/* Volunteer Routes */}
              <Route element={<ProtectedRoute allowedRoles={['REGISTRATION_VOLUNTEER', 'ORGANIZER', 'SYSTEM_ADMIN']} />}>
                <Route path="/volunteer" element={<VolunteerDashboard />} />
              </Route>

              {/* Nurse Routes */}
              <Route element={<ProtectedRoute allowedRoles={['NURSE', 'SYSTEM_ADMIN']} />}>
                <Route path="/nurse" element={<NurseDashboard />} />
              </Route>

              {/* Doctor Routes */}
              <Route element={<ProtectedRoute allowedRoles={['DOCTOR', 'SYSTEM_ADMIN']} />}>
                <Route path="/doctor" element={<DoctorDashboard />} />
              </Route>

              {/* Pharmacy Routes */}
              <Route element={<ProtectedRoute allowedRoles={['PHARMACY', 'SYSTEM_ADMIN']} />}>
                <Route path="/pharmacy" element={<PharmacyDashboard />} />
              </Route>

              {/* Reports & Analytics */}
              <Route element={<ProtectedRoute allowedRoles={['SYSTEM_ADMIN', 'ORGANIZER', 'DOCTOR', 'PHARMACY']} />}>
                <Route path="/reports" element={<ReportsPage />} />
              </Route>
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
