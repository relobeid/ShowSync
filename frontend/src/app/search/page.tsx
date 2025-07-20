'use client';

import { useState } from 'react';
import Layout from '@/components/layout/Layout';
import ProtectedRoute from '@/components/auth/ProtectedRoute';
import MediaCard from '@/components/media/MediaCard';

// Sample data for demonstration - will be replaced with real API calls
const sampleMovies = [
  {
    id: 1,
    type: 'MOVIE' as const,
    title: 'The Matrix',
    originalTitle: 'The Matrix',
    overview: 'A computer hacker learns from mysterious rebels about the true nature of his reality.',
    releaseDate: '1999-03-30',
    voteAverage: 8.2,
    voteCount: 26573,
    posterPath: '/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg',
  },
  {
    id: 2,
    type: 'MOVIE' as const,
    title: 'Blade Runner 2049',
    originalTitle: 'Blade Runner 2049',
    overview: 'Thirty years after the events of the first film, a new blade runner unearths a long-buried secret.',
    releaseDate: '2017-10-06',
    voteAverage: 8.0,
    voteCount: 15420,
    posterPath: '/gajva2L0rPYkEWjzgFlBXCAVBE5.jpg',
  },
  {
    id: 3,
    type: 'TV_SHOW' as const,
    title: 'Breaking Bad',
    originalTitle: 'Breaking Bad',
    overview: 'A high school chemistry teacher diagnosed with lung cancer turns to manufacturing methamphetamine.',
    releaseDate: '2008-01-20',
    voteAverage: 9.5,
    voteCount: 12500,
    posterPath: '/3xnWaLQjelJDDF7LT1WBo6f4BRe.jpg',
  },
  {
    id: 4,
    type: 'BOOK' as const,
    title: 'Dune',
    originalTitle: 'Dune',
    overview: 'Set in the distant future amidst a feudal interstellar society, Paul Atreides navigates political intrigue.',
    releaseDate: '1965-08-01',
    voteAverage: 4.6,
    voteCount: 3200,
    posterPath: null,
  },
];

const categories = [
  { id: 'suggestions', name: 'Suggestions for You', count: 24 },
  { id: 'trending', name: 'Trending Now', count: 18 },
  { id: 'new-arrivals', name: 'New Arrivals', count: 32 },
  { id: 'action', name: 'Action & Adventure', count: 156 },
  { id: 'drama', name: 'Drama', count: 203 },
  { id: 'horror', name: 'Horror & Thriller', count: 89 },
  { id: 'comedy', name: 'Comedy', count: 124 },
  { id: 'sci-fi', name: 'Sci-Fi & Fantasy', count: 98 },
  { id: 'documentary', name: 'Documentaries', count: 67 },
  { id: 'international', name: 'International', count: 145 },
];

