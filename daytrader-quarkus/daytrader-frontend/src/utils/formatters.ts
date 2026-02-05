import { format, parseISO } from 'date-fns';

// Currency formatter
export const formatCurrency = (value: number | null | undefined): string => {
  if (value == null || isNaN(value)) {
    return '$0.00';
  }
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);
};

// Number formatter
export const formatNumber = (value: number | null | undefined, decimals: number = 2): string => {
  if (value == null || isNaN(value)) {
    return '0.00';
  }
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(value);
};

// Percentage formatter
export const formatPercent = (value: number | null | undefined, decimals: number = 2): string => {
  if (value == null || isNaN(value)) {
    return '0.00%';
  }
  return `${value >= 0 ? '+' : ''}${formatNumber(value, decimals)}%`;
};

// Date formatter
export const formatDate = (dateString: string): string => {
  try {
    const date = parseISO(dateString);
    return format(date, 'MMM dd, yyyy');
  } catch {
    return dateString;
  }
};

// DateTime formatter
export const formatDateTime = (dateString: string): string => {
  try {
    const date = parseISO(dateString);
    return format(date, 'MMM dd, yyyy HH:mm:ss');
  } catch {
    return dateString;
  }
};

// Time formatter
export const formatTime = (dateString: string): string => {
  try {
    const date = parseISO(dateString);
    return format(date, 'HH:mm:ss');
  } catch {
    return dateString;
  }
};

// Volume formatter (e.g., 1.2M, 3.5B)
export const formatVolume = (value: number | null | undefined): string => {
  if (value == null || isNaN(value)) {
    return '0';
  }
  if (value >= 1_000_000_000) {
    return `${(value / 1_000_000_000).toFixed(1)}B`;
  }
  if (value >= 1_000_000) {
    return `${(value / 1_000_000).toFixed(1)}M`;
  }
  if (value >= 1_000) {
    return `${(value / 1_000).toFixed(1)}K`;
  }
  return value.toString();
};

// Get color class for gain/loss
export const getGainLossColor = (value: number | null | undefined): string => {
  if (value == null || isNaN(value)) return 'text-gray-600';
  if (value > 0) return 'text-green-600';
  if (value < 0) return 'text-red-600';
  return 'text-gray-600';
};

// Get background color class for gain/loss
export const getGainLossBgColor = (value: number | null | undefined): string => {
  if (value == null || isNaN(value)) return 'bg-gray-50';
  if (value > 0) return 'bg-green-50';
  if (value < 0) return 'bg-red-50';
  return 'bg-gray-50';
};

