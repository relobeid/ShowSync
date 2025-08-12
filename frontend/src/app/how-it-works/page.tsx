'use client';

import Link from 'next/link';
import Layout from '@/components/layout/Layout';

export default function HowItWorksPage() {
  return (
    <Layout>
      <div className="min-h-screen pt-24 pb-12 px-4">
        <div className="max-w-6xl mx-auto">

          {/* Step 1 */}
          <div className="mb-32">
            <div className="text-center mb-12">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-red-600 text-white rounded-full text-2xl font-bold mb-6">
                1
              </div>
              <h2 className="text-4xl font-bold text-white mb-4">
                Choose and Rate Your Favorite Media
              </h2>
              <p className="text-xl text-gray-400 max-w-2xl mx-auto">
                Just like IMDB and Rotten Tomatoes, rate movies, TV shows, and books you love
              </p>
            </div>
            
            {/* Rating Interface Mockup */}
            <div className="max-w-6xl mx-auto">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                {/* Movie Card 1 - The Matrix */}
                <div className="group relative">
                  <div className="glass-effect rounded-2xl p-6 border border-gray-700/50 hover:border-red-500/30 transition-all duration-500 hover:scale-105 hover:shadow-2xl hover:shadow-red-500/10">
                    {/* Poster Container */}
                    <div className="relative mb-6">
                      <div className="w-full h-64 rounded-xl overflow-hidden bg-gray-700/50 backdrop-blur-sm border border-gray-600/30">
                        <img 
                          src="https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg" 
                          alt="The Matrix poster"
                          className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                        />
                        {/* Subtle overlay */}
                        <div className="absolute inset-0 bg-gradient-to-t from-gray-900/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                      </div>
                      {/* Rating badge */}
                      <div className="absolute -top-3 -right-3 bg-gradient-to-r from-red-500 to-red-600 text-white px-3 py-1 rounded-full text-sm font-bold shadow-lg">
                        5★
                      </div>
                    </div>
                    
                    {/* Content */}
                    <div className="space-y-4">
                      <div>
                        <h3 className="text-xl font-bold text-white mb-2 group-hover:text-red-400 transition-colors">The Matrix</h3>
                        <p className="text-gray-400 text-sm">1999 • Sci-Fi • Action</p>
                      </div>
                      
                      {/* Star Rating */}
                      <div className="flex items-center justify-center space-x-1 py-3">
                        {[1,2,3,4,5].map(i => (
                          <div key={i} className={`transition-all duration-200 ${i <= 5 ? 'text-yellow-400 scale-110' : 'text-gray-600'}`}>
                            <svg className="w-6 h-6 fill-current" viewBox="0 0 24 24">
                              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                            </svg>
                          </div>
                        ))}
                      </div>
                      
                      {/* Status */}
                      <div className="text-center">
                        <div className="inline-flex items-center px-4 py-2 bg-green-500/10 border border-green-500/30 rounded-full">
                          <svg className="w-4 h-4 text-green-400 mr-2" fill="currentColor" viewBox="0 0 20 20">
                            <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"/>
                          </svg>
                          <span className="text-green-400 text-sm font-medium">Loved it!</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Movie Card 2 - Dune */}
                <div className="group relative">
                  <div className="glass-effect rounded-2xl p-6 border border-gray-700/50 hover:border-yellow-500/30 transition-all duration-500 hover:scale-105 hover:shadow-2xl hover:shadow-yellow-500/10">
                    {/* Poster Container */}
                    <div className="relative mb-6">
                      <div className="w-full h-64 rounded-xl overflow-hidden bg-gray-700/50 backdrop-blur-sm border border-gray-600/30">
                        <img 
                          src="https://image.tmdb.org/t/p/w500/d5NXSklXo0qyIYkgV94XAgMIckC.jpg" 
                          alt="Dune poster"
                          className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                        />
                        <div className="absolute inset-0 bg-gradient-to-t from-gray-900/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                      </div>
                      {/* Rating badge */}
                      <div className="absolute -top-3 -right-3 bg-gradient-to-r from-yellow-500 to-orange-500 text-white px-3 py-1 rounded-full text-sm font-bold shadow-lg">
                        4★
                      </div>
                    </div>
                    
                    {/* Content */}
                    <div className="space-y-4">
                      <div>
                        <h3 className="text-xl font-bold text-white mb-2 group-hover:text-yellow-400 transition-colors">Dune</h3>
                        <p className="text-gray-400 text-sm">2021 • Sci-Fi • Adventure</p>
                      </div>
                      
                      {/* Star Rating */}
                      <div className="flex items-center justify-center space-x-1 py-3">
                        {[1,2,3,4,5].map(i => (
                          <div key={i} className={`transition-all duration-200 ${i <= 4 ? 'text-yellow-400 scale-110' : 'text-gray-600'}`}>
                            <svg className="w-6 h-6 fill-current" viewBox="0 0 24 24">
                              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                            </svg>
                          </div>
                        ))}
                      </div>
                      
                      {/* Status */}
                      <div className="text-center">
                        <div className="inline-flex items-center px-4 py-2 bg-blue-500/10 border border-blue-500/30 rounded-full">
                          <svg className="w-4 h-4 text-blue-400 mr-2" fill="currentColor" viewBox="0 0 20 20">
                            <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"/>
                          </svg>
                          <span className="text-blue-400 text-sm font-medium">Really good</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Movie Card 3 - Breaking Bad */}
                <div className="group relative">
                  <div className="glass-effect rounded-2xl p-6 border border-gray-700/50 hover:border-green-500/30 transition-all duration-500 hover:scale-105 hover:shadow-2xl hover:shadow-green-500/10">
                    {/* Poster Container */}
                    <div className="relative mb-6">
                      <div className="w-full h-64 rounded-xl overflow-hidden bg-gray-700/50 backdrop-blur-sm border border-gray-600/30">
                        <img 
                          src="https://image.tmdb.org/t/p/w500/3xnWaLQjelJDDF7LT1WBo6f4BRe.jpg" 
                          alt="Breaking Bad poster"
                          className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                        />
                        <div className="absolute inset-0 bg-gradient-to-t from-gray-900/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                      </div>
                      {/* Rating badge */}
                      <div className="absolute -top-3 -right-3 bg-gradient-to-r from-red-500 to-red-600 text-white px-3 py-1 rounded-full text-sm font-bold shadow-lg">
                        5★
                      </div>
                    </div>
                    
                    {/* Content */}
                    <div className="space-y-4">
                      <div>
                        <h3 className="text-xl font-bold text-white mb-2 group-hover:text-green-400 transition-colors">Breaking Bad</h3>
                        <p className="text-gray-400 text-sm">2008 • Crime • Drama</p>
                      </div>
                      
                      {/* Star Rating */}
                      <div className="flex items-center justify-center space-x-1 py-3">
                        {[1,2,3,4,5].map(i => (
                          <div key={i} className={`transition-all duration-200 ${i <= 5 ? 'text-yellow-400 scale-110' : 'text-gray-600'}`}>
                            <svg className="w-6 h-6 fill-current" viewBox="0 0 24 24">
                              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                            </svg>
                          </div>
                        ))}
                      </div>
                      
                      {/* Status */}
                      <div className="text-center">
                        <div className="inline-flex items-center px-4 py-2 bg-green-500/10 border border-green-500/30 rounded-full">
                          <svg className="w-4 h-4 text-green-400 mr-2" fill="currentColor" viewBox="0 0 20 20">
                            <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"/>
                          </svg>
                          <span className="text-green-400 text-sm font-medium">Masterpiece!</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Step 2 */}
          <div className="mb-32">
            <div className="text-center mb-12">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-red-600 text-white rounded-full text-2xl font-bold mb-6">
                2
              </div>
              <h2 className="text-4xl font-bold text-white mb-4">
                Let Our AI Do All The Magic
              </h2>
              <p className="text-xl text-gray-400 max-w-2xl mx-auto">
                Our advanced algorithms sync you with like-minded people who share your exact taste
              </p>
            </div>

            {/* AI Magic Mockup */}
            <div className="glass-effect rounded-2xl p-8 border border-gray-700 max-w-4xl mx-auto ai-glow">
              <div className="text-center mb-8">
                <div className="relative inline-block">
                  {/* ShowSync Logo with sophisticated glow */}
                  <div className="relative w-32 h-32 mx-auto mb-4">
                    <div className="absolute inset-0 bg-gradient-to-r from-red-500/20 via-red-400/40 to-red-500/20 rounded-full blur-xl animate-pulse"></div>
                    <div className="relative w-full h-full bg-gray-800/50 backdrop-blur-sm rounded-full border border-red-500/30 flex items-center justify-center shadow-2xl">
                      <img 
                        src="/logo.png" 
                        alt="ShowSync AI" 
                        className="w-20 h-20 filter drop-shadow-lg"
                      />
                    </div>
                  </div>
                  
                  {/* Sophisticated particle system */}
                  <div className="absolute inset-0 pointer-events-none">
                    {/* Orbital rings */}
                    <div className="absolute inset-0 flex items-center justify-center">
                      {/* Inner ring */}
                      <div className="absolute w-32 h-32 border border-red-400/20 rounded-full animate-spin" style={{ animationDuration: '8s' }}>
                        {Array.from({ length: 6 }).map((_, i) => (
                          <div
                            key={`inner-${i}`}
                            className="absolute w-1.5 h-1.5 bg-red-400 rounded-full -top-0.5 left-1/2 transform -translate-x-1/2 opacity-60"
                            style={{
                              transformOrigin: '0 64px',
                              transform: `translateX(-50%) rotate(${i * 60}deg)`,
                            }}
                          />
                        ))}
                      </div>
                      
                      {/* Middle ring */}
                      <div className="absolute w-40 h-40 border border-red-300/10 rounded-full animate-spin" style={{ animationDuration: '12s', animationDirection: 'reverse' }}>
                        {Array.from({ length: 8 }).map((_, i) => (
                          <div
                            key={`middle-${i}`}
                            className="absolute w-1 h-1 bg-red-300 rounded-full -top-0.5 left-1/2 transform -translate-x-1/2 opacity-40"
                            style={{
                              transformOrigin: '0 80px',
                              transform: `translateX(-50%) rotate(${i * 45}deg)`,
                            }}
                          />
                        ))}
                      </div>
                      
                      {/* Outer ring */}
                      <div className="absolute w-48 h-48 border border-red-200/5 rounded-full animate-spin" style={{ animationDuration: '16s' }}>
                        {Array.from({ length: 12 }).map((_, i) => (
                          <div
                            key={`outer-${i}`}
                            className="absolute w-0.5 h-0.5 bg-red-200 rounded-full -top-0.5 left-1/2 transform -translate-x-1/2 opacity-30"
                            style={{
                              transformOrigin: '0 96px',
                              transform: `translateX(-50%) rotate(${i * 30}deg)`,
                            }}
                          />
                        ))}
                      </div>
                    </div>
                    
                    {/* Floating sparkles */}
                    {Array.from({ length: 15 }).map((_, i) => (
                      <div
                        key={`sparkle-${i}`}
                        className="absolute w-1 h-1 bg-white rounded-full animate-pulse"
                        style={{
                          top: `${20 + Math.random() * 60}%`,
                          left: `${20 + Math.random() * 60}%`,
                          animationDelay: `${Math.random() * 3}s`,
                          animationDuration: `${2 + Math.random() * 2}s`
                        }}
                      />
                    ))}
                    
                    {/* Energy waves */}
                    <div className="absolute inset-0 flex items-center justify-center">
                      <div className="absolute w-24 h-24 border border-red-500/30 rounded-full animate-ping" style={{ animationDuration: '2s' }}></div>
                      <div className="absolute w-28 h-28 border border-red-400/20 rounded-full animate-ping" style={{ animationDuration: '2.5s', animationDelay: '0.5s' }}></div>
                      <div className="absolute w-32 h-32 border border-red-300/10 rounded-full animate-ping" style={{ animationDuration: '3s', animationDelay: '1s' }}></div>
                    </div>
                  </div>
                </div>
              </div>

              <div className="bg-gray-900 rounded-xl p-6">
                <div className="flex items-center justify-between mb-6">
                  <h3 className="text-lg font-semibold text-white">Taste Analysis Complete</h3>
                  <div className="text-green-400 text-sm">✓ 94% Match Found</div>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="bg-gray-800 rounded-lg p-4 text-center">
                    <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white font-bold text-lg mx-auto mb-3">
                      A
                    </div>
                    <div className="text-white font-semibold">Alex_SciFi</div>
                    <div className="text-sm text-gray-400">94% taste match</div>
                    <div className="text-xs text-green-400 mt-1">Online now</div>
                  </div>
                  
                  <div className="bg-gray-800 rounded-lg p-4 text-center">
                    <div className="w-12 h-12 bg-gradient-to-br from-green-500 to-teal-600 rounded-full flex items-center justify-center text-white font-bold text-lg mx-auto mb-3">
                      M
                    </div>
                    <div className="text-white font-semibold">Maya_BookLover</div>
                    <div className="text-sm text-gray-400">91% taste match</div>
                    <div className="text-xs text-green-400 mt-1">Online now</div>
                  </div>
                  
                  <div className="bg-gray-800 rounded-lg p-4 text-center">
                    <div className="w-12 h-12 bg-gradient-to-br from-red-500 to-pink-600 rounded-full flex items-center justify-center text-white font-bold text-lg mx-auto mb-3">
                      J
                    </div>
                    <div className="text-white font-semibold">Jake_CinemaFan</div>
                    <div className="text-sm text-gray-400">89% taste match</div>
                    <div className="text-xs text-yellow-400 mt-1">Away</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Step 3 */}
          <div className="mb-32">
            <div className="text-center mb-12">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-red-600 text-white rounded-full text-2xl font-bold mb-6">
                3
              </div>
              <h2 className="text-4xl font-bold text-white mb-4">
                Chat and Make New Friends
              </h2>
              <p className="text-xl text-gray-400 max-w-2xl mx-auto">
                Join Discord-like group chats with your taste buddies and discuss what you love
              </p>
            </div>

            {/* Discord-like Chat Mockup */}
            <div className="glass-effect rounded-2xl border border-gray-700 max-w-4xl mx-auto overflow-hidden">
              {/* Chat Header */}
              <div className="bg-gray-800 p-4 border-b border-gray-700">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-white font-semibold flex items-center">
                      <span className="text-red-500 mr-2">#</span>
                      sci-fi-lovers
                    </h3>
                    <p className="text-sm text-gray-400">3 members • Based on your shared taste in sci-fi</p>
                  </div>
                  <div className="flex items-center space-x-2">
                    <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
                    <span className="text-sm text-green-400">Live</span>
                  </div>
                </div>
              </div>

              {/* Chat Messages */}
              <div className="bg-gray-900 p-6 space-y-4">
                <div className="flex items-start space-x-3">
                  <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white font-bold">
                    A
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center space-x-2 mb-1">
                      <span className="text-sm font-semibold text-white">Alex_SciFi</span>
                      <span className="text-xs text-gray-500">Today at 2:34 PM</span>
                    </div>
                    <p className="text-gray-300">Just finished watching Blade Runner 2049 again... the cinematography is absolutely stunning! 😍</p>
                  </div>
                </div>

                <div className="flex items-start space-x-3">
                  <div className="w-10 h-10 bg-gradient-to-br from-green-500 to-teal-600 rounded-full flex items-center justify-center text-white font-bold">
                    M
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center space-x-2 mb-1">
                      <span className="text-sm font-semibold text-white">Maya_BookLover</span>
                      <span className="text-xs text-gray-500">Today at 2:35 PM</span>
                    </div>
                    <p className="text-gray-300">YES! Denis Villeneuve is a master. Have you read the original Philip K. Dick novel? The book adds so much depth to the story.</p>
                  </div>
                </div>

                <div className="flex items-start space-x-3">
                  <div className="w-10 h-10 bg-gradient-to-br from-red-500 to-red-700 rounded-full flex items-center justify-center text-white font-bold">
                    Y
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center space-x-2 mb-1">
                      <span className="text-sm font-semibold text-white">You</span>
                      <span className="text-xs text-gray-500">Today at 2:36 PM</span>
                    </div>
                    <p className="text-gray-300">I&apos;ve been meaning to read that! Adding it to my list now 📚</p>
                  </div>
                </div>

                {/* Typing indicator */}
                <div className="flex items-start space-x-3">
                  <div className="w-10 h-10 bg-gradient-to-br from-yellow-500 to-orange-600 rounded-full flex items-center justify-center text-white font-bold">
                    J
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center space-x-2 mb-1">
                      <span className="text-sm font-semibold text-white">Jake_CinemaFan</span>
                      <span className="text-xs text-gray-500">is typing...</span>
                    </div>
                    <div className="flex space-x-1">
                      <div className="w-2 h-2 bg-gray-500 rounded-full animate-bounce" style={{ animationDelay: '0s' }}></div>
                      <div className="w-2 h-2 bg-gray-500 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
                      <div className="w-2 h-2 bg-gray-500 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Message Input */}
              <div className="bg-gray-800 p-4">
                <div className="flex items-center space-x-3 bg-gray-700 rounded-lg p-3">
                  <div className="w-8 h-8 bg-gradient-to-br from-red-500 to-red-700 rounded-full flex items-center justify-center text-white font-bold text-sm">
                    Y
                  </div>
                  <div className="flex-1 text-gray-400">
                    Message #sci-fi-lovers
                  </div>
                  <div className="text-gray-500">
                    <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z"/>
                    </svg>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Final Statement */}
          <div className="text-center mb-20">
            <div className="glass-effect rounded-2xl p-12 border border-gray-700 max-w-2xl mx-auto">
              <h2 className="text-5xl font-bold text-white mb-8">
                That is ShowSync.
              </h2>
              <p className="text-xl text-gray-400 mb-8">
                Your AI-powered media companion that connects you with people who truly get your taste.
              </p>
              <Link href="/auth/register" className="btn-primary text-lg px-8 py-4">
                Get Started Now
              </Link>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}
