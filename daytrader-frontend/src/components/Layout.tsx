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
    <div className="min-h-screen flex flex-col bg-gray-50">
      <Navbar 
        isAuthenticated={isAuthenticated} 
        username={username} 
        onLogout={onLogout} 
      />
      
      <main className="flex-grow container mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>
      
      <footer className="bg-gray-800 text-white py-4">
        <div className="container mx-auto px-4 text-center text-sm">
          <p>&copy; {new Date().getFullYear()} DayTrader. All rights reserved.</p>
          <p className="text-gray-400 mt-1">
            Modernized from IBM DayTrader7 Sample Application
          </p>
        </div>
      </footer>
    </div>
  );
}

