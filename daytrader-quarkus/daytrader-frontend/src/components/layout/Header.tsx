import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { TrendingUp, LogOut, User } from 'lucide-react';
import { useAuthStore } from '../../stores/authStore';
import { Button } from '../ui';

export const Header: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="bg-primary-700 text-white shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2 hover:opacity-80 transition-opacity">
            <TrendingUp className="h-8 w-8" />
            <span className="text-2xl font-bold">DayTrader</span>
          </Link>

          {/* Navigation */}
          {isAuthenticated && (
            <nav className="hidden md:flex items-center space-x-6">
              <Link to="/" className="hover:text-primary-200 transition-colors">
                Home
              </Link>
              <Link to="/portfolio" className="hover:text-primary-200 transition-colors">
                Portfolio
              </Link>
              <Link to="/quotes" className="hover:text-primary-200 transition-colors">
                Quotes
              </Link>
              <Link to="/market" className="hover:text-primary-200 transition-colors">
                Market
              </Link>
              <Link to="/account" className="hover:text-primary-200 transition-colors">
                Account
              </Link>
            </nav>
          )}

          {/* User menu */}
          {isAuthenticated && user && (
            <div className="flex items-center space-x-4">
              <div className="hidden md:flex items-center space-x-2">
                <User className="h-5 w-5" />
                <span className="text-sm">{user.userId}</span>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={handleLogout}
                className="text-white hover:bg-primary-600"
              >
                <LogOut className="h-4 w-4 mr-2" />
                Logout
              </Button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

