import React from 'react';
import { Typography, Box } from '@mui/material';
import { TrendingUp, TrendingDown } from '@mui/icons-material';

interface GainLossDisplayProps {
  gain: number;
  gainPercent?: number;
  showIcon?: boolean;
  variant?: 'h4' | 'h5' | 'h6' | 'body1' | 'body2';
}

export const GainLossDisplay: React.FC<GainLossDisplayProps> = ({ 
  gain, 
  gainPercent,
  showIcon = true,
  variant = 'body1'
}) => {
  const isPositive = gain >= 0;
  const color = isPositive ? 'success.main' : 'error.main';
  const sign = isPositive ? '+' : '';

  return (
    <Box display="flex" alignItems="center" gap={0.5}>
      {showIcon && (
        isPositive ? (
          <TrendingUp fontSize="small" color="success" />
        ) : (
          <TrendingDown fontSize="small" color="error" />
        )
      )}
      <Typography variant={variant} color={color} fontWeight={500}>
        {sign}${Math.abs(gain).toFixed(2)}
        {gainPercent !== undefined && ` (${sign}${Math.abs(gainPercent).toFixed(2)}%)`}
      </Typography>
    </Box>
  );
};

