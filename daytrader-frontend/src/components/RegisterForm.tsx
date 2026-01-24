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

  const inputClass = "w-full px-4 py-3 bg-[#16213E] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-500/50 focus:border-purple-500 transition-all";
  const labelClass = "block text-sm font-medium text-gray-400 mb-2";

  return (
    <div className="max-w-xl mx-auto w-full">
      <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-2xl p-8 border border-white/5 shadow-2xl">
        {/* Logo */}
        <div className="flex justify-center mb-6">
          <div className="w-12 h-12 bg-gradient-to-br from-purple-500 to-purple-700 rounded-xl flex items-center justify-center shadow-lg shadow-purple-500/30">
            <span className="text-white font-bold text-lg">DT</span>
          </div>
        </div>

        <h2 className="text-2xl font-bold text-white text-center mb-2">
          Create Your Account
        </h2>
        <p className="text-gray-400 text-center text-sm mb-6">
          Start your trading journey today
        </p>

        {(error || validationError) && (
          <div className="mb-4 p-3 bg-red-500/20 border border-red-500/30 text-red-400 rounded-lg text-sm">
            {error || validationError}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
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
            className="w-full py-3 px-4 bg-gradient-to-r from-purple-600 to-purple-500 text-white font-semibold rounded-lg hover:from-purple-500 hover:to-purple-400 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 focus:ring-offset-[#0D0D0D] disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-lg shadow-purple-500/20 hover:shadow-purple-500/40">
            {isLoading ? 'Creating Account...' : 'Create Account'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-400">
          Already have an account?{' '}
          <Link to="/login" className="text-purple-400 hover:text-purple-300 font-medium transition-colors">Sign in</Link>
        </p>
      </div>
    </div>
  );
}

