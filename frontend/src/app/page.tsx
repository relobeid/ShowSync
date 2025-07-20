'use client';

import { useState, useEffect } from 'react';
import Layout from "@/components/layout/Layout";

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
        initial: "J",
        name: "Jake @filmjunkie23",
        bio: "Horror movie addict",
        review: "rated hereditary 5 stars on showsync and immediately got matched with 3 other people who also loved psychological horror 💀 we ended up talking for hours about ari aster's cinematography and now we have a group chat that's basically our own film school",
        rating: "found my horror movie soulmates",
        bgColor: "from-blue-500 to-blue-700"
      },
      {
        initial: "S",
        name: "Sofia @kdramaobsessed",
        bio: "Anime & K-drama stan",
        review: "the ai somehow knew from my ratings that i'd love parasite even tho i mostly rated anime 5 stars?? got instantly connected with people who also appreciate dark social commentary and now we rate everything together and discuss in real time",
        rating: "AI actually reads my taste",
        bgColor: "from-purple-500 to-purple-700"
      }
    ],
    // Page 2 - More formal/analytical
    [
      {
        initial: "A",
        name: "Alex Chen",
        bio: "Film studies graduate",
        review: "As someone who studies cinema professionally, I was skeptical about AI taste matching. However, ShowSync's algorithm connected me with viewers who share my appreciation for complex narratives and visual storytelling. The discussions we have about cinematography and direction are incredibly insightful.",
        rating: "surprisingly sophisticated matching",
        bgColor: "from-green-500 to-green-700"
      },
      {
        initial: "M",
        name: "Maya Rodriguez",
        bio: "Independent bookstore owner",
        review: "I tend to gravitate toward literary fiction and experimental narratives—both in books and films. ShowSync found people with eerily similar reading/viewing habits. We've created this amazing cross-pollination between book discussions and their film adaptations.",
        rating: "found my intellectual tribe",
        bgColor: "from-indigo-500 to-indigo-700"
      }
    ],
    // Page 3 - Very casual/Gen Z
    [
      {
        initial: "T",
        name: "Tyler @chaoscinema",
        bio: "Unhinged movie takes",
        review: "bruh this app is UNREAL... gave the room 5 stars as a joke and got matched with other people who unironically love so-bad-it's-good movies 😭😭 we're planning a neil breen marathon and i've never been more excited about anything in my life",
        rating: "chaos energy matched perfectly",
        bgColor: "from-pink-500 to-pink-700"
      },
      {
        initial: "Z",
        name: "Zara ✨",
        bio: "Comfort show connoisseur",
        review: "ok but like... how did it know i rewatch the office AND parks and rec on repeat??? matched me with ppl who also need background noise shows and we've created the ultimate comfort viewing rotation. finally people who get it 🥺",
        rating: "comfort show solidarity",
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
      <div className={`theater-curtains ${showCurtains ? 'curtains-visible' : 'curtains-hidden'} spotlight relative py-24 mb-16`}>
        <div className="relative z-20 text-center animate-fade-in">
          <div className="flex justify-center items-center mb-8">
            <div className="movie-reel mr-6 animate-pulse"></div>
      <div className="text-center">
              <div className="text-6xl font-bold text-white mb-2">Your AI</div>
              <div className="text-5xl font-bold h-20 flex items-center justify-center">
                <span 
                  className={`word-animation ${isAnimating ? 'word-exit' : 'word-enter'}`}
                  style={{
                    background: 'linear-gradient(45deg, #e50914, #f40612, #ff6b6b)',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent',
                    backgroundClip: 'text',
                    textShadow: '0 0 30px rgba(229, 9, 20, 0.5)',
                    filter: 'drop-shadow(0 4px 8px rgba(229, 9, 20, 0.3))'
                  }}
                >
                  {flickerWords[currentWord]}
                </span>
              </div>
              <div className="text-6xl font-bold text-white">Club</div>
            </div>
            <div className="movie-reel ml-6 animate-pulse" style={{ animationDelay: '0.5s' }}></div>
          </div>
          
          <p className="text-2xl text-gray-300 mb-4 max-w-4xl mx-auto leading-relaxed font-medium">
            Rate movies, shows, and books on ShowSync. Our AI reads your taste and instantly connects you 
            with people who get exactly what you love. Never feel alone while watching again.
          </p>
          
          <p className="text-lg text-yellow-300 mb-8 max-w-2xl mx-auto">
            <span className="font-semibold">Discord meets IMDB:</span> Rate media, get matched with your taste twins, start discussing immediately
          </p>

          <div className="flex flex-col sm:flex-row gap-6 justify-center items-center">
            <button className="btn-primary text-xl px-10 py-5 ai-glow">
              <span className="movie-reel w-6 h-6 mr-3 inline-block scale-50"></span>
              Build My Taste Profile
            </button>
            <button className="btn-secondary text-xl px-10 py-5">
              <span className="popcorn-icon mr-3"></span>
              See How It Works
            </button>
          </div>
        </div>
      </div>

      {/* Film Strip Divider */}
      <div className="film-strip h-8 mb-16 opacity-60"></div>

      {/* How ShowSync Works */}
      <div className="mb-20">
        <h2 className="text-center text-4xl font-bold mb-4 text-white">
          How ShowSync Eliminates Media Loneliness
        </h2>
        <p className="text-center text-gray-400 mb-12 text-lg max-w-3xl mx-auto">
          Rate what you watch and read. Our AI finds your taste twins. Connect instantly for real conversations.
        </p>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Step 1 */}
          <div className="media-card p-8 text-center animate-fade-in" style={{ animationDelay: '0.2s' }}>
            <div className="relative mb-6">
              <div className="w-20 h-20 mx-auto bg-gradient-to-br from-purple-500 to-purple-700 rounded-full flex items-center justify-center text-3xl shadow-2xl">
                ⭐
              </div>
              <div className="absolute -top-2 -right-2 w-8 h-8 bg-red-500 rounded-full flex items-center justify-center text-sm font-bold animate-pulse">
                AI
              </div>
            </div>
            <h3 className="text-2xl font-bold mb-4 text-yellow-300">Rate Your Favorites</h3>
            <p className="text-gray-300 leading-relaxed">
              Use ShowSync to rate movies, TV shows, and books you love or hate. The more you rate, the better our AI understands your unique taste fingerprint.
            </p>
          </div>

          {/* Step 2 */}
          <div className="media-card p-8 text-center animate-fade-in" style={{ animationDelay: '0.4s' }}>
            <div className="relative mb-6">
              <div className="w-20 h-20 mx-auto bg-gradient-to-br from-blue-500 to-indigo-700 rounded-full flex items-center justify-center text-3xl shadow-2xl">
                🧠
              </div>
              <div className="absolute -top-2 -right-2 w-8 h-8 bg-green-500 rounded-full flex items-center justify-center text-xs font-bold">
                SYNC
              </div>
            </div>
            <h3 className="text-2xl font-bold mb-4 text-yellow-300">AI Finds Your People</h3>
            <p className="text-gray-300 leading-relaxed">
              Our AI analyzes your ratings and instantly syncs you with people who have eerily similar taste. No more wondering if anyone else "gets" your weird movie preferences.
            </p>
          </div>

          {/* Step 3 */}
          <div className="media-card p-8 text-center animate-fade-in" style={{ animationDelay: '0.6s' }}>
            <div className="relative mb-6">
              <div className="w-20 h-20 mx-auto bg-gradient-to-br from-red-500 to-pink-700 rounded-full flex items-center justify-center text-3xl shadow-2xl">
                💬
              </div>
              <div className="absolute -top-2 -right-2 w-8 h-8 bg-yellow-500 rounded-full flex items-center justify-center text-xs font-bold text-black">
                NOW
              </div>
            </div>
            <h3 className="text-2xl font-bold mb-4 text-yellow-300">Instant Discussion</h3>
            <p className="text-gray-300 leading-relaxed">
              Jump into Discord-style chats with your taste twins. Share reactions, debate plot holes, and discover new obsessions. Never watch or read alone again.
            </p>
          </div>
        </div>
      </div>

      {/* Real User Experiences - Carousel */}
      <div className="mb-20">
        <h2 className="text-center text-4xl font-bold mb-12 text-white">
          Real Stories From Real Viewers
        </h2>
        
        <div className="relative">
          {/* Navigation arrows */}
          <button 
            onClick={prevPage}
            className="absolute left-0 top-1/2 transform -translate-y-1/2 z-10 bg-gray-800 hover:bg-gray-700 rounded-full p-3 transition-colors"
          >
            <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          
          <button 
            onClick={nextPage}
            className="absolute right-0 top-1/2 transform -translate-y-1/2 z-10 bg-gray-800 hover:bg-gray-700 rounded-full p-3 transition-colors"
          >
            <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </button>

          {/* Testimonial Content */}
          <div className="mx-12">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              {allTestimonials[currentTestimonialPage].map((testimonial, index) => (
                <div key={index} className="glass-effect rounded-2xl p-8 border border-yellow-600/30 animate-fade-in">
                  <div className="flex items-center mb-4">
                    <div className={`w-12 h-12 bg-gradient-to-br ${testimonial.bgColor} rounded-full flex items-center justify-center text-white font-bold mr-4`}>
                      {testimonial.initial}
                    </div>
                    <div>
                      <p className="font-semibold text-white">{testimonial.name}</p>
                      <p className="text-sm text-gray-400">{testimonial.bio}</p>
                    </div>
                  </div>
                  <p className="text-gray-300 leading-relaxed mb-4">
                    "{testimonial.review}"
                  </p>
                  <div className="text-sm text-yellow-400">
                    ⭐⭐⭐⭐⭐ "{testimonial.rating}"
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Clickable indicators */}
          <div className="flex justify-center mt-8 space-x-3">
            {Array.from({ length: totalPages }).map((_, index) => (
              <button
                key={index}
                onClick={() => goToPage(index)}
                className={`w-3 h-3 rounded-full transition-all duration-200 ${
                  currentTestimonialPage === index 
                    ? 'bg-red-500 scale-125' 
                    : 'bg-gray-600 hover:bg-gray-500'
                }`}
              />
            ))}
          </div>
        </div>
      </div>

      {/* Updated Stats Section with Better Numbers */}
      <div className="mb-20">
        <div className="glass-effect rounded-2xl p-8 text-center">
          <h3 className="text-subtitle mb-8">The Community Vibes</h3>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="text-3xl font-bold gradient-text mb-2">47s</div>
              <div className="text-gray-400">Average time to find taste twins</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold gradient-text mb-2">96%</div>
              <div className="text-gray-400">Say AI nailed their taste match</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold gradient-text mb-2">7.3</div>
              <div className="text-gray-400">Hours spent discussing per week</div>
            </div>
          </div>
        </div>
      </div>

      {/* CTA Section */}
      <div className="theater-curtains">
        <div className="relative z-20 text-center py-16 animate-fade-in spotlight">
          <div className="ai-glow rounded-3xl p-12 max-w-4xl mx-auto">
            <h3 className="text-4xl font-bold mb-6 text-white">Ready to Find Your Taste Twins?</h3>
            <p className="text-xl text-gray-300 mb-8 leading-relaxed">
              Stop watching movies and shows in isolation. Start connecting with people who actually understand your taste in media.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <button className="btn-primary text-xl px-10 py-5">
                <span className="movie-reel w-6 h-6 mr-3 inline-block scale-50"></span>
                Start Rating & Connecting
              </button>
              <button className="btn-secondary text-xl px-10 py-5">
                <span className="popcorn-icon mr-3"></span>
                Browse Active Discussions
              </button>
            </div>
            <p className="text-sm text-gray-400 mt-6">
              Rate media • AI taste matching • Instant discussion • Never watch alone
            </p>
          </div>
        </div>
    </div>

      {/* Film Strip Footer */}
      <div className="film-strip h-6 mt-16 opacity-40"></div>
    </Layout>
  );
}
