import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { RegisterForm, type RegisterData } from '../components';
import { authApi } from '../api';

export function RegisterPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (data: RegisterData) => {
    setIsLoading(true);
    setError('');

    try {
      await authApi.register({
        userID: data.userId,
        password: data.password,
        fullName: data.fullName,
        email: data.email,
        address: data.address,
        creditCard: data.creditCard,
        openBalance: data.openBalance,
      });
      // Redirect to login after successful registration
      navigate('/login', { 
        state: { message: 'Registration successful! Please log in.' } 
      });
    } catch (err: unknown) {
      const apiError = err as { message?: string; error?: string };
      setError(apiError?.message || apiError?.error || 'Registration failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center py-8">
      <RegisterForm 
        onSubmit={handleSubmit} 
        isLoading={isLoading} 
        error={error} 
      />
    </div>
  );
}

