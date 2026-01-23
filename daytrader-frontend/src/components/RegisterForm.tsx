import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';

export interface RegisterData {
  userId: string;
  password: string;
  fullName: string;
  email: string;
  address: string;
  creditCard: string;
  openBalance: number;
}

interface RegisterFormProps {
  onSubmit: (data: RegisterData) => Promise<void>;
  isLoading?: boolean;
  error?: string;
}

export function RegisterForm({ onSubmit, isLoading = false, error }: RegisterFormProps) {
  const [formData, setFormData] = useState<RegisterData>({
    userId: '',
    password: '',
    fullName: '',
    email: '',
    address: '',
    creditCard: '',
    openBalance: 10000,
  });
  const [confirmPassword, setConfirmPassword] = useState('');
  const [validationError, setValidationError] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'number' ? parseFloat(value) || 0 : value,
    }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setValidationError('');

    if (formData.password !== confirmPassword) {
      setValidationError('Passwords do not match');
      return;
    }

    if (formData.password.length < 4) {
      setValidationError('Password must be at least 4 characters');
      return;
    }

    await onSubmit(formData);
  };

  const inputClass = "w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500";
  const labelClass = "block text-sm font-medium text-gray-700 mb-1";

  return (
    <div className="max-w-lg mx-auto">
      <div className="bg-white rounded-lg shadow-md p-8">
        <h2 className="text-2xl font-bold text-gray-900 text-center mb-6">
          Create Your Account
        </h2>

        {(error || validationError) && (
          <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
            {error || validationError}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="userId" className={labelClass}>Username *</label>
              <input id="userId" name="userId" type="text" required value={formData.userId}
                onChange={handleChange} className={inputClass} placeholder="Choose a username" />
            </div>
            <div>
              <label htmlFor="fullName" className={labelClass}>Full Name *</label>
              <input id="fullName" name="fullName" type="text" required value={formData.fullName}
                onChange={handleChange} className={inputClass} placeholder="Your full name" />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="password" className={labelClass}>Password *</label>
              <input id="password" name="password" type="password" required value={formData.password}
                onChange={handleChange} className={inputClass} placeholder="Min 4 characters" />
            </div>
            <div>
              <label htmlFor="confirmPassword" className={labelClass}>Confirm Password *</label>
              <input id="confirmPassword" type="password" required value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)} className={inputClass} placeholder="Confirm password" />
            </div>
          </div>

          <div>
            <label htmlFor="email" className={labelClass}>Email *</label>
            <input id="email" name="email" type="email" required value={formData.email}
              onChange={handleChange} className={inputClass} placeholder="your@email.com" />
          </div>

          <div>
            <label htmlFor="address" className={labelClass}>Address</label>
            <input id="address" name="address" type="text" value={formData.address}
              onChange={handleChange} className={inputClass} placeholder="Your address" />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="creditCard" className={labelClass}>Credit Card</label>
              <input id="creditCard" name="creditCard" type="text" value={formData.creditCard}
                onChange={handleChange} className={inputClass} placeholder="Card number" />
            </div>
            <div>
              <label htmlFor="openBalance" className={labelClass}>Opening Balance ($)</label>
              <input id="openBalance" name="openBalance" type="number" min="0" step="0.01"
                value={formData.openBalance} onChange={handleChange} className={inputClass} />
            </div>
          </div>

          <button type="submit" disabled={isLoading}
            className="w-full py-2 px-4 bg-blue-600 text-white font-medium rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
            {isLoading ? 'Creating Account...' : 'Create Account'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-600">
          Already have an account?{' '}
          <Link to="/login" className="text-blue-600 hover:text-blue-700 font-medium">Sign in</Link>
        </p>
      </div>
    </div>
  );
}

