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
  };

  const handlePreferencesUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('Preferences settings are not yet supported. This feature is coming soon!');
    
    // TODO: Implement when backend endpoint is available
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
  };

  return (
    <ProtectedRoute>
      <Layout>
        <div className="container-responsive mobile-py-8 lg:py-12 animate-fade-in">
          <h1 className="text-title text-white mb-6 lg:mb-8 mobile-text-center">Settings</h1>

          {/* Success Message */}
          {success && (
            <div className="mb-6 p-4 bg-green-900/20 border border-green-800 text-green-400 rounded-xl">
              <div className="flex items-center">
                <span className="mr-2">✅</span>
                {success}
              </div>
            </div>
          )}

          {/* Error Message */}
          {error && (
            <div className="mb-6 p-4 bg-red-900/20 border border-red-800 text-red-400 rounded-xl">
              <div className="flex items-center">
                <span className="mr-2">❌</span>
                {error}
              </div>
            </div>
          )}

          <div className="space-y-responsive">
            {/* Account Settings Section */}
            <div className="glass-effect rounded-2xl p-6 lg:p-8 border border-gray-700">
              <h2 className="text-subtitle text-white mb-4 lg:mb-6">Account Settings</h2>
              <form onSubmit={handleAccountUpdate} className="space-y-4 lg:space-y-6">
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
                  <p className="mt-1 text-sm text-gray-500">
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
                    rows={4}
                    value={accountForm.bio}
                    onChange={(e) => setAccountForm({ ...accountForm, bio: e.target.value })}
                    className="input-field resize-none"
                    maxLength={500}
                    placeholder="Tell us about yourself..."
                  />
                  <div className="flex justify-between mt-1">
                    <span></span>
                    <p className="text-sm text-gray-500">
                      {accountForm.bio.length}/500
                    </p>
                  </div>
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
                  className="btn-primary w-full btn-responsive"
                >
                  {loading ? (
                    <span className="flex items-center justify-center">
                      <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
                      Updating...
                    </span>
                  ) : (
                    'Update Account Settings'
                  )}
                </button>
              </form>
            </div>

            {/* Password Change Section */}
            <div className="glass-effect rounded-2xl p-6 lg:p-8 border border-gray-700">
              <h2 className="text-subtitle text-white mb-4 lg:mb-6">Change Password</h2>
              <form onSubmit={handlePasswordChange} className="space-y-4 lg:space-y-6">
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
                  <p className="text-sm text-yellow-400 flex items-center">
                    <span className="mr-2">⚠️</span>
                    <strong>Coming Soon:</strong> Password change functionality is not yet available. 
                    This feature will be implemented in a future update.
                  </p>
                </div>

                <button
                  type="submit"
                  disabled={true} // Disabled until backend support
                  className="btn-secondary w-full btn-responsive opacity-50 cursor-not-allowed"
                >
                  Change Password (Coming Soon)
                </button>
              </form>
            </div>

            {/* Preferences Section */}
            <div className="glass-effect rounded-2xl p-6 lg:p-8 border border-gray-700">
              <h2 className="text-subtitle text-white mb-4 lg:mb-6">Preferences & Privacy</h2>
              <form onSubmit={handlePreferencesUpdate} className="space-y-4 lg:space-y-6">
                <div>
                  <h3 className="text-lg font-medium text-white mb-3 lg:mb-4">Notifications</h3>
                  <div className="space-y-3 lg:space-y-4">
                    <label className="flex items-center cursor-pointer">
                      <input
                        type="checkbox"
                        checked={preferencesForm.emailNotifications}
                        onChange={(e) => setPreferencesForm({ ...preferencesForm, emailNotifications: e.target.checked })}
                        className="w-4 h-4 text-red-600 bg-gray-800 border-gray-600 rounded focus:ring-red-500 focus:ring-2 disabled:opacity-50 disabled:cursor-not-allowed"
                        disabled // Not supported yet
                      />
                      <span className="ml-3 text-sm text-gray-300">Email notifications</span>
                    </label>

                    <label className="flex items-center cursor-pointer">
                      <input
                        type="checkbox"
                        checked={preferencesForm.pushNotifications}
                        onChange={(e) => setPreferencesForm({ ...preferencesForm, pushNotifications: e.target.checked })}
                        className="w-4 h-4 text-red-600 bg-gray-800 border-gray-600 rounded focus:ring-red-500 focus:ring-2 disabled:opacity-50 disabled:cursor-not-allowed"
                        disabled // Not supported yet
                      />
                      <span className="ml-3 text-sm text-gray-300">Push notifications</span>
                    </label>

                    <label className="flex items-center cursor-pointer">
                      <input
                        type="checkbox"
                        checked={preferencesForm.groupInvitations}
                        onChange={(e) => setPreferencesForm({ ...preferencesForm, groupInvitations: e.target.checked })}
                        className="w-4 h-4 text-red-600 bg-gray-800 border-gray-600 rounded focus:ring-red-500 focus:ring-2 disabled:opacity-50 disabled:cursor-not-allowed"
                        disabled // Not supported yet
                      />
                      <span className="ml-3 text-sm text-gray-300">Group invitations</span>
                    </label>

                    <label className="flex items-center cursor-pointer">
                      <input
                        type="checkbox"
                        checked={preferencesForm.reviewNotifications}
                        onChange={(e) => setPreferencesForm({ ...preferencesForm, reviewNotifications: e.target.checked })}
                        className="w-4 h-4 text-red-600 bg-gray-800 border-gray-600 rounded focus:ring-red-500 focus:ring-2 disabled:opacity-50 disabled:cursor-not-allowed"
                        disabled // Not supported yet
                      />
                      <span className="ml-3 text-sm text-gray-300">Review and rating notifications</span>
                    </label>
                  </div>
                </div>

                <div>
                  <h3 className="text-lg font-medium text-white mb-3 lg:mb-4">Privacy</h3>
                  <div>
                    <label htmlFor="privacyLevel" className="block text-sm font-medium text-gray-300 mb-2">
                      Profile Visibility
                    </label>
                    <select
                      id="privacyLevel"
                      value={preferencesForm.privacyLevel}
                      onChange={(e) => setPreferencesForm({ ...preferencesForm, privacyLevel: e.target.value as 'public' | 'friends' | 'private' })}
                      className="bg-gray-800 border border-gray-600 rounded-lg px-3 py-2 text-white w-full focus:ring-red-500 focus:border-red-500 disabled:opacity-50 disabled:cursor-not-allowed"
                      disabled // Not supported yet
                    >
                      <option value="public">Public - Anyone can see your profile</option>
                      <option value="friends">Friends only - Only your friends can see your profile</option>
                      <option value="private">Private - Only you can see your profile</option>
                    </select>
                  </div>
                </div>

                <div className="bg-yellow-900/20 border border-yellow-800 rounded-xl p-4">
                  <p className="text-sm text-yellow-400 flex items-center">
                    <span className="mr-2">⚠️</span>
                    <strong>Coming Soon:</strong> Notification and privacy preferences are not yet available. 
                    These features will be implemented in a future update.
                  </p>
                </div>

                <button
                  type="submit"
                  disabled={true} // Disabled until backend support
                  className="btn-secondary w-full btn-responsive opacity-50 cursor-not-allowed"
                >
                  Update Preferences (Coming Soon)
                </button>
              </form>
            </div>

            {/* Account Deletion Section */}
            <div className="glass-effect rounded-2xl p-6 lg:p-8 border border-red-800/50 bg-red-900/10">
              <h2 className="text-subtitle text-red-400 mb-4 lg:mb-6 flex items-center">
                <span className="mr-2">⚠️</span>
                Danger Zone
              </h2>
              <div className="space-y-4 lg:space-y-6">
                <div>
                  <h3 className="text-lg font-medium text-white mb-2 lg:mb-3">Delete Account</h3>
                  <p className="text-sm text-gray-300 mb-3 lg:mb-4">
                    Once you delete your account, there is no going back. Please be certain.
                    This will permanently delete:
                  </p>
                  <ul className="text-sm text-gray-400 list-disc list-inside mb-4 lg:mb-6 space-y-1 ml-4">
                    <li>Your profile and personal information</li>
                    <li>Your media library and ratings</li>
                    <li>All your reviews and comments</li>
                    <li>Your group memberships (you&apos;ll be removed from all groups)</li>
                    <li>Any groups you created (ownership will be transferred or groups will be deleted)</li>
                  </ul>
                </div>

                <div className="bg-yellow-900/20 border border-yellow-800 rounded-xl p-4">
                  <p className="text-sm text-yellow-400 flex items-center">
                    <span className="mr-2">⚠️</span>
                    <strong>Coming Soon:</strong> Account deletion is not yet available. 
                    Please contact support if you need to delete your account.
                  </p>
                </div>

                <button
                  type="button"
                  onClick={handleAccountDeletion}
                  disabled={true} // Disabled until backend support
                  className="bg-red-900/50 text-red-400 border border-red-800 rounded-lg px-4 py-2 btn-responsive opacity-50 cursor-not-allowed w-full sm:w-auto"
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