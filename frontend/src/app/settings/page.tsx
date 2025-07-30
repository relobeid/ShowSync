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
        <div className="container-responsive mobile-py-8">
          <h1 className="text-2xl sm:text-3xl font-bold text-white mb-6 sm:mb-8 mobile-text-center">Settings</h1>

          {/* Success Message */}
          {success && (
            <div className="mb-6 p-4 bg-green-900/20 border border-green-800 text-green-400 rounded-xl flex items-center">
              <svg className="w-5 h-5 mr-3 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"/>
              </svg>
              {success}
            </div>
          )}

          {/* Error Message */}
          {error && (
            <div className="mb-6 p-4 bg-red-900/20 border border-red-800 text-red-400 rounded-xl flex items-center">
              <svg className="w-5 h-5 mr-3 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"/>
              </svg>
              {error}
            </div>
          )}

          <div className="space-y-responsive">
            {/* Account Settings Section */}
            <div className="glass-effect rounded-2xl p-6 sm:p-8 border border-gray-700">
              <h2 className="text-xl sm:text-2xl font-semibold text-white mb-4 sm:mb-6">Account Settings</h2>
              <form onSubmit={handleAccountUpdate} className="space-y-4 sm:space-y-6">
                <div>
                  <label htmlFor="email" className="block text-sm font-medium text-gray-300 mb-2">
                    Email Address
                  </label>
                  <input
                    type="email"
                    id="email"
                    value={accountForm.email}
                    onChange={(e) => setAccountForm({ ...accountForm, email: e.target.value })}
                    className="input-field opacity-50 cursor-not-allowed"
                    disabled // Email updates not supported yet
                  />
                  <p className="mt-2 text-xs sm:text-sm text-gray-500">
                    Email changes are not yet supported. Contact support to change your email.
                  </p>
                </div>

                <div>
                  <label htmlFor="displayName" className="block text-sm font-medium text-gray-300 mb-2">
                    Display Name
                  </label>
                  <input
                    type="text"
                    id="displayName"
                    value={accountForm.displayName}
                    onChange={(e) => setAccountForm({ ...accountForm, displayName: e.target.value })}
                    className="input-field"
                    maxLength={50}
                  />
                </div>

                <div>
                  <label htmlFor="bio" className="block text-sm font-medium text-gray-300 mb-2">
                    Bio
                  </label>
                  <textarea
                    id="bio"
                    rows={3}
                    value={accountForm.bio}
                    onChange={(e) => setAccountForm({ ...accountForm, bio: e.target.value })}
                    className="input-field resize-none"
                    maxLength={500}
                    placeholder="Tell us about yourself..."
                  />
                </div>

                <div>
                  <label htmlFor="profilePictureUrl" className="block text-sm font-medium text-gray-300 mb-2">
                    Profile Picture URL
                  </label>
                  <input
                    type="url"
                    id="profilePictureUrl"
                    value={accountForm.profilePictureUrl}
                    onChange={(e) => setAccountForm({ ...accountForm, profilePictureUrl: e.target.value })}
                    className="input-field"
                    placeholder="https://example.com/your-photo.jpg"
                  />
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className={`btn-primary btn-responsive w-full ${loading ? 'opacity-50 cursor-not-allowed' : ''}`}
                >
                  {loading ? (
                    <div className="flex items-center justify-center">
                      <svg className="animate-spin -ml-1 mr-3 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Updating...
                    </div>
                  ) : (
                    'Update Account Settings'
                  )}
                </button>
              </form>
            </div>

            {/* Password Change Section */}
            <div className="glass-effect rounded-2xl p-6 sm:p-8 border border-gray-700">
              <h2 className="text-xl sm:text-2xl font-semibold text-white mb-4 sm:mb-6">Change Password</h2>
              <form onSubmit={handlePasswordChange} className="space-y-4 sm:space-y-6">
                <div>
                  <label htmlFor="currentPassword" className="block text-sm font-medium text-gray-300 mb-2">
                    Current Password
                  </label>
                  <input
                    type="password"
                    id="currentPassword"
                    value={passwordForm.currentPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
                    className="input-field opacity-50 cursor-not-allowed"
                    disabled // Not supported yet
                  />
                </div>

                <div>
                  <label htmlFor="newPassword" className="block text-sm font-medium text-gray-300 mb-2">
                    New Password
                  </label>
                  <input
                    type="password"
                    id="newPassword"
                    value={passwordForm.newPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                    className="input-field opacity-50 cursor-not-allowed"
                    disabled // Not supported yet
                  />
                </div>

                <div>
                  <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-300 mb-2">
                    Confirm New Password
                  </label>
                  <input
                    type="password"
                    id="confirmPassword"
                    value={passwordForm.confirmPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                    className="input-field opacity-50 cursor-not-allowed"
                    disabled // Not supported yet
                  />
                </div>

                <div className="bg-yellow-900/20 border border-yellow-800 rounded-xl p-4">
                  <div className="flex items-start">
                    <svg className="w-5 h-5 text-yellow-400 mt-0.5 mr-3 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"/>
                    </svg>
                    <div>
                      <p className="text-sm text-yellow-300 font-semibold">Coming Soon</p>
                      <p className="text-xs sm:text-sm text-yellow-200 mt-1">
                        Password change functionality is not yet available. This feature will be implemented in a future update.
                      </p>
                    </div>
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={true} // Disabled until backend support
                  className="btn-secondary btn-responsive w-full opacity-50 cursor-not-allowed"
                >
                  Change Password (Coming Soon)
                </button>
              </form>
            </div>

            {/* Preferences Section */}
            <div className="glass-effect rounded-2xl p-6 sm:p-8 border border-gray-700">
              <h2 className="text-xl sm:text-2xl font-semibold text-white mb-4 sm:mb-6">Preferences & Privacy</h2>
              <form onSubmit={handlePreferencesUpdate} className="space-y-4 sm:space-y-6">
                <div>
                  <h3 className="text-lg font-medium text-white mb-3">Notifications</h3>
                  <div className="space-y-3">
                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={preferencesForm.emailNotifications}
                        onChange={(e) => setPreferencesForm({ ...preferencesForm, emailNotifications: e.target.checked })}
                        className="rounded border-gray-600 bg-gray-800 text-red-600 shadow-sm focus:border-red-500 focus:ring focus:ring-red-500 focus:ring-opacity-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        disabled // Not supported yet
                      />
                      <span className="ml-3 text-sm text-gray-300">Email notifications</span>
                    </label>

                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={preferencesForm.pushNotifications}
                        onChange={(e) => setPreferencesForm({ ...preferencesForm, pushNotifications: e.target.checked })}
                        className="rounded border-gray-600 bg-gray-800 text-red-600 shadow-sm focus:border-red-500 focus:ring focus:ring-red-500 focus:ring-opacity-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        disabled // Not supported yet
                      />
                      <span className="ml-3 text-sm text-gray-300">Push notifications</span>
                    </label>

                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={preferencesForm.groupInvitations}
                        onChange={(e) => setPreferencesForm({ ...preferencesForm, groupInvitations: e.target.checked })}
                        className="rounded border-gray-600 bg-gray-800 text-red-600 shadow-sm focus:border-red-500 focus:ring focus:ring-red-500 focus:ring-opacity-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        disabled // Not supported yet
                      />
                      <span className="ml-3 text-sm text-gray-300">Group invitations</span>
                    </label>

                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={preferencesForm.reviewNotifications}
                        onChange={(e) => setPreferencesForm({ ...preferencesForm, reviewNotifications: e.target.checked })}
                        className="rounded border-gray-600 bg-gray-800 text-red-600 shadow-sm focus:border-red-500 focus:ring focus:ring-red-500 focus:ring-opacity-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        disabled // Not supported yet
                      />
                      <span className="ml-3 text-sm text-gray-300">Review and rating notifications</span>
                    </label>
                  </div>
                </div>

                <div>
                  <h3 className="text-lg font-medium text-white mb-3">Privacy</h3>
                  <div>
                    <label htmlFor="privacyLevel" className="block text-sm font-medium text-gray-300 mb-2">
                      Profile Visibility
                    </label>
                    <select
                      id="privacyLevel"
                      value={preferencesForm.privacyLevel}
                      onChange={(e) => setPreferencesForm({ ...preferencesForm, privacyLevel: e.target.value as 'public' | 'friends' | 'private' })}
                      className="input-field opacity-50 cursor-not-allowed"
                      disabled // Not supported yet
                    >
                      <option value="public">Public - Anyone can see your profile</option>
                      <option value="friends">Friends only - Only your friends can see your profile</option>
                      <option value="private">Private - Only you can see your profile</option>
                    </select>
                  </div>
                </div>

                <div className="bg-yellow-900/20 border border-yellow-800 rounded-xl p-4">
                  <div className="flex items-start">
                    <svg className="w-5 h-5 text-yellow-400 mt-0.5 mr-3 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"/>
                    </svg>
                    <div>
                      <p className="text-sm text-yellow-300 font-semibold">Coming Soon</p>
                      <p className="text-xs sm:text-sm text-yellow-200 mt-1">
                        Notification and privacy preferences are not yet available. These features will be implemented in a future update.
                      </p>
                    </div>
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={true} // Disabled until backend support
                  className="btn-secondary btn-responsive w-full opacity-50 cursor-not-allowed"
                >
                  Update Preferences (Coming Soon)
                </button>
              </form>
            </div>

            {/* Account Deletion Section */}
            <div className="glass-effect rounded-2xl p-6 sm:p-8 border border-gray-700 border-l-4 border-l-red-500">
              <h2 className="text-xl sm:text-2xl font-semibold text-red-400 mb-4 sm:mb-6">Danger Zone</h2>
              <div className="space-y-4 sm:space-y-6">
                <div>
                  <h3 className="text-lg font-medium text-white mb-2">Delete Account</h3>
                  <p className="text-sm sm:text-base text-gray-300 mb-4">
                    Once you delete your account, there is no going back. Please be certain.
                    This will permanently delete:
                  </p>
                  <ul className="text-sm text-gray-400 list-disc list-inside mb-4 space-y-1 ml-4">
                    <li>Your profile and personal information</li>
                    <li>Your media library and ratings</li>
                    <li>All your reviews and comments</li>
                    <li>Your group memberships (you&apos;ll be removed from all groups)</li>
                    <li>Any groups you created (ownership will be transferred or groups will be deleted)</li>
                  </ul>
                </div>

                <div className="bg-yellow-900/20 border border-yellow-800 rounded-xl p-4">
                  <div className="flex items-start">
                    <svg className="w-5 h-5 text-yellow-400 mt-0.5 mr-3 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"/>
                    </svg>
                    <div>
                      <p className="text-sm text-yellow-300 font-semibold">Coming Soon</p>
                      <p className="text-xs sm:text-sm text-yellow-200 mt-1">
                        Account deletion is not yet available. Please contact support if you need to delete your account.
                      </p>
                    </div>
                  </div>
                </div>

                <button
                  type="button"
                  onClick={handleAccountDeletion}
                  disabled={true} // Disabled until backend support
                  className="bg-red-900/20 border border-red-800 text-red-400 py-2 sm:py-3 px-4 sm:px-6 rounded-lg cursor-not-allowed opacity-50 text-sm sm:text-base font-medium w-full sm:w-auto"
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