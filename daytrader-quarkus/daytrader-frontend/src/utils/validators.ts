import { z } from 'zod';

// Login validation schema
export const loginSchema = z.object({
  userId: z.string().min(1, 'User ID is required'),
  password: z.string().min(1, 'Password is required'),
});

// Registration validation schema
export const registerSchema = z.object({
  userId: z
    .string()
    .min(3, 'User ID must be at least 3 characters')
    .max(50, 'User ID must be at most 50 characters')
    .regex(/^[a-zA-Z0-9_:-]+$/, 'User ID can only contain letters, numbers, _, :, and -'),
  password: z
    .string()
    .min(8, 'Password must be at least 8 characters'),
  confirmPassword: z.string(),
  fullName: z.string().min(1, 'Full name is required').max(100),
  email: z.string().email('Invalid email address'),
  address: z.string().optional(),
  creditCard: z
    .string()
    .optional()
    .refine(
      (val) => !val || /^[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}$/.test(val),
      { message: 'Credit card must be in format XXXX-XXXX-XXXX-XXXX' }
    ),
  openBalance: z
    .number()
    .min(0, 'Opening balance must be positive')
    .max(1000000, 'Opening balance cannot exceed $1,000,000')
    .optional(),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword'],
});

// Profile update validation schema
export const updateProfileSchema = z.object({
  fullName: z.string().min(1, 'Full name is required').max(100).optional(),
  email: z.string().email('Invalid email address').optional(),
  address: z.string().max(200).optional(),
  creditCard: z.string().optional(),
});

// Change password validation schema
export const changePasswordSchema = z.object({
  currentPassword: z.string().min(1, 'Current password is required'),
  newPassword: z.string().min(8, 'New password must be at least 8 characters'),
  confirmPassword: z.string(),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword'],
});

// Buy order validation schema
export const buyOrderSchema = z.object({
  symbol: z.string().min(1, 'Symbol is required').max(10),
  quantity: z.number().min(0.01, 'Quantity must be at least 0.01'),
});

// Quote search validation schema
export const quoteSearchSchema = z.object({
  symbols: z.string().min(1, 'At least one symbol is required'),
});

