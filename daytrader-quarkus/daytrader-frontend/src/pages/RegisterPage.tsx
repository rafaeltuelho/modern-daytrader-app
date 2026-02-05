import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { AuthLayout } from '../components/layout/AuthLayout';
import { Input, Button, Alert } from '../components/ui';
import { accountApi } from '../api/account.api';
import { registerSchema } from '../utils/validators';
import type { RegisterRequest } from '../types/account.types';
import { useAuthStore } from '../stores/authStore';

type RegisterFormData = RegisterRequest & { confirmPassword: string };

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuthStore();
  const [error, setError] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      openBalance: 10000,
    },
  });

  const onSubmit = async (data: RegisterFormData) => {
    setError('');
    setIsLoading(true);

    try {
      // Remove confirmPassword before sending to API
      const { confirmPassword, ...registerData } = data;
      await accountApi.register(registerData);

      // Auto-login after registration
      await login({
        userId: data.userId,
        password: data.password,
      });

      navigate('/', { replace: true });
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout>
      <h2 className="text-2xl font-bold text-center mb-6">Create Account</h2>

      {error && (
        <Alert variant="error" className="mb-4">
          {error}
        </Alert>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <Input
          label="User ID"
          type="text"
          placeholder="Choose a user ID"
          error={errors.userId?.message}
          {...register('userId')}
        />

        <Input
          label="Password"
          type="password"
          placeholder="Choose a password"
          error={errors.password?.message}
          {...register('password')}
        />

        <Input
          label="Confirm Password"
          type="password"
          placeholder="Confirm your password"
          error={errors.confirmPassword?.message}
          {...register('confirmPassword')}
        />

        <Input
          label="Full Name"
          type="text"
          placeholder="Enter your full name"
          error={errors.fullName?.message}
          {...register('fullName')}
        />

        <Input
          label="Email"
          type="email"
          placeholder="Enter your email"
          error={errors.email?.message}
          {...register('email')}
        />

        <Input
          label="Address (Optional)"
          type="text"
          placeholder="Enter your address"
          error={errors.address?.message}
          {...register('address')}
        />

        <Input
          label="Credit Card (Optional)"
          type="text"
          placeholder="XXXX-XXXX-XXXX-XXXX"
          error={errors.creditCard?.message}
          {...register('creditCard')}
        />

        <Input
          label="Opening Balance"
          type="number"
          step="0.01"
          placeholder="10000.00"
          error={errors.openBalance?.message}
          {...register('openBalance', { valueAsNumber: true })}
        />

        <Button
          type="submit"
          variant="primary"
          className="w-full"
          isLoading={isLoading}
        >
          Create Account
        </Button>
      </form>

      <div className="mt-6 text-center">
        <p className="text-sm text-gray-600">
          Already have an account?{' '}
          <Link to="/login" className="text-primary-600 hover:text-primary-700 font-medium">
            Sign in here
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
};

