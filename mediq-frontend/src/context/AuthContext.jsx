import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedToken = localStorage.getItem('mediq_token');
    const storedUser = localStorage.getItem('mediq_user');

    if (storedToken && storedUser && typeof storedToken === 'string' && storedToken.split('.').length === 3) {
      try {
        setToken(storedToken);
        const parsed = JSON.parse(storedUser);
        const normalized = {
          ...parsed,
          id: parsed.userId || parsed.id,
          userId: parsed.userId || parsed.id,
        };
        setUser(normalized);
      } catch (e) {
        console.error('Failed to parse stored user context', e);
        localStorage.removeItem('mediq_token');
        localStorage.removeItem('mediq_user');
      }
    } else if (storedToken || storedUser) {
      // Clear corrupt token/user data
      localStorage.removeItem('mediq_token');
      localStorage.removeItem('mediq_user');
    }
    setLoading(false);
  }, []);

  const login = (tokenData, userData) => {
    const jwtToken = typeof tokenData === 'string' ? tokenData : tokenData?.accessToken;
    if (jwtToken) {
      const normalizedUser = {
        ...userData,
        id: userData.userId || userData.id,
        userId: userData.userId || userData.id,
      };
      localStorage.setItem('mediq_token', jwtToken);
      localStorage.setItem('mediq_user', JSON.stringify(normalizedUser));
      setToken(jwtToken);
      setUser(normalizedUser);
    }
  };

  const logout = () => {
    localStorage.removeItem('mediq_token');
    localStorage.removeItem('mediq_user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
