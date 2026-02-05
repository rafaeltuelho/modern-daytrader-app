import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { DashboardLayout } from '../components/layout/DashboardLayout';
import { Card, Input, Button, Alert, Table, type Column } from '../components/ui';
import { accountApi } from '../api/account.api';
import { tradingApi } from '../api/trading.api';
import { updateProfileSchema } from '../utils/validators';
import type { UpdateProfileRequest } from '../types/account.types';
import type { OrderResponse } from '../types/trading.types';
import { formatCurrency, formatDateTime } from '../utils/formatters';

export const AccountPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [updateSuccess, setUpdateSuccess] = useState('');
  const [updateError, setUpdateError] = useState('');

  // Fetch account and profile
  const { data: account } = useQuery({
    queryKey: ['currentAccount'],
    queryFn: () => accountApi.getCurrentAccount(),
  });

  const { data: profile } = useQuery({
    queryKey: ['currentProfile'],
    queryFn: () => accountApi.getCurrentProfile(),
  });

  // Fetch order history
  const { data: orders, isLoading: ordersLoading } = useQuery({
    queryKey: ['orders'],
    queryFn: () => tradingApi.getOrders(),
  });

  // Profile update form
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<UpdateProfileRequest>({
    resolver: zodResolver(updateProfileSchema),
    values: profile
      ? {
          fullName: profile.fullName,
          email: profile.email,
          address: profile.address || '',
          creditCard: profile.creditCard || '',
        }
      : undefined,
  });

  // Update profile mutation
  const updateMutation = useMutation({
    mutationFn: (data: UpdateProfileRequest) => accountApi.updateCurrentProfile(data),
    onSuccess: () => {
      setUpdateSuccess('Profile updated successfully!');
      setUpdateError('');
      queryClient.invalidateQueries({ queryKey: ['currentProfile'] });
      queryClient.invalidateQueries({ queryKey: ['currentAccount'] });
    },
    onError: (error: any) => {
      setUpdateError(error.response?.data?.message || 'Failed to update profile');
      setUpdateSuccess('');
    },
  });

  const onSubmit = (data: UpdateProfileRequest) => {
    updateMutation.mutate(data);
  };

  const orderColumns: Column<OrderResponse>[] = [
    {
      header: 'Order ID',
      accessor: 'id',
      className: 'font-semibold',
    },
    {
      header: 'Type',
      accessor: (row) => (
        <span
          className={`px-2 py-1 rounded text-xs font-semibold ${
            row.orderType === 'buy' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
          }`}
        >
          {row.orderType.toUpperCase()}
        </span>
      ),
    },
    {
      header: 'Symbol',
      accessor: 'symbol',
      className: 'font-semibold',
    },
    {
      header: 'Quantity',
      accessor: (row) => row.quantity.toFixed(2),
    },
    {
      header: 'Price',
      accessor: (row) => row.price ? formatCurrency(row.price) : 'N/A',
    },
    {
      header: 'Total',
      accessor: (row) => row.price ? formatCurrency(row.orderFee + row.price * row.quantity) : 'N/A',
    },
    {
      header: 'Status',
      accessor: (row) => (
        <span
          className={`px-2 py-1 rounded text-xs font-semibold ${
            row.orderStatus === 'completed' || row.orderStatus === 'closed'
              ? 'bg-green-100 text-green-800'
              : row.orderStatus === 'processing' || row.orderStatus === 'open'
              ? 'bg-yellow-100 text-yellow-800'
              : 'bg-gray-100 text-gray-800'
          }`}
        >
          {row.orderStatus.toUpperCase()}
        </span>
      ),
    },
    {
      header: 'Date',
      accessor: (row) => formatDateTime(row.openDate),
    },
  ];

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <h1 className="text-3xl font-bold text-gray-900">Account</h1>

        {/* Account Details */}
        {account && (
          <Card title="Account Details">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <p className="text-sm text-gray-600">Account ID</p>
                <p className="text-lg font-semibold text-gray-900">{account.id}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Balance</p>
                <p className="text-lg font-semibold text-gray-900">
                  {formatCurrency(account.balance)}
                </p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Login Count</p>
                <p className="text-lg font-semibold text-gray-900">{account.loginCount}</p>
              </div>
            </div>
          </Card>
        )}

        {/* Profile Edit Form */}
        <Card title="Edit Profile">
          {updateSuccess && (
            <Alert variant="success" className="mb-4" onDismiss={() => setUpdateSuccess('')}>
              {updateSuccess}
            </Alert>
          )}
          {updateError && (
            <Alert variant="error" className="mb-4" onDismiss={() => setUpdateError('')}>
              {updateError}
            </Alert>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <Input
              label="Full Name"
              type="text"
              error={errors.fullName?.message}
              {...register('fullName')}
            />

            <Input
              label="Email"
              type="email"
              error={errors.email?.message}
              {...register('email')}
            />

            <Input
              label="Address"
              type="text"
              error={errors.address?.message}
              {...register('address')}
            />

            <Input
              label="Credit Card"
              type="text"
              placeholder="XXXX-XXXX-XXXX-XXXX"
              error={errors.creditCard?.message}
              {...register('creditCard')}
            />

            <Button type="submit" variant="primary" isLoading={updateMutation.isPending}>
              Update Profile
            </Button>
          </form>
        </Card>

        {/* Order History */}
        <Card title="Order History">
          <Table
            columns={orderColumns}
            data={orders || []}
            isLoading={ordersLoading}
            emptyMessage="No orders found"
          />
        </Card>
      </div>
    </DashboardLayout>
  );
};

