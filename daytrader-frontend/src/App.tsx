import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { Layout, PrivateRoute } from './components';
import {
  LoginPage,
  RegisterPage,
  DashboardPage,
  PortfolioPage,
  TradePage,
  QuotesPage,
  AccountPage,
  OrderHistoryPage,
} from './pages';

function App() {
  const { isAuthenticated, userID, logout } = useAuth();

  return (
    <Layout isAuthenticated={isAuthenticated} username={userID || undefined} onLogout={logout}>
      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={
          isAuthenticated ? <Navigate to="/dashboard" replace /> : <LoginPage />
        } />
        <Route path="/register" element={
          isAuthenticated ? <Navigate to="/dashboard" replace /> : <RegisterPage />
        } />

        {/* Protected Routes */}
        <Route path="/dashboard" element={
          <PrivateRoute isAuthenticated={isAuthenticated}>
            <DashboardPage />
          </PrivateRoute>
        } />
        <Route path="/portfolio" element={
          <PrivateRoute isAuthenticated={isAuthenticated}>
            <PortfolioPage />
          </PrivateRoute>
        } />
        <Route path="/trade" element={
          <PrivateRoute isAuthenticated={isAuthenticated}>
            <TradePage />
          </PrivateRoute>
        } />
        <Route path="/quotes" element={
          <PrivateRoute isAuthenticated={isAuthenticated}>
            <QuotesPage />
          </PrivateRoute>
        } />
        <Route path="/account" element={
          <PrivateRoute isAuthenticated={isAuthenticated}>
            <AccountPage />
          </PrivateRoute>
        } />
        <Route path="/orders" element={
          <PrivateRoute isAuthenticated={isAuthenticated}>
            <OrderHistoryPage />
          </PrivateRoute>
        } />

        {/* Default Route */}
        <Route path="/" element={
          <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
        } />

        {/* Catch-all Route */}
        <Route path="*" element={
          <Navigate to="/" replace />
        } />
      </Routes>
    </Layout>
  );
}

export default App;
