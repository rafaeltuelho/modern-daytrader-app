import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { LoginForm } from '../components';
import { authApi } from '../api';
import { useAuth } from '../context/AuthContext';

export function LoginPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/dashboard';

  const handleSubmit = async (username: string, password: string, _rememberMe: boolean) => {
    setIsLoading(true);
    setError('');

    try {
      const response = await authApi.login({ userID: username, password });
      login(response);
      navigate(from, { replace: true });
    } catch (err: unknown) {
      const apiError = err as { message?: string; error?: string };
      setError(apiError?.message || apiError?.error || 'Login failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center">
      <LoginForm 
        onSubmit={handleSubmit} 
        isLoading={isLoading} 
        error={error} 
      />
    </div>
  );
}

