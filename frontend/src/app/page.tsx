'use client';

import { useState, useEffect } from 'react';
import Layout from '@/components/layout/Layout';

export default function Home() {
  const [currentTestimonialPage, setCurrentTestimonialPage] = useState(0);
  const [currentWord, setCurrentWord] = useState(0);
  const [isAnimating, setIsAnimating] = useState(false);
  const [showCurtains, setShowCurtains] = useState(true);
  
  const flickerWords = ['Movie', 'Book', 'TV', 'Anime', 'Media'];
  
  useEffect(() => {
    const interval = setInterval(() => {
      setIsAnimating(true);
      setTimeout(() => {
        setCurrentWord((prev) => (prev + 1) % flickerWords.length);
        setIsAnimating(false);
      }, 300); // Halfway through animation
    }, 2500); // Change word every 2.5 seconds
    
    return () => clearInterval(interval);
  }, [flickerWords.length]);

  // Scroll listener to hide curtains
  useEffect(() => {
    const handleScroll = () => {
      const heroSection = window.innerHeight * 0.8; // Hide after scrolling past hero
      if (window.scrollY > heroSection) {
        setShowCurtains(false);
      } else {
        setShowCurtains(true);
      }
    };

    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // All testimonials with varied writing styles
  const allTestimonials = [
    // Page 1 - Mix of casual and formal
    [
      {
        name: "Alex Chen",
        role: "Movie Enthusiast",
        review: "ShowSync changed how I discover movies! The AI recommendations are scarily accurate.",
        rating: 5,
        avatar: "AC",
        bgColor: "from-purple-500 to-pink-600"
      },
      {
        name: "Sarah Martinez",
        role: "Book Club Leader",
        review: "Finally, a platform that gets both my reading and viewing habits. Love the sync features!",
        rating: 5,
        avatar: "SM",
        bgColor: "from-blue-500 to-cyan-600"
      },
      {
        name: "Jordan Kim",
        role: "Streaming Addict",
        review: "No more endless scrolling through Netflix! ShowSync knows exactly what I want to watch.",
        rating: 5,
        avatar: "JK",
        bgColor: "from-green-500 to-teal-600"
      }
    ],
    // Page 2 - Professional reviews
    [
      {
        name: "Emily Rodriguez",
        role: "Entertainment Journalist",
        review: "ShowSync's cross-platform integration is revolutionary. It's like having a personal curator.",
        rating: 5,
        avatar: "ER",
        bgColor: "from-red-500 to-rose-600"
      },
      {
        name: "Michael Thompson",
        role: "Film Critic",
        review: "The social features make movie discussions so much more engaging. Highly recommended!",
        rating: 5,
        avatar: "MT",
        bgColor: "from-indigo-500 to-purple-600"
      },
      {
        name: "Lisa Wang",
        role: "Tech Reviewer",
        review: "Beautiful interface, smart recommendations, and great community features. Five stars!",
        rating: 5,
        avatar: "LW",
        bgColor: "from-yellow-500 to-orange-600"
      }
    ]
  ];

  const totalPages = allTestimonials.length;

  const nextPage = () => {
    setCurrentTestimonialPage((prev) => (prev + 1) % totalPages);
  };

  const prevPage = () => {
    setCurrentTestimonialPage((prev) => (prev - 1 + totalPages) % totalPages);
  };

  const goToPage = (page: number) => {
    setCurrentTestimonialPage(page);
  };

  return (
    <Layout>
      {/* Cinema Hero Section with Theater Curtains */}
      <div className={`theater-curtains ${showCurtains ? 'curtains-visible' : 'curtains-hidden'} spotlight relative flex items-center justify-center mobile-py-8 lg:pt-24`} style={{ minHeight: 'calc(100vh - 88px)' }}>
        {/* Floating Media Symbols - Responsive positioning */}
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          {/* Left side floating symbols */}
          <div className="absolute left-4 sm:left-16 lg:left-32 top-1/4 w-12 sm:w-14 lg:w-16 h-12 sm:h-14 lg:h-16 opacity-10 animate-bounce text-gray-400" style={{ animationDelay: '0s', animationDuration: '3s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M18 4l2 4h-3l-2-4h-2l2 4h-3l-2-4H8l2 4H7L5 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V4h-4z"/>
            </svg>
          </div>
          <div className="absolute left-8 sm:left-24 lg:left-48 top-1/2 w-10 sm:w-11 lg:w-12 h-10 sm:h-11 lg:h-12 opacity-20 animate-bounce text-blue-400" style={{ animationDelay: '1s', animationDuration: '4s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M21 3H3c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h18c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H3V5h18v14zm-10-7l5 3-5 3V9z"/>
            </svg>
          </div>
          <div className="absolute left-6 sm:left-20 lg:left-40 top-3/4 w-12 sm:w-13 lg:w-14 h-12 sm:h-13 lg:h-14 opacity-15 animate-bounce text-green-400" style={{ animationDelay: '2s', animationDuration: '3.5s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4z"/>
            </svg>
          </div>
          
          {/* Right side floating symbols */}
          <div className="absolute right-4 sm:right-16 lg:right-32 top-1/3 w-10 sm:w-12 lg:w-14 h-10 sm:h-12 lg:h-14 opacity-15 animate-bounce text-red-400" style={{ animationDelay: '0.5s', animationDuration: '3.8s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
          </div>
          <div className="absolute right-8 sm:right-24 lg:right-48 top-2/3 w-12 sm:w-14 lg:w-16 h-12 sm:h-14 lg:h-16 opacity-10 animate-bounce text-yellow-400" style={{ animationDelay: '1.5s', animationDuration: '4.2s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M9.5 3A6.5 6.5 0 0 1 16 9.5c0 1.61-.59 3.09-1.56 4.23l.27.27h.79l5 5-1.5 1.5-5-5v-.79l-.27-.27C12.59 16.41 11.11 17 9.5 17A6.5 6.5 0 0 1 3 10.5A6.5 6.5 0 0 1 9.5 3m0 2C7.01 5 5 7.01 5 9.5S7.01 14 9.5 14 14 11.99 14 9.5 11.99 5 9.5 5z"/>
            </svg>
          </div>
          <div className="absolute right-6 sm:right-20 lg:right-40 top-1/6 w-8 sm:w-10 lg:w-12 h-8 sm:h-10 lg:h-12 opacity-20 animate-bounce text-purple-400" style={{ animationDelay: '2.5s', animationDuration: '3.2s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
          </div>
        </div>

        {/* Main Hero Content */}
        <div className="container-responsive text-center relative z-10">
          <div className="max-w-4xl mx-auto space-y-responsive">
            {/* Main Heading with responsive text */}
            <h1 className="text-hero animate-fade-in mobile-text-center">
              Your Ultimate{' '}
              <span className={`word-animation gradient-text ${isAnimating ? 'word-exit' : 'word-enter'}`}>
                {flickerWords[currentWord]}
              </span>{' '}
              Companion
            </h1>
            
            {/* Subtitle with responsive sizing */}
            <p className="text-subtitle text-gray-300 animate-fade-in mobile-text-center px-4 lg:px-0" style={{ animationDelay: '0.2s' }}>
              Track, discover, and share your favorite movies, shows, books, and more. 
              <span className="hidden sm:inline"><br /></span>
              <span className="sm:hidden"> </span>
              Join millions building their perfect entertainment library.
            </p>

            {/* CTA Buttons with responsive layout */}
            <div className="flex flex-col sm:flex-row gap-4 sm:gap-6 justify-center items-center animate-fade-in mobile-px-4" style={{ animationDelay: '0.4s' }}>
              <button className="btn-primary btn-responsive w-full sm:w-auto">
                <span className="popcorn-icon mr-2 scale-75"></span>
                Start Your Journey
              </button>
              <button className="btn-secondary btn-responsive w-full sm:w-auto">
                <span className="mr-2">👀</span>
                Watch Demo
              </button>
            </div>

            {/* Feature highlights - responsive layout */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 lg:gap-8 mt-8 lg:mt-16 animate-fade-in mobile-px-4" style={{ animationDelay: '0.6s' }}>
              <div className="glass-effect rounded-xl p-4 lg:p-6 border border-gray-700 text-center">
                <div className="text-2xl lg:text-3xl mb-2">🤖</div>
                <h3 className="font-semibold text-white text-sm lg:text-base mb-2">AI-Powered</h3>
                <p className="text-gray-400 text-xs lg:text-sm">Smart recommendations based on your taste</p>
              </div>
              <div className="glass-effect rounded-xl p-4 lg:p-6 border border-gray-700 text-center">
                <div className="text-2xl lg:text-3xl mb-2">🔄</div>
                <h3 className="font-semibold text-white text-sm lg:text-base mb-2">Cross-Platform</h3>
                <p className="text-gray-400 text-xs lg:text-sm">Movies, TV shows, books, and more</p>
              </div>
              <div className="glass-effect rounded-xl p-4 lg:p-6 border border-gray-700 text-center">
                <div className="text-2xl lg:text-3xl mb-2">👥</div>
                <h3 className="font-semibold text-white text-sm lg:text-base mb-2">Social</h3>
                <p className="text-gray-400 text-xs lg:text-sm">Share and discover with friends</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Features Section */}
      <section className="container-responsive mobile-py-8 lg:py-20 space-y-responsive">
        <div className="text-center mb-8 lg:mb-16">
          <h2 className="text-title mb-4 lg:mb-6 animate-fade-in mobile-text-center">
            Everything You Need in One Place
          </h2>
          <p className="text-gray-400 text-lg lg:text-xl max-w-3xl mx-auto animate-fade-in mobile-px-4" style={{ animationDelay: '0.2s' }}>
            From tracking your watchlist to discovering hidden gems, ShowSync makes managing your entertainment effortless and fun.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 lg:gap-12">
          {/* Feature 1 */}
          <div className="glass-effect rounded-2xl p-6 lg:p-8 border border-gray-700 animate-fade-in" style={{ animationDelay: '0.3s' }}>
            <div className="flex items-start gap-4 lg:gap-6">
              <div className="movie-reel w-12 h-12 lg:w-16 lg:h-16 flex-shrink-0"></div>
              <div>
                <h3 className="text-xl lg:text-2xl font-bold text-white mb-3 lg:mb-4">Smart Library Management</h3>
                <p className="text-gray-300 mb-4 lg:mb-6">
                  Organize your entire entertainment collection with intelligent categorization, 
                  custom lists, and progress tracking across all your devices.
                </p>
                <ul className="space-y-2 text-gray-400 text-sm lg:text-base">
                  <li className="flex items-center"><span className="text-green-400 mr-2">✓</span> Auto-sync across devices</li>
                  <li className="flex items-center"><span className="text-green-400 mr-2">✓</span> Custom collections</li>
                  <li className="flex items-center"><span className="text-green-400 mr-2">✓</span> Progress tracking</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Feature 2 */}
          <div className="glass-effect rounded-2xl p-6 lg:p-8 border border-gray-700 animate-fade-in" style={{ animationDelay: '0.4s' }}>
            <div className="flex items-start gap-4 lg:gap-6">
              <div className="ai-glow rounded-xl p-3 lg:p-4 flex-shrink-0">
                <span className="text-2xl lg:text-3xl">🤖</span>
              </div>
              <div>
                <h3 className="text-xl lg:text-2xl font-bold text-white mb-3 lg:mb-4">AI-Powered Discovery</h3>
                <p className="text-gray-300 mb-4 lg:mb-6">
                  Our advanced AI learns your preferences and suggests content you&apos;ll love, 
                  even from genres you haven&apos;t explored yet.
                </p>
                <ul className="space-y-2 text-gray-400 text-sm lg:text-base">
                  <li className="flex items-center"><span className="text-blue-400 mr-2">✓</span> Personalized recommendations</li>
                  <li className="flex items-center"><span className="text-blue-400 mr-2">✓</span> Cross-genre discovery</li>
                  <li className="flex items-center"><span className="text-blue-400 mr-2">✓</span> Mood-based suggestions</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Feature 3 */}
          <div className="glass-effect rounded-2xl p-6 lg:p-8 border border-gray-700 animate-fade-in" style={{ animationDelay: '0.5s' }}>
            <div className="flex items-start gap-4 lg:gap-6">
              <div className="bg-gradient-to-br from-purple-500 to-pink-600 rounded-xl p-3 lg:p-4 flex-shrink-0">
                <span className="text-2xl lg:text-3xl">👥</span>
              </div>
              <div>
                <h3 className="text-xl lg:text-2xl font-bold text-white mb-3 lg:mb-4">Social Features</h3>
                <p className="text-gray-300 mb-4 lg:mb-6">
                  Share reviews, create watch parties, and discover what your friends are loving. 
                  Entertainment is better when shared.
                </p>
                <ul className="space-y-2 text-gray-400 text-sm lg:text-base">
                  <li className="flex items-center"><span className="text-purple-400 mr-2">✓</span> Friend recommendations</li>
                  <li className="flex items-center"><span className="text-purple-400 mr-2">✓</span> Watch parties</li>
                  <li className="flex items-center"><span className="text-purple-400 mr-2">✓</span> Review system</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Feature 4 */}
          <div className="glass-effect rounded-2xl p-6 lg:p-8 border border-gray-700 animate-fade-in" style={{ animationDelay: '0.6s' }}>
            <div className="flex items-start gap-4 lg:gap-6">
              <div className="bg-gradient-to-br from-yellow-500 to-orange-600 rounded-xl p-3 lg:p-4 flex-shrink-0">
                <span className="text-2xl lg:text-3xl">📊</span>
              </div>
              <div>
                <h3 className="text-xl lg:text-2xl font-bold text-white mb-3 lg:mb-4">Detailed Analytics</h3>
                <p className="text-gray-300 mb-4 lg:mb-6">
                  Get insights into your viewing habits, favorite genres, and discover patterns 
                  in your entertainment consumption.
                </p>
                <ul className="space-y-2 text-gray-400 text-sm lg:text-base">
                  <li className="flex items-center"><span className="text-yellow-400 mr-2">✓</span> Viewing statistics</li>
                  <li className="flex items-center"><span className="text-yellow-400 mr-2">✓</span> Genre breakdown</li>
                  <li className="flex items-center"><span className="text-yellow-400 mr-2">✓</span> Time tracking</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Testimonials Section */}
      <section className="container-responsive mobile-py-8 lg:py-20">
        <div className="text-center mb-8 lg:mb-16">
          <h2 className="text-title mb-4 lg:mb-6 animate-fade-in mobile-text-center">
            What Our Users Say
          </h2>
          <p className="text-gray-400 text-lg lg:text-xl max-w-3xl mx-auto animate-fade-in mobile-px-4" style={{ animationDelay: '0.2s' }}>
            Join thousands of entertainment enthusiasts who&apos;ve found their perfect companion.
          </p>
        </div>

        <div className="glass-effect rounded-2xl p-6 lg:p-8 border border-gray-700 animate-fade-in" style={{ animationDelay: '0.3s' }}>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 lg:gap-8 mb-6 lg:mb-8">
            {allTestimonials[currentTestimonialPage].map((testimonial, index) => (
              <div key={index} className="text-center animate-fade-in" style={{ animationDelay: `${index * 0.1}s` }}>
                <div className={`w-16 h-16 lg:w-20 lg:h-20 mx-auto mb-3 lg:mb-4 rounded-full bg-gradient-to-br ${testimonial.bgColor} flex items-center justify-center text-white font-bold text-lg lg:text-xl shadow-lg`}>
                  {testimonial.avatar}
                </div>
                <div className="flex justify-center mb-2 lg:mb-3">
                  {[...Array(testimonial.rating)].map((_, i) => (
                    <span key={i} className="text-yellow-400 text-lg lg:text-xl">⭐</span>
                  ))}
                </div>
                <p className="text-gray-300 mb-3 lg:mb-4 italic text-sm lg:text-base">
                  &quot;{testimonial.review}&quot;
                </p>
                <div>
                  <p className="font-semibold text-white text-sm lg:text-base">{testimonial.name}</p>
                  <p className="text-gray-400 text-xs lg:text-sm">{testimonial.role}</p>
                </div>
              </div>
            ))}
          </div>

          {/* Testimonial Navigation */}
          <div className="flex justify-center items-center gap-4 lg:gap-6">
            <button
              onClick={prevPage}
              className="p-2 lg:p-3 rounded-full bg-gray-800 hover:bg-gray-700 text-gray-400 hover:text-white transition-all"
            >
              <svg className="w-5 h-5 lg:w-6 lg:h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            
            <div className="flex gap-2">
              {Array.from({ length: totalPages }, (_, i) => (
                <button
                  key={i}
                  onClick={() => goToPage(i)}
                  className={`w-2 h-2 lg:w-3 lg:h-3 rounded-full transition-all ${
                    i === currentTestimonialPage ? 'bg-red-500' : 'bg-gray-600 hover:bg-gray-500'
                  }`}
                />
              ))}
            </div>
            
            <button
              onClick={nextPage}
              className="p-2 lg:p-3 rounded-full bg-gray-800 hover:bg-gray-700 text-gray-400 hover:text-white transition-all"
            >
              <svg className="w-5 h-5 lg:w-6 lg:h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="container-responsive mobile-py-8 lg:py-20 text-center">
        <div className="glass-effect rounded-2xl p-8 lg:p-16 border border-gray-700 ai-glow animate-fade-in">
          <h2 className="text-title mb-4 lg:mb-6 mobile-text-center">
            Ready to Transform Your Entertainment Experience?
          </h2>
          <p className="text-gray-300 text-lg lg:text-xl mb-6 lg:mb-8 max-w-2xl mx-auto mobile-px-4">
            Join ShowSync today and never struggle to find something to watch, read, or explore again.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 sm:gap-6 justify-center items-center mobile-px-4">
            <button className="btn-primary btn-responsive w-full sm:w-auto text-lg lg:text-xl px-8 lg:px-12 py-3 lg:py-4">
              <span className="popcorn-icon mr-2"></span>
              Get Started Free
            </button>
            <p className="text-gray-400 text-sm lg:text-base">
              No credit card required • Free forever plan available
            </p>
          </div>
        </div>
      </section>
    </Layout>
  );
}
