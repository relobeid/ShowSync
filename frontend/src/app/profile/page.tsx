'use client';

import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '@/context/AuthContext';
import ProtectedRoute from '@/components/auth/ProtectedRoute';
import Layout from '@/components/layout/Layout';
import MediaCard from '@/components/media/MediaCard';
import { api } from '@/lib/api';
import { User } from '@/types/api';

// Sample media data for demonstration
const sampleMedia = [
  {
    id: 1,
    type: 'MOVIE' as const,
    title: 'The Matrix',
    originalTitle: 'The Matrix',
    overview: 'A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.',
    releaseDate: '1999-03-30',
    voteAverage: 8.2,
    voteCount: 26573,
    posterPath: '/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg',
  },
  {
    id: 2,
    type: 'TV_SHOW' as const,
    title: 'Breaking Bad',
    originalTitle: 'Breaking Bad',
    overview: 'A high school chemistry teacher diagnosed with inoperable lung cancer turns to manufacturing and selling methamphetamine.',
    releaseDate: '2008-01-20',
    voteAverage: 9.5,
    voteCount: 12500,
    posterPath: '/3xnWaLQjelJDDF7LT1WBo6f4BRe.jpg',
  },
  {
    id: 3,
    type: 'BOOK' as const,
    title: 'The Hobbit',
    originalTitle: 'The Hobbit',
    overview: 'Bilbo Baggins enjoys a quiet and contented life, with no desire to travel far from the comforts of home.',
    releaseDate: '1937-09-21',
    voteAverage: 4.8,
    voteCount: 2500,
    posterPath: null,
  }
];

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
    }

    if (formData.bio && formData.bio.length > 500) {
      newErrors.bio = 'Bio must be less than 500 characters';
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
      const updatedUser = await api.auth.updateProfile(formData, token!);
      setProfileUser(updatedUser);
      setIsEditing(false);
      setSuccess('Profile updated successfully!');
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <ProtectedRoute>
        <Layout>
          <div className="animate-fade-in">
            <div className="text-center py-12">
              <div className="skeleton w-32 h-32 rounded-full mx-auto mb-4"></div>
              <div className="skeleton w-48 h-6 mx-auto mb-2"></div>
              <div className="skeleton w-64 h-4 mx-auto"></div>
            </div>
          </div>
        </Layout>
      </ProtectedRoute>
    );
  }

  return (
    <ProtectedRoute>
      <Layout>
        <div className="animate-fade-in">
          {/* Profile Header */}
          <div className="glass-effect rounded-2xl p-8 mb-8 border border-gray-700">
            <div className="flex flex-col md:flex-row items-center md:items-start gap-6">
              {/* Avatar */}
              <div className="w-32 h-32 bg-gradient-to-br from-red-500 to-red-700 rounded-full flex items-center justify-center text-4xl font-bold text-white shadow-2xl">
                {profileUser?.displayName?.[0]?.toUpperCase() || profileUser?.username[0].toUpperCase()}
              </div>

              {/* Profile Info */}
              <div className="flex-1 text-center md:text-left">
                <div className="flex flex-col md:flex-row md:items-center gap-4 mb-4">
                  <h1 className="text-title">
                    {profileUser?.displayName || profileUser?.username}
                  </h1>
                  <button
                    onClick={() => setIsEditing(!isEditing)}
                    className="btn-secondary text-sm px-4 py-2"
                  >
                    {isEditing ? '❌ Cancel' : '✏️ Edit Profile'}
                  </button>
                </div>
                
                <p className="text-gray-400 mb-2">@{profileUser?.username}</p>
                <p className="text-gray-300 mb-4">{profileUser?.email}</p>
                
                {profileUser?.bio && (
                  <p className="text-gray-300 leading-relaxed">{profileUser.bio}</p>
                )}
              </div>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 mt-8 pt-8 border-t border-gray-700">
              <div className="text-center">
                <div className="text-2xl font-bold gradient-text mb-1">24</div>
                <div className="text-gray-400 text-sm">Movies Watched</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold gradient-text mb-1">8</div>
                <div className="text-gray-400 text-sm">TV Shows</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold gradient-text mb-1">12</div>
                <div className="text-gray-400 text-sm">Books Read</div>
              </div>
            </div>
          </div>

          {/* Edit Form */}
          {isEditing && (
            <div className="glass-effect rounded-2xl p-8 mb-8 border border-gray-700 animate-scale-in">
              <h2 className="text-subtitle mb-6">Edit Profile</h2>
              
              {error && (
                <div className="bg-red-900/20 border border-red-800 rounded-xl p-4 mb-6">
                  <p className="text-red-400">{error}</p>
                </div>
              )}

              {success && (
                <div className="bg-green-900/20 border border-green-800 rounded-xl p-4 mb-6">
                  <p className="text-green-400">{success}</p>
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-6">
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-2">
                    Display Name
                  </label>
                  <input
                    type="text"
                    name="displayName"
                    value={formData.displayName}
                    onChange={handleChange}
                    className={`input-field ${errors.displayName ? 'border-red-500' : ''}`}
                    maxLength={50}
                  />
                  {errors.displayName && (
                    <p className="mt-1 text-sm text-red-400">{errors.displayName}</p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-2">
                    Bio
                  </label>
                  <textarea
                    name="bio"
                    value={formData.bio}
                    onChange={handleChange}
                    rows={4}
                    className={`input-field ${errors.bio ? 'border-red-500' : ''}`}
                    placeholder="Tell us about yourself..."
                    maxLength={500}
                  />
                  <div className="flex justify-between mt-1">
                    {errors.bio && <p className="text-sm text-red-400">{errors.bio}</p>}
                    <p className="text-sm text-gray-500 ml-auto">
                      {formData.bio.length}/500
                    </p>
                  </div>
                </div>

                <div className="flex gap-4">
                  <button
                    type="submit"
                    disabled={saving}
                    className="btn-primary flex-1"
                  >
                    {saving ? 'Saving...' : 'Save Changes'}
                  </button>
                  <button
                    type="button"
                    onClick={() => setIsEditing(false)}
                    className="btn-secondary flex-1"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          )}

          {/* Recent Activity / Library Preview */}
          <div className="space-y-8">
            <div>
              <h2 className="text-subtitle mb-6">Your Library Preview</h2>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {sampleMedia.map((media) => (
                  <MediaCard
                    key={media.id}
                    {...media}
                    onAddToLibrary={(id) => console.log('Add to library:', id)}
                    onViewDetails={(id) => console.log('View details:', id)}
                  />
                ))}
              </div>
            </div>
          </div>
        </div>
      </Layout>
    </ProtectedRoute>
  );
} 