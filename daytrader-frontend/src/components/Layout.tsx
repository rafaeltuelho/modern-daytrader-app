import type { ReactNode } from 'react';
import { Navbar } from './Navbar';

interface LayoutProps {
  children: ReactNode;
  isAuthenticated: boolean;
  username?: string;
  onLogout: () => void;
}

export function Layout({ children, isAuthenticated, username, onLogout }: LayoutProps) {
  return (
    <div className="min-h-screen flex flex-col bg-[#0D0D0D]">
      <Navbar
        isAuthenticated={isAuthenticated}
        username={username}
        onLogout={onLogout}
      />

      <main className="flex-grow container mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>

      <footer className="bg-[#16213E]/80 backdrop-blur-sm border-t border-white/5 text-white py-6">
        <div className="container mx-auto px-4 text-center text-sm">
          <p className="text-gray-300">&copy; {new Date().getFullYear()} DayTrader Pro. All rights reserved.</p>
          <p className="text-gray-500 mt-1">
            Professional Trading Platform
          </p>
        </div>
      </footer>
    </div>
  );
}

