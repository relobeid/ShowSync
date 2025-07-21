'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/context/AuthContext';

export default function RegisterPage() {
  const { register } = useAuth();
  const router = useRouter();
  
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    displayName: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
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

    if (!formData.username.trim()) {
      newErrors.username = 'Username is required';
    } else if (formData.username.length < 3) {
      newErrors.username = 'Username must be at least 3 characters';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Please enter a valid email address';
    }

    if (!formData.password) {
      newErrors.password = 'Password is required';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your password';
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    if (!formData.displayName.trim()) {
      newErrors.displayName = 'Display name is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setLoading(true);
    
    try {
      await register(
        formData.username,
        formData.email,
        formData.password,
        formData.displayName
      );
      router.push('/'); // Redirect to home after successful registration
    } catch (error) {
      setErrors({
        submit: error instanceof Error ? error.message : 'Registration failed'
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-950 via-gray-900 to-black py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8 animate-fade-in">
        {/* Logo */}
        <div className="text-center">
          <div className="flex items-center justify-center mb-6">
            <Link href="/" className="flex items-center group">
              <span className="text-3xl font-bold gradient-text">ShowSync</span>
              <div className="w-2 h-2 bg-red-500 rounded-full animate-pulse ml-2"></div>
            </Link>
          </div>
          <h2 className="text-3xl font-bold text-white mb-2">
            Join the Club
          </h2>
          <p className="text-gray-400">
            Already have an account?{' '}
            <Link 
              href="/auth/login"
              className="text-red-400 hover:text-red-300 font-medium transition-colors"
            >
              Sign in here
            </Link>
          </p>
        </div>
        
        {/* Registration Form */}
        <div className="glass-effect rounded-2xl p-8 border border-gray-700">
          <form className="space-y-6" onSubmit={handleSubmit}>
            <div>
              <label htmlFor="username" className="block text-sm font-medium text-gray-300 mb-2">
                Username
              </label>
              <input
                id="username"
                name="username"
                type="text"
                required
                className={`input-field ${
                  errors.username ? 'border-red-500 focus:border-red-500' : ''
                }`}
                placeholder="Choose a username"
                value={formData.username}
                onChange={handleChange}
              />
              {errors.username && (
                <p className="mt-2 text-sm text-red-400 flex items-center">
                  <span className="mr-1">⚠️</span>
                  {errors.username}
                </p>
              )}
            </div>

            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-300 mb-2">
                Email Address
              </label>
              <input
                id="email"
                name="email"
                type="email"
                required
                className={`input-field ${
                  errors.email ? 'border-red-500 focus:border-red-500' : ''
                }`}
                placeholder="Enter your email"
                value={formData.email}
                onChange={handleChange}
              />
              {errors.email && (
                <p className="mt-2 text-sm text-red-400 flex items-center">
                  <span className="mr-1">⚠️</span>
                  {errors.email}
                </p>
              )}
            </div>

            <div>
              <label htmlFor="displayName" className="block text-sm font-medium text-gray-300 mb-2">
                Display Name
              </label>
              <input
                id="displayName"
                name="displayName"
                type="text"
                required
                className={`input-field ${
                  errors.displayName ? 'border-red-500 focus:border-red-500' : ''
                }`}
                placeholder="How should we call you?"
                value={formData.displayName}
                onChange={handleChange}
              />
              {errors.displayName && (
                <p className="mt-2 text-sm text-red-400 flex items-center">
                  <span className="mr-1">⚠️</span>
                  {errors.displayName}
                </p>
              )}
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-300 mb-2">
                Password
              </label>
              <input
                id="password"
                name="password"
                type="password"
                required
                className={`input-field ${
                  errors.password ? 'border-red-500 focus:border-red-500' : ''
                }`}
                placeholder="Create a password"
                value={formData.password}
                onChange={handleChange}
              />
              {errors.password && (
                <p className="mt-2 text-sm text-red-400 flex items-center">
                  <span className="mr-1">⚠️</span>
                  {errors.password}
                </p>
              )}
            </div>

            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-300 mb-2">
                Confirm Password
              </label>
              <input
                id="confirmPassword"
                name="confirmPassword"
                type="password"
                required
                className={`input-field ${
                  errors.confirmPassword ? 'border-red-500 focus:border-red-500' : ''
                }`}
                placeholder="Confirm your password"
                value={formData.confirmPassword}
                onChange={handleChange}
              />
              {errors.confirmPassword && (
                <p className="mt-2 text-sm text-red-400 flex items-center">
                  <span className="mr-1">⚠️</span>
                  {errors.confirmPassword}
                </p>
              )}
            </div>

            {errors.submit && (
              <div className="bg-red-900/20 border border-red-800 rounded-xl p-4">
                <p className="text-sm text-red-400 flex items-center">
                  <span className="mr-2">❌</span>
                  {errors.submit}
                </p>
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className={`w-full btn-primary text-lg py-4 ${
                loading ? 'opacity-50 cursor-not-allowed' : ''
              }`}
            >
              {loading ? (
                <span className="flex items-center justify-center">
                  <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
                  Creating account...
                </span>
              ) : (
                <span className="flex items-center justify-center">
                   Create Account
                </span>
              )}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
} 