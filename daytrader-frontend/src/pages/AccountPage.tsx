import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  TextField,
  Button,
  Alert,
  Divider,
  Grid
} from '@mui/material';
import { Save as SaveIcon, Edit as EditIcon, Cancel as CancelIcon } from '@mui/icons-material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuth } from '../store/AuthContext';
import { useAccountProfile, useUpdateProfile } from '../hooks/useAccountProfile';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorAlert } from '../components/ErrorAlert';

// Validation schema
const profileSchema = z.object({
  fullName: z.string().min(1, 'Full name is required'),
  email: z.string().email('Invalid email address'),
  address: z.string().optional(),
  creditCard: z.string().optional(),
});

type ProfileFormData = z.infer<typeof profileSchema>;

export const AccountPage: React.FC = () => {
  const { user } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  const { data: profile, isLoading, error, refetch } = useAccountProfile();
  const updateProfile = useUpdateProfile();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<ProfileFormData>({
    resolver: zodResolver(profileSchema),
  });

  // Reset form when profile data loads
  useEffect(() => {
    if (profile) {
      reset({
        fullName: profile.fullName,
        email: profile.email,
        address: profile.address || '',
        creditCard: profile.creditCard || '',
      });
    }
  }, [profile, reset]);

  const onSubmit = async (data: ProfileFormData) => {
    try {
      setSuccessMessage('');
      await updateProfile.mutateAsync(data);
      setSuccessMessage('Profile updated successfully!');
      setIsEditing(false);

      // Clear success message after 5 seconds
      setTimeout(() => setSuccessMessage(''), 5000);
    } catch (err) {
      // Error is handled by the mutation
      console.error('Failed to update profile:', err);
    }
  };

  const handleCancel = () => {
    reset();
    setIsEditing(false);
    setSuccessMessage('');
  };

  if (isLoading) {
    return (
      <Box>
        <Typography variant="h4" gutterBottom fontWeight={600}>
          Account Profile
        </Typography>
        <LoadingSpinner message="Loading profile..." />
      </Box>
    );
  }

  if (error) {
    return (
      <Box>
        <Typography variant="h4" gutterBottom fontWeight={600}>
          Account Profile
        </Typography>
        <ErrorAlert message="Failed to load account profile" onRetry={refetch} />
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom fontWeight={600}>
        Account Profile
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Manage your account information
      </Typography>

      {successMessage && (
        <Alert severity="success" sx={{ mb: 3 }} onClose={() => setSuccessMessage('')}>
          {successMessage}
        </Alert>
      )}

      {updateProfile.isError && (
        <Alert severity="error" sx={{ mb: 3 }}>
          Failed to update profile. Please try again.
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* Account Information (Read-only) */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" fontWeight={600} gutterBottom>
                Account Information
              </Typography>
              <Divider sx={{ mb: 2 }} />

              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  User ID
                </Typography>
                <Typography variant="body1" fontWeight={500}>
                  {user?.userID || profile?.userID}
                </Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Account ID
                </Typography>
                <Typography variant="body1" fontWeight={500}>
                  {user?.accountID}
                </Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Balance
                </Typography>
                <Typography variant="body1" fontWeight={500} color="primary.main">
                  ${user?.balance?.toFixed(2) || '0.00'}
                </Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Open Balance
                </Typography>
                <Typography variant="body1" fontWeight={500}>
                  ${user?.openBalance?.toFixed(2) || '0.00'}
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Profile Information (Editable) */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6" fontWeight={600}>
                  Profile Information
                </Typography>
                {!isEditing && (
                  <Button
                    variant="outlined"
                    startIcon={<EditIcon />}
                    onClick={() => setIsEditing(true)}
                  >
                    Edit
                  </Button>
                )}
              </Box>
              <Divider sx={{ mb: 2 }} />

              <form onSubmit={handleSubmit(onSubmit)}>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12 }}>
                    <TextField
                      fullWidth
                      label="Full Name"
                      {...register('fullName')}
                      error={!!errors.fullName}
                      helperText={errors.fullName?.message}
                      disabled={!isEditing}
                    />
                  </Grid>

                  <Grid size={{ xs: 12 }}>
                    <TextField
                      fullWidth
                      label="Email"
                      type="email"
                      {...register('email')}
                      error={!!errors.email}
                      helperText={errors.email?.message}
                      disabled={!isEditing}
                    />
                  </Grid>

                  <Grid size={{ xs: 12 }}>
                    <TextField
                      fullWidth
                      label="Address"
                      multiline
                      rows={2}
                      {...register('address')}
                      error={!!errors.address}
                      helperText={errors.address?.message}
                      disabled={!isEditing}
                    />
                  </Grid>

                  <Grid size={{ xs: 12 }}>
                    <TextField
                      fullWidth
                      label="Credit Card"
                      {...register('creditCard')}
                      error={!!errors.creditCard}
                      helperText={errors.creditCard?.message}
                      disabled={!isEditing}
                      placeholder="**** **** **** ****"
                    />
                  </Grid>

                  {isEditing && (
                    <Grid size={{ xs: 12 }}>
                      <Box display="flex" gap={2} justifyContent="flex-end">
                        <Button
                          variant="outlined"
                          startIcon={<CancelIcon />}
                          onClick={handleCancel}
                          disabled={updateProfile.isPending}
                        >
                          Cancel
                        </Button>
                        <Button
                          type="submit"
                          variant="contained"
                          startIcon={<SaveIcon />}
                          disabled={!isDirty || updateProfile.isPending}
                        >
                          {updateProfile.isPending ? 'Saving...' : 'Save Changes'}
                        </Button>
                      </Box>
                    </Grid>
                  )}
                </Grid>
              </form>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

