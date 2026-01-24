import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { getToken, removeToken } from '../api/client';
import type { LoginResponse } from '../types';

interface AuthContextType {
  isAuthenticated: boolean;
  userID: string | null;
  token: string | null;
  login: (response: LoginResponse) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userID, setUserID] = useState<string | null>(null);
  const [token, setToken] = useState<string | null>(null);

  // Check for existing token on mount
  useEffect(() => {
    const storedToken = getToken();
    if (storedToken) {
      setToken(storedToken);
      setIsAuthenticated(true);
      // Extract userID from token or localStorage if needed
      const storedUserID = localStorage.getItem('daytrader_user');
      if (storedUserID) {
        setUserID(storedUserID);
      }
    }
  }, []);

  const login = (response: LoginResponse) => {
    setToken(response.token);
    setUserID(response.userID);
    setIsAuthenticated(true);
    localStorage.setItem('daytrader_user', response.userID);
  };

  const logout = () => {
    removeToken();
    localStorage.removeItem('daytrader_user');
    setToken(null);
    setUserID(null);
    setIsAuthenticated(false);
  };

  const value: AuthContextType = {
    isAuthenticated,
    userID,
    token,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

export default AuthContext;