export default function SearchPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('suggestions');
  const [mediaType, setMediaType] = useState<'all' | 'movies' | 'tv' | 'books'>('all');

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    // TODO: Implement actual search functionality
    console.log('Searching for:', searchQuery);
  };

  const filteredMedia = sampleMovies.filter(item => {
    if (mediaType === 'movies') return item.type === 'MOVIE';
    if (mediaType === 'tv') return item.type === 'TV_SHOW';
    if (mediaType === 'books') return item.type === 'BOOK';
    return true;
  });

  return (
    <ProtectedRoute>
      <Layout>
        <div className="flex gap-8 min-h-screen animate-fade-in">
          {/* Netflix-style Sidebar */}
          <div className="w-80 glass-effect rounded-2xl p-6 h-fit sticky top-24 border border-gray-700">
            <h2 className="text-xl font-bold text-white mb-6 flex items-center">
              <div className="movie-reel w-6 h-6 mr-3 scale-75"></div>
              Browse & Discover
            </h2>

            {/* Search Bar */}
            <form onSubmit={handleSearch} className="mb-8">
              <div className="relative">
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Search movies, shows, books..."
                  className="input-field pr-12 text-lg"
                />
                <button
                  type="submit"
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-white transition-colors"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
                  </svg>
                </button>
              </div>
            </form>

            {/* Media Type Filter */}
            <div className="mb-8">
              <h3 className="text-sm font-semibold text-gray-300 mb-3 uppercase tracking-wider">
                Content Type
              </h3>
              <div className="space-y-2">
                {[
                  { key: 'all', label: 'All Content', icon: '🎭' },
                  { key: 'movies', label: 'Movies', icon: '🎬' },
                  { key: 'tv', label: 'TV Shows', icon: '📺' },
                  { key: 'books', label: 'Books', icon: '📖' },
                ].map(({ key, label, icon }) => (
                  <button
                    key={key}
                    onClick={() => setMediaType(key as 'all' | 'movies' | 'tv' | 'books')}
                    className={`w-full text-left px-3 py-2 rounded-lg transition-all duration-200 ${
                      mediaType === key
                        ? 'bg-red-600 text-white shadow-lg'
                        : 'text-gray-300 hover:text-white hover:bg-gray-800'
                    }`}
                  >
                    <span className="flex items-center">
                      <span className="text-lg mr-3">{icon}</span>
                      {label}
                    </span>
                  </button>
                ))}
              </div>
            </div>

            {/* Categories */}
            <div className="space-y-1">
              <h3 className="text-sm font-semibold text-gray-300 mb-3 uppercase tracking-wider">
                Categories
              </h3>
              {categories.map((category) => (
                <button
                  key={category.id}
                  onClick={() => setSelectedCategory(category.id)}
                  className={`w-full text-left px-3 py-3 rounded-lg transition-all duration-200 group ${
                    selectedCategory === category.id
                      ? 'bg-gradient-to-r from-red-600 to-red-700 text-white shadow-lg'
                      : 'text-gray-300 hover:text-white hover:bg-gray-800'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-medium">{category.name}</span>
                    <span className={`text-xs px-2 py-1 rounded-full ${
                      selectedCategory === category.id
                        ? 'bg-white/20 text-white'
                        : 'bg-gray-700 text-gray-400 group-hover:bg-gray-600'
                    }`}>
                      {category.count}
                    </span>
                  </div>
                </button>
              ))}
            </div>

            {/* AI Taste Profile Section */}
            <div className="mt-8 ai-glow rounded-xl p-4">
              <h3 className="font-semibold text-white mb-2 flex items-center">
                <span className="w-2 h-2 bg-red-500 rounded-full animate-pulse mr-2"></span>
                AI Taste Profile
              </h3>
              <p className="text-sm text-gray-300 mb-3">
                Rate more content to improve your personalized recommendations
              </p>
              <div className="bg-gray-800 rounded-lg p-3">
                <div className="flex justify-between items-center mb-2">
                  <span className="text-xs text-gray-400">Profile Strength</span>
                  <span className="text-xs text-yellow-400 font-semibold">68%</span>
                </div>
                <div className="w-full bg-gray-700 rounded-full h-2">
                  <div className="bg-gradient-to-r from-yellow-400 to-red-500 h-2 rounded-full" style={{ width: '68%' }}></div>
                </div>
              </div>
            </div>
          </div>

          {/* Main Content Area */}
          <div className="flex-1">
            {/* Header */}
            <div className="mb-8">
              <div className="flex items-center justify-between mb-4">
                <h1 className="text-4xl font-bold text-white">
                  {categories.find(c => c.id === selectedCategory)?.name || 'Search Results'}
                </h1>
                <div className="flex items-center space-x-4">
                  <select className="bg-gray-800 border border-gray-600 rounded-lg px-3 py-2 text-white text-sm">
                    <option>Sort by Relevance</option>
                    <option>Sort by Rating</option>
                    <option>Sort by Year</option>
                    <option>Sort by Popularity</option>
                  </select>
                  <button className="btn-secondary text-sm px-4 py-2">
                    <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 100 4m0-4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 100 4m0-4v2m0-6V4"/>
                    </svg>
                    Filters
                  </button>
                </div>
              </div>
              
              {searchQuery && (
                <p className="text-gray-400">
                  Showing results for &quot;<span className="text-white font-medium">{searchQuery}</span>&quot; 
                  • {filteredMedia.length} matches found
                </p>
              )}
            </div>

            {/* Content Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {filteredMedia.map((media, index) => (
                <div
                  key={media.id}
                  className="animate-fade-in"
                  style={{ animationDelay: `${index * 0.1}s` }}
                >
                  <MediaCard
                    {...media}
                    onAddToLibrary={(id) => console.log('Add to library:', id)}
                    onViewDetails={(id) => console.log('View details:', id)}
                  />
                </div>
              ))}
            </div>

            {/* Show more content suggestion */}
            <div className="mt-12 text-center">
              <div className="glass-effect rounded-2xl p-8 border border-gray-700">
                <h3 className="text-2xl font-bold text-white mb-4">Want More Personalized Results?</h3>
                <p className="text-gray-300 mb-6 max-w-2xl mx-auto">
                  Rate a few more movies and shows to help our AI understand your taste better. 
                  The more you rate, the better your recommendations become.
                </p>
                <button className="btn-primary px-8 py-3">
                  <div className="movie-reel w-5 h-5 mr-2 inline-block scale-75"></div>
                  Improve My Recommendations
                </button>
              </div>
            </div>
          </div>
        </div>
      </Layout>
    </ProtectedRoute>
  );
} 