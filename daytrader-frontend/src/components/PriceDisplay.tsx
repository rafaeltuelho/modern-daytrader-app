import React from 'react';
import { Typography, Box } from '@mui/material';
import { TrendingUp, TrendingDown } from '@mui/icons-material';

interface PriceDisplayProps {
  price: number;
  change?: number;
  showIcon?: boolean;
  variant?: 'h4' | 'h5' | 'h6' | 'body1' | 'body2';
}

export const PriceDisplay: React.FC<PriceDisplayProps> = ({ 
  price, 
  change,
  showIcon = false,
  variant = 'body1'
}) => {
  const isPositive = change !== undefined && change >= 0;
  const color = change === undefined ? 'text.primary' : isPositive ? 'success.main' : 'error.main';

  return (
    <Box display="flex" alignItems="center" gap={0.5}>
      {showIcon && change !== undefined && (
        isPositive ? (
          <TrendingUp fontSize="small" color="success" />
        ) : (
          <TrendingDown fontSize="small" color="error" />
        )
      )}
      <Typography variant={variant} color={color} fontWeight={500}>
        ${price.toFixed(2)}
      </Typography>
    </Box>
  );
};

