'use client';

import { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import ProtectedRoute from '@/components/auth/ProtectedRoute';
import Layout from '@/components/layout/Layout';
import { api } from '@/lib/api';

interface AccountSettingsForm {
  email: string;
  displayName: string;
  bio: string;
  profilePictureUrl: string;
}

interface PasswordChangeForm {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

interface PreferencesForm {
  emailNotifications: boolean;
  pushNotifications: boolean;
  groupInvitations: boolean;
  reviewNotifications: boolean;
  privacyLevel: 'public' | 'friends' | 'private';
}

export default function SettingsPage() {
  const { user, token } = useAuth();
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  // Account Settings State
  const [accountForm, setAccountForm] = useState<AccountSettingsForm>({
    email: '',
    displayName: '',
    bio: '',
    profilePictureUrl: '',
  });

  // Password Change State
  const [passwordForm, setPasswordForm] = useState<PasswordChangeForm>({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  // Preferences State
  const [preferencesForm, setPreferencesForm] = useState<PreferencesForm>({
    emailNotifications: true,
    pushNotifications: false,
    groupInvitations: true,
    reviewNotifications: true,
    privacyLevel: 'public',
  });

  // Initialize form with user data
  useEffect(() => {
    if (user) {
      setAccountForm({
        email: user.email || '',
        displayName: user.displayName || '',
        bio: user.bio || '',
        profilePictureUrl: user.profilePictureUrl || '',
      });
    }
  }, [user]);

  const handleAccountUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      await api.auth.updateProfile({
        displayName: accountForm.displayName,
        bio: accountForm.bio,
        profilePictureUrl: accountForm.profilePictureUrl,
      }, token);

      setSuccess('Account settings updated successfully!');
      
      // Note: Email updates would require backend support
      if (accountForm.email !== user?.email) {
        setError('Email updates are not yet supported. Please contact support to change your email.');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update account settings');
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setError('New passwords do not match');
      return;
    }

    if (passwordForm.newPassword.length < 8) {
      setError('New password must be at least 8 characters long');
      return;
    }

    setError('Password change is not yet supported. This feature is coming soon!');
    
    // TODO: Implement when backend endpoint is available
    // try {
    //   await api.auth.changePassword({
    //     currentPassword: passwordForm.currentPassword,
    //     newPassword: passwordForm.newPassword,
    //   }, token);
    //   setSuccess('Password changed successfully!');
    //   setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
    // } catch (err) {
    //   setError(err instanceof Error ? err.message : 'Failed to change password');
    // }
  };

  const handlePreferencesUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('Preferences settings are not yet supported. This feature is coming soon!');
    
    // TODO: Implement when backend endpoint is available
    // try {
    //   await api.auth.updatePreferences(preferencesForm, token);
    //   setSuccess('Preferences updated successfully!');
    // } catch (err) {
    //   setError(err instanceof Error ? err.message : 'Failed to update preferences');
    // }
  };

  const handleAccountDeletion = async () => {
    const confirmed = window.confirm(
      'Are you sure you want to delete your account? This action cannot be undone.'
    );
    
    if (!confirmed) return;

    const doubleConfirmed = window.confirm(
      'This will permanently delete all your data including your library, reviews, and group memberships. Type "DELETE" in the next prompt to confirm.'
    );

    if (!doubleConfirmed) return;

    const finalConfirmation = window.prompt(
      'Type "DELETE" (in capital letters) to permanently delete your account:'
    );

    if (finalConfirmation !== 'DELETE') {
      setError('Account deletion cancelled. You must type "DELETE" exactly to confirm.');
      return;
    }

    setError('Account deletion is not yet supported. Please contact support to delete your account.');
    
    // TODO: Implement when backend endpoint is available
    // try {
    //   await api.auth.deleteAccount(token);
    //   logout();
    //   window.location.href = '/';
    // } catch (err) {
    //   setError(err instanceof Error ? err.message : 'Failed to delete account');
    // }
  };

  return (
    <ProtectedRoute>
      <Layout>
        <div className="max-w-4xl mx-auto py-8 px-4">
          <h1 className="text-3xl font-bold text-gray-900 mb-8">Settings</h1>

          {/* Success Message */}
          {success && (
            <div className="mb-6 p-4 bg-green-100 border border-green-400 text-green-700 rounded-md">
              {success}
            </div>
          )}

          {/* Error Message */}
          {error && (
            <div className="mb-6 p-4 bg-red-100 border border-red-400 text-red-700 rounded-md">
              {error}
            </div>
          )}

          <div className="space-y-8">
            {/* Account Settings Section */}
            <div className="bg-white shadow rounded-lg p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Account Settings</h2>
              <form onSubmit={handleAccountUpdate} className="space-y-4">
                <div>
                  <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                    Email Address
                  </label>
                  <input
                    type="email"
                    id="email"
                    value={accountForm.email}
                    onChange={(e) => setAccountForm({ ...accountForm, email: e.target.value })}
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                    disabled // Email updates not supported yet
                  />
                  <p className="mt-1 text-sm text-gray-500">
                    Email changes are not yet supported. Contact support to change your email.
                  </p>
                </div>

                <div>
                  <label htmlFor="displayName" className="block text-sm font-medium text-gray-700">
                    Display Name
                  </label>
                  <input
                    type="text"
                    id="displayName"
                    value={accountForm.displayName}
                    onChange={(e) => setAccountForm({ ...accountForm, displayName: e.target.value })}
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                    maxLength={50}
                  />
                </div>

                <div>
                  <label htmlFor="bio" className="block text-sm font-medium text-gray-700">
                    Bio
                  </label>
                  <textarea
                    id="bio"
                    rows={3}
                    value={accountForm.bio}
                    onChange={(e) => setAccountForm({ ...accountForm, bio: e.target.value })}
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                    maxLength={500}
                    placeholder="Tell us about yourself..."
                  />
                </div>

                <div>
                  <label htmlFor="profilePictureUrl" className="block text-sm font-medium text-gray-700">
                    Profile Picture URL
                  </label>
                  <input
                    type="url"
                    id="profilePictureUrl"
                    value={accountForm.profilePictureUrl}
                    onChange={(e) => setAccountForm({ ...accountForm, profilePictureUrl: e.target.value })}
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                    placeholder="https://example.com/your-photo.jpg"
                  />
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:bg-gray-400"
                >
                  {loading ? 'Updating...' : 'Update Account Settings'}
                </button>
              </form>
            </div>

            {/* Password Change Section */}
            <div className="bg-white shadow rounded-lg p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Change Password</h2>
              <form onSubmit={handlePasswordChange} className="space-y-4">
                <div>
                  <label htmlFor="currentPassword" className="block text-sm font-medium text-gray-700">
                    Current Password
                  </label>
                  <input
                    type="password"
                    id="currentPassword"
                    value={passwordForm.currentPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                    disabled // Not supported yet
                  />
                </div>

                <div>
                  <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700">
                    New Password
                  </label>
                  <input
                    type="password"
                    id="newPassword"
                    value={passwordForm.newPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                    disabled // Not supported yet
                  />
                </div>

                <div>
                  <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700">
                    Confirm New Password
                  </label>
                  <input
                    type="password"
                    id="confirmPassword"
                    value={passwordForm.confirmPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                    disabled // Not supported yet
                  />
                </div>

                <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4">
                  <p className="text-sm text-yellow-800">
                    <strong>Coming Soon:</strong> Password change functionality is not yet available. 
                    This feature will be implemented in a future update.
                  </p>
                </div>

                <button
                  type="submit"
                  disabled={true} // Disabled until backend support
                  className="w-full bg-gray-400 text-white py-2 px-4 rounded-md cursor-not-allowed"
                >
                  Change Password (Coming Soon)
                </button>
              </form>
            </div>

            {/* Preferences Section */}
            <div className="bg-white shadow rounded-lg p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Preferences & Privacy</h2>
              <form onSubmit={handlePreferencesUpdate} className="space-y-4">
                <div>
                  <h3 className="text-lg font-medium text-gray-900 mb-3">Notifications</h3>
                  <div className="space-y-3">
                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={preferencesForm.emailNotifications}
                        onChange={(e) => setPreferencesForm({ ...preferencesForm, emailNotifications: e.target.checked })}
                        className="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
                        disabled // Not supported yet
                      />
                      <span className="ml-2 text-sm text-gray-700">Email notifications</span>
                    </label>

                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={preferencesForm.pushNotifications}
                        onChange={(e) => setPreferencesForm({ ...preferencesForm, pushNotifications: e.target.checked })}
                        className="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
                        disabled // Not supported yet
                      />
                      <span className="ml-2 text-sm text-gray-700">Push notifications</span>
                    </label>

                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={preferencesForm.groupInvitations}
                        onChange={(e) => setPreferencesForm({ ...preferencesForm, groupInvitations: e.target.checked })}
                        className="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
                        disabled // Not supported yet
                      />
                      <span className="ml-2 text-sm text-gray-700">Group invitations</span>
                    </label>

                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={preferencesForm.reviewNotifications}
                        onChange={(e) => setPreferencesForm({ ...preferencesForm, reviewNotifications: e.target.checked })}
                        className="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
                        disabled // Not supported yet
                      />
                      <span className="ml-2 text-sm text-gray-700">Review and rating notifications</span>
                    </label>
                  </div>
                </div>

                <div>
                  <h3 className="text-lg font-medium text-gray-900 mb-3">Privacy</h3>
                  <div>
                    <label htmlFor="privacyLevel" className="block text-sm font-medium text-gray-700">
                      Profile Visibility
                    </label>
                    <select
                      id="privacyLevel"
                      value={preferencesForm.privacyLevel}
                      onChange={(e) => setPreferencesForm({ ...preferencesForm, privacyLevel: e.target.value as 'public' | 'friends' | 'private' })}
                      className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                      disabled // Not supported yet
                    >
                      <option value="public">Public - Anyone can see your profile</option>
                      <option value="friends">Friends only - Only your friends can see your profile</option>
                      <option value="private">Private - Only you can see your profile</option>
                    </select>
                  </div>
                </div>

                <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4">
                  <p className="text-sm text-yellow-800">
                    <strong>Coming Soon:</strong> Notification and privacy preferences are not yet available. 
                    These features will be implemented in a future update.
                  </p>
                </div>

                <button
                  type="submit"
                  disabled={true} // Disabled until backend support
                  className="w-full bg-gray-400 text-white py-2 px-4 rounded-md cursor-not-allowed"
                >
                  Update Preferences (Coming Soon)
                </button>
              </form>
            </div>

            {/* Account Deletion Section */}
            <div className="bg-white shadow rounded-lg p-6 border-l-4 border-red-500">
              <h2 className="text-xl font-semibold text-red-600 mb-4">Danger Zone</h2>
              <div className="space-y-4">
                <div>
                  <h3 className="text-lg font-medium text-gray-900">Delete Account</h3>
                  <p className="text-sm text-gray-600 mb-4">
                    Once you delete your account, there is no going back. Please be certain.
                    This will permanently delete:
                  </p>
                  <ul className="text-sm text-gray-600 list-disc list-inside mb-4 space-y-1">
                    <li>Your profile and personal information</li>
                    <li>Your media library and ratings</li>
                    <li>All your reviews and comments</li>
                    <li>Your group memberships (you&apos;ll be removed from all groups)</li>
                    <li>Any groups you created (ownership will be transferred or groups will be deleted)</li>
                  </ul>
                </div>

                <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4">
                  <p className="text-sm text-yellow-800">
                    <strong>Coming Soon:</strong> Account deletion is not yet available. 
                    Please contact support if you need to delete your account.
                  </p>
                </div>

                <button
                  type="button"
                  onClick={handleAccountDeletion}
                  disabled={true} // Disabled until backend support
                  className="bg-gray-400 text-white py-2 px-4 rounded-md cursor-not-allowed"
                >
                  Delete Account (Coming Soon)
                </button>
              </div>
            </div>
          </div>
        </div>
      </Layout>
    </ProtectedRoute>
  );
} 