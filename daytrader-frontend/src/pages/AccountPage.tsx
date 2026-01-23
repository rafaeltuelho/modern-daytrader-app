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
      <h1 className="text-3xl font-bold text-gray-900">Account</h1>

      {/* Profile Information */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Profile Information</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-500">Username</label>
            <p className="mt-1 text-lg text-gray-900">{userID || profile?.userID || '-'}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-500">Full Name</label>
            <p className="mt-1 text-lg text-gray-900">{profile?.fullName || '-'}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-500">Email</label>
            <p className="mt-1 text-lg text-gray-900">{profile?.email || '-'}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-500">Address</label>
            <p className="mt-1 text-lg text-gray-900">{profile?.address || '-'}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-500">Credit Card</label>
            <p className="mt-1 text-lg text-gray-900">
              {profile?.creditCard ? `****${profile.creditCard.slice(-4)}` : '-'}
            </p>
          </div>
        </div>
      </div>

      {/* Account Statistics */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Account Statistics</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="p-4 bg-blue-50 rounded-lg">
            <p className="text-sm text-gray-600">Current Balance</p>
            <p className="text-2xl font-bold text-blue-600">${account.balance.toFixed(2)}</p>
          </div>
          <div className="p-4 bg-green-50 rounded-lg">
            <p className="text-sm text-gray-600">Opening Balance</p>
            <p className="text-2xl font-bold text-green-600">${account.openBalance.toFixed(2)}</p>
          </div>
          <div className="p-4 bg-purple-50 rounded-lg">
            <p className="text-sm text-gray-600">Login Count</p>
            <p className="text-2xl font-bold text-purple-600">{account.loginCount}</p>
          </div>
          <div className="p-4 bg-orange-50 rounded-lg">
            <p className="text-sm text-gray-600">Logout Count</p>
            <p className="text-2xl font-bold text-orange-600">{account.logoutCount}</p>
          </div>
        </div>
      </div>

      {/* Account Dates */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Activity</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-500">Account Created</label>
            <p className="mt-1 text-lg text-gray-900">
              {new Date(account.creationDate).toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}
            </p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-500">Last Login</label>
            <p className="mt-1 text-lg text-gray-900">
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
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Performance Summary</h2>
        <div className="flex items-center justify-center p-8">
          <div className="text-center">
            <p className="text-sm text-gray-600 mb-2">Total Return</p>
            <p className={`text-4xl font-bold ${account.balance >= account.openBalance ? 'text-green-600' : 'text-red-600'}`}>
              {account.balance >= account.openBalance ? '+' : ''}
              ${(account.balance - account.openBalance).toFixed(2)}
            </p>
            <p className={`text-lg ${account.balance >= account.openBalance ? 'text-green-500' : 'text-red-500'}`}>
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

