import { useAccount } from '../hooks';
import { LoadingSpinner, ErrorAlert } from '../components';
import { useAuth } from '../context/AuthContext';

export function AccountPage() {
  const { data: account, isLoading, error } = useAccount();
  const { userID } = useAuth();

  if (isLoading) {
    return <LoadingSpinner message="Loading account..." />;
  }

  if (error) {
    return <ErrorAlert message="Failed to load account information" />;
  }

  if (!account) {
    return <ErrorAlert message="Account not found" />;
  }

  const profile = account.profile;

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <h1 className="text-3xl font-bold text-white">Account</h1>

      {/* Profile Information */}
      <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-6 border border-white/5">
        <h2 className="text-xl font-semibold text-white mb-4 flex items-center gap-2">
          <svg className="w-5 h-5 text-purple-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
          Profile Information
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="p-3 bg-[#16213E]/60 rounded-lg border border-white/5">
            <label className="block text-sm font-medium text-gray-500">Username</label>
            <p className="mt-1 text-lg text-white">{userID || profile?.userID || '-'}</p>
          </div>
          <div className="p-3 bg-[#16213E]/60 rounded-lg border border-white/5">
            <label className="block text-sm font-medium text-gray-500">Full Name</label>
            <p className="mt-1 text-lg text-white">{profile?.fullName || '-'}</p>
          </div>
          <div className="p-3 bg-[#16213E]/60 rounded-lg border border-white/5">
            <label className="block text-sm font-medium text-gray-500">Email</label>
            <p className="mt-1 text-lg text-white">{profile?.email || '-'}</p>
          </div>
          <div className="p-3 bg-[#16213E]/60 rounded-lg border border-white/5">
            <label className="block text-sm font-medium text-gray-500">Address</label>
            <p className="mt-1 text-lg text-white">{profile?.address || '-'}</p>
          </div>
          <div className="p-3 bg-[#16213E]/60 rounded-lg border border-white/5">
            <label className="block text-sm font-medium text-gray-500">Credit Card</label>
            <p className="mt-1 text-lg text-white">
              {profile?.creditCard ? `****${profile.creditCard.slice(-4)}` : '-'}
            </p>
          </div>
        </div>
      </div>

      {/* Account Statistics */}
      <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-6 border border-white/5">
        <h2 className="text-xl font-semibold text-white mb-4 flex items-center gap-2">
          <svg className="w-5 h-5 text-purple-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
          Account Statistics
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="p-4 bg-gradient-to-br from-blue-500/20 to-blue-600/10 rounded-xl border border-blue-500/20">
            <p className="text-sm text-gray-400">Current Balance</p>
            <p className="text-2xl font-bold text-blue-400">${account.balance.toFixed(2)}</p>
          </div>
          <div className="p-4 bg-gradient-to-br from-teal-500/20 to-teal-600/10 rounded-xl border border-teal-500/20">
            <p className="text-sm text-gray-400">Opening Balance</p>
            <p className="text-2xl font-bold text-teal-400">${account.openBalance.toFixed(2)}</p>
          </div>
          <div className="p-4 bg-gradient-to-br from-purple-500/20 to-purple-600/10 rounded-xl border border-purple-500/20">
            <p className="text-sm text-gray-400">Login Count</p>
            <p className="text-2xl font-bold text-purple-400">{account.loginCount}</p>
          </div>
          <div className="p-4 bg-gradient-to-br from-amber-500/20 to-amber-600/10 rounded-xl border border-amber-500/20">
            <p className="text-sm text-gray-400">Logout Count</p>
            <p className="text-2xl font-bold text-amber-400">{account.logoutCount}</p>
          </div>
        </div>
      </div>

      {/* Account Dates */}
      <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-6 border border-white/5">
        <h2 className="text-xl font-semibold text-white mb-4 flex items-center gap-2">
          <svg className="w-5 h-5 text-purple-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          Activity
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="p-3 bg-[#16213E]/60 rounded-lg border border-white/5">
            <label className="block text-sm font-medium text-gray-500">Account Created</label>
            <p className="mt-1 text-lg text-white">
              {new Date(account.creationDate).toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}
            </p>
          </div>
          <div className="p-3 bg-[#16213E]/60 rounded-lg border border-white/5">
            <label className="block text-sm font-medium text-gray-500">Last Login</label>
            <p className="mt-1 text-lg text-white">
              {new Date(account.lastLogin).toLocaleString('en-US', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
              })}
            </p>
          </div>
        </div>
      </div>

      {/* Performance Summary */}
      <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-6 border border-white/5">
        <h2 className="text-xl font-semibold text-white mb-4 flex items-center gap-2">
          <svg className="w-5 h-5 text-purple-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
          </svg>
          Performance Summary
        </h2>
        <div className="flex items-center justify-center p-8">
          <div className={`text-center p-8 rounded-2xl ${account.balance >= account.openBalance ? 'bg-gradient-to-br from-emerald-500/20 to-emerald-600/10 border border-emerald-500/20' : 'bg-gradient-to-br from-red-500/20 to-red-600/10 border border-red-500/20'}`}>
            <p className="text-sm text-gray-400 mb-2">Total Return</p>
            <p className={`text-5xl font-bold ${account.balance >= account.openBalance ? 'text-emerald-400' : 'text-red-400'}`}>
              {account.balance >= account.openBalance ? '+' : ''}
              ${(account.balance - account.openBalance).toFixed(2)}
            </p>
            <p className={`text-xl mt-2 font-medium ${account.balance >= account.openBalance ? 'text-emerald-400' : 'text-red-400'}`}>
              ({account.openBalance > 0
                ? (((account.balance - account.openBalance) / account.openBalance) * 100).toFixed(2)
                : '0.00'}%)
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

