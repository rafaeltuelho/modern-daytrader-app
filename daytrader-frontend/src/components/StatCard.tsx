import React from 'react';
import { Card, CardContent, Typography, Box, Skeleton } from '@mui/material';

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon?: React.ReactNode;
  color?: string;
  isLoading?: boolean;
}

export const StatCard: React.FC<StatCardProps> = ({ 
  title, 
  value, 
  subtitle,
  icon,
  color = 'primary.main',
  isLoading = false
}) => {
  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            {title}
          </Typography>
          {icon && (
            <Box color={color}>
              {icon}
            </Box>
          )}
        </Box>
        {isLoading ? (
          <>
            <Skeleton variant="text" width="60%" height={48} />
            {subtitle && <Skeleton variant="text" width="40%" />}
          </>
        ) : (
          <>
            <Typography variant="h4" color={color} fontWeight={600} gutterBottom>
              {value}
            </Typography>
            {subtitle && (
              <Typography variant="body2" color="text.secondary">
                {subtitle}
              </Typography>
            )}
          </>
        )}
      </CardContent>
    </Card>
  );
};

