'use client';

import ProtectedRoute from '@/components/auth/ProtectedRoute';
import Layout from '@/components/layout/Layout';

export default function LibraryPage() {
  return (
    <ProtectedRoute>
      <Layout>
        <div className="container-responsive mobile-py-8">
          <div className="text-center py-20">
            <h1 className="text-3xl sm:text-4xl font-bold text-white mb-4">
              Your Media Library
            </h1>
            <p className="text-lg text-gray-400 mb-8">
              Keep track of everything you&apos;ve watched and want to watch
            </p>
            <div className="glass-effect rounded-2xl p-8 border border-gray-700 max-w-md mx-auto">
              <div className="text-6xl mb-4">📚</div>
              <h3 className="text-xl font-semibold text-white mb-2">
                Coming Soon
              </h3>
              <p className="text-gray-400">
                Your personal media library is being built. Soon you&apos;ll be able to track all your movies, shows, and books in one place.
              </p>
            </div>
          </div>
        </div>
      </Layout>
    </ProtectedRoute>
  );
}
