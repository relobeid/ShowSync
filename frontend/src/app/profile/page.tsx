'use client';

import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '@/context/AuthContext';
import ProtectedRoute from '@/components/auth/ProtectedRoute';
import Layout from '@/components/layout/Layout';
import { api } from '@/lib/api';
import { User } from '@/types/api';

export default function ProfilePage() {
  const { token } = useAuth();
  const [profileUser, setProfileUser] = useState<User | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [formData, setFormData] = useState({
    displayName: '',
    bio: '',
    profilePictureUrl: '',
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  const fetchProfile = useCallback(async () => {
    try {
      setLoading(true);
      const profileData = await api.auth.getProfile(token!);
      setProfileUser(profileData);
      setFormData({
        displayName: profileData.displayName || '',
        bio: profileData.bio || '',
        profilePictureUrl: profileData.profilePictureUrl || '',
      });
    } catch (error) {
      console.error('Error fetching profile:', error);
      setError('Failed to load profile');
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    if (token) {
      fetchProfile();
    }
  }, [token, fetchProfile]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.displayName.trim()) {
      newErrors.displayName = 'Display name is required';
    } else if (formData.displayName.length < 2) {
      newErrors.displayName = 'Display name must be at least 2 characters';
    } else if (formData.displayName.length > 50) {
      newErrors.displayName = 'Display name must be less than 50 characters';
    }

    if (formData.bio && formData.bio.length > 500) {
      newErrors.bio = 'Bio must be less than 500 characters';
    }

    if (formData.profilePictureUrl && formData.profilePictureUrl.length > 0) {
      try {
        new URL(formData.profilePictureUrl);
      } catch {
        newErrors.profilePictureUrl = 'Please enter a valid URL';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setSaving(true);
    setError('');
    setSuccess('');
    
    try {
      const updatedProfile = await api.auth.updateProfile(formData, token!);
      setProfileUser(prev => prev ? { ...prev, ...updatedProfile } : null);
      setIsEditing(false);
      setSuccess('Profile updated successfully!');
    } catch (error) {
      console.error('Error updating profile:', error);
      setError(error instanceof Error ? error.message : 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  if (loading) {
    return (
      <ProtectedRoute>
        <Layout>
          <div className="min-h-screen flex items-center justify-center">
            <div className="text-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
              <p className="mt-4 text-gray-600">Loading profile...</p>
            </div>
          </div>
        </Layout>
      </ProtectedRoute>
    );
  }

  if (!profileUser) {
    return (
      <ProtectedRoute>
        <Layout>
          <div className="max-w-4xl mx-auto py-8 px-4">
            <div className="text-center">
              <p className="text-red-600">Failed to load profile data</p>
              <button 
                onClick={fetchProfile}
                className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
              >
                Try Again
              </button>
            </div>
          </div>
        </Layout>
      </ProtectedRoute>
    );
  }

  return (
    <ProtectedRoute>
      <Layout>
        <div className="max-w-4xl mx-auto py-8 px-4">
          <div className="bg-white shadow-lg rounded-lg overflow-hidden">
            {/* Header */}
            <div className="bg-gradient-to-r from-blue-600 to-blue-700 px-6 py-8">
              <div className="flex items-center space-x-6">
                                 <div className="w-24 h-24 bg-white rounded-full flex items-center justify-center overflow-hidden">
                   {profileUser.profilePictureUrl ? (
                     <img 
                       src={profileUser.profilePictureUrl} 
                       alt="Profile" 
                       className="w-full h-full object-cover"
                       onError={(e) => {
                         e.currentTarget.style.display = 'none';
                         e.currentTarget.nextElementSibling?.classList.remove('hidden');
                       }}
                     />
                   ) : null}
                   <div className={`text-3xl font-bold text-blue-600 ${profileUser.profilePictureUrl ? 'hidden' : ''}`}>
                     {profileUser.displayName?.charAt(0).toUpperCase() || profileUser.username.charAt(0).toUpperCase()}
                   </div>
                 </div>
                 <div className="text-white">
                   <h1 className="text-3xl font-bold">{profileUser.displayName || profileUser.username}</h1>
                   <p className="text-blue-100">@{profileUser.username}</p>
                   <p className="text-blue-100">{profileUser.email}</p>
                 </div>
              </div>
            </div>

            {/* Content */}
            <div className="p-6">
              {/* Success/Error Messages */}
              {success && (
                <div className="mb-6 p-4 bg-green-50 border border-green-200 text-green-800 rounded-md">
                  {success}
                </div>
              )}
              
              {error && (
                <div className="mb-6 p-4 bg-red-50 border border-red-200 text-red-800 rounded-md">
                  {error}
                </div>
              )}

              {!isEditing ? (
                // Display Mode
                <div className="space-y-6">
                  <div className="flex justify-between items-center">
                    <h2 className="text-2xl font-bold text-gray-900">Profile Information</h2>
                    <button
                      onClick={() => setIsEditing(true)}
                      className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
                    >
                      Edit Profile
                    </button>
                  </div>

                                     <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                     <div>
                       <label className="block text-sm font-medium text-gray-700 mb-1">Display Name</label>
                       <p className="text-gray-900">{profileUser.displayName || 'Not set'}</p>
                     </div>
                     
                     <div>
                       <label className="block text-sm font-medium text-gray-700 mb-1">Role</label>
                       <p className="text-gray-900 capitalize">{profileUser.role.toLowerCase()}</p>
                     </div>

                     <div>
                       <label className="block text-sm font-medium text-gray-700 mb-1">Member Since</label>
                       <p className="text-gray-900">{formatDate(profileUser.createdAt)}</p>
                     </div>

                     <div>
                       <label className="block text-sm font-medium text-gray-700 mb-1">Email Status</label>
                       <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                         profileUser.emailVerified 
                           ? 'bg-green-100 text-green-800' 
                           : 'bg-yellow-100 text-yellow-800'
                       }`}>
                         {profileUser.emailVerified ? 'Verified' : 'Unverified'}
                       </span>
                     </div>

                     {profileUser.lastLoginAt && (
                       <div>
                         <label className="block text-sm font-medium text-gray-700 mb-1">Last Login</label>
                         <p className="text-gray-900">{formatDate(profileUser.lastLoginAt)}</p>
                       </div>
                     )}
                   </div>

                   <div>
                     <label className="block text-sm font-medium text-gray-700 mb-1">Bio</label>
                     <p className="text-gray-900 whitespace-pre-wrap">
                       {profileUser.bio || 'No bio added yet.'}
                     </p>
                   </div>
                </div>
              ) : (
                // Edit Mode
                <div className="space-y-6">
                  <div className="flex justify-between items-center">
                    <h2 className="text-2xl font-bold text-gray-900">Edit Profile</h2>
                    <button
                      onClick={() => {
                        setIsEditing(false);
                        setErrors({});
                        setError('');
                        setSuccess('');
                                                 // Reset form data
                         setFormData({
                           displayName: profileUser.displayName || '',
                           bio: profileUser.bio || '',
                           profilePictureUrl: profileUser.profilePictureUrl || '',
                         });
                      }}
                      className="px-4 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 transition-colors"
                    >
                      Cancel
                    </button>
                  </div>

                  <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                      <label htmlFor="displayName" className="block text-sm font-medium text-gray-700 mb-1">
                        Display Name *
                      </label>
                      <input
                        type="text"
                        id="displayName"
                        name="displayName"
                        value={formData.displayName}
                        onChange={handleChange}
                        className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                          errors.displayName ? 'border-red-300' : 'border-gray-300'
                        }`}
                        placeholder="Enter your display name"
                      />
                      {errors.displayName && (
                        <p className="mt-1 text-sm text-red-600">{errors.displayName}</p>
                      )}
                    </div>

                    <div>
                      <label htmlFor="profilePictureUrl" className="block text-sm font-medium text-gray-700 mb-1">
                        Profile Picture URL
                      </label>
                      <input
                        type="url"
                        id="profilePictureUrl"
                        name="profilePictureUrl"
                        value={formData.profilePictureUrl}
                        onChange={handleChange}
                        className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                          errors.profilePictureUrl ? 'border-red-300' : 'border-gray-300'
                        }`}
                        placeholder="https://example.com/your-photo.jpg"
                      />
                      {errors.profilePictureUrl && (
                        <p className="mt-1 text-sm text-red-600">{errors.profilePictureUrl}</p>
                      )}
                      <p className="mt-1 text-sm text-gray-500">
                        Enter a URL to your profile picture
                      </p>
                    </div>

                    <div>
                      <label htmlFor="bio" className="block text-sm font-medium text-gray-700 mb-1">
                        Bio
                      </label>
                      <textarea
                        id="bio"
                        name="bio"
                        rows={4}
                        value={formData.bio}
                        onChange={handleChange}
                        className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                          errors.bio ? 'border-red-300' : 'border-gray-300'
                        }`}
                        placeholder="Tell us about yourself..."
                      />
                      {errors.bio && (
                        <p className="mt-1 text-sm text-red-600">{errors.bio}</p>
                      )}
                      <p className="mt-1 text-sm text-gray-500">
                        {formData.bio.length}/500 characters
                      </p>
                    </div>

                    <div className="flex justify-end space-x-3">
                      <button
                        type="button"
                        onClick={() => {
                          setIsEditing(false);
                          setErrors({});
                          setError('');
                          setSuccess('');
                        }}
                        className="px-4 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 transition-colors"
                      >
                        Cancel
                      </button>
                      <button
                        type="submit"
                        disabled={saving}
                        className={`px-6 py-2 rounded-md text-white transition-colors ${
                          saving
                            ? 'bg-blue-400 cursor-not-allowed'
                            : 'bg-blue-600 hover:bg-blue-700'
                        }`}
                      >
                        {saving ? 'Saving...' : 'Save Changes'}
                      </button>
                    </div>
                  </form>
                </div>
              )}
            </div>
          </div>
        </div>
      </Layout>
    </ProtectedRoute>
  );
} 